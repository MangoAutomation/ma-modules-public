/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.spring.db.AbstractBasicTableDefinition;
import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.vo.AbstractVO;

import net.jazdw.rql.parser.ASTNode;

/**
 * Use StreamedVORqlQueryWithTotal instead (need service)
 *
 * @author Jared Wiltshire
 */
@Deprecated
public class StreamedVOQueryWithTotal<T extends AbstractVO<?>, TABLE extends AbstractBasicTableDefinition, DAO extends AbstractBasicDao<T, TABLE>> implements StreamedArrayWithTotal {
    private final DAO dao;
    private final ConditionSortLimit conditions;
    private final Function<T, ?> toModel;
    private final Predicate<T> filter;

    public StreamedVOQueryWithTotal(DAO dao, ConditionSortLimit conditions) {
        this(dao, conditions, item -> true, Function.identity());
    }

    public StreamedVOQueryWithTotal(DAO dao, ASTNode rql) {
        this(dao, dao.rqlToCondition(rql), item -> true, Function.identity());
    }

    public StreamedVOQueryWithTotal(DAO dao, ASTNode rql, Function<T, ?> toModel) {
        this(dao, dao.rqlToCondition(rql), item -> true, toModel);
    }

    public StreamedVOQueryWithTotal(DAO dao, ASTNode rql, Predicate<T> filter, Function<T, ?> toModel) {
        this(dao, dao.rqlToCondition(rql), filter, toModel);
    }

    public StreamedVOQueryWithTotal(DAO dao, ConditionSortLimit conditions, Predicate<T> filter, Function<T, ?> toModel) {
        this.dao = dao;
        this.conditions = conditions;
        this.toModel = toModel;
        this.filter = filter;
    }

    @Override
    public StreamedArray getItems() {
        return new StreamedVOArray();
    }

    @Override
    public int getTotal() {
        return dao.customizedCount(conditions);
    }

    private class StreamedVOArray implements JSONStreamedArray {
        @Override
        public void writeArrayValues(JsonGenerator jgen) throws IOException {
            dao.customizedQuery(conditions, (T item, int index) -> {
                if (filter.test(item)) {
                    try {
                        jgen.writeObject(toModel.apply(item));
                    } catch (IOException e) {
                        throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                    }
                }
            });
        }
    }
}
