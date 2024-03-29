/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.jazdw.rql.parser.ASTNode;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.http.HttpStatus;

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.db.query.RQLSubSelectCondition;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.spring.service.AbstractBasicVOService;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.vo.AbstractBasicVO;

/**
 *
 * @author Terry Packer
 */
public class StreamedSeroJsonBasicVORqlQuery <T extends AbstractBasicVO, R extends Record, TABLE extends Table<R>, DAO extends AbstractBasicDao<T, R, TABLE>, SERVICE extends AbstractBasicVOService<T, DAO>> implements JsonStreamedArray {

    protected final SERVICE service;
    protected final ConditionSortLimit conditions;
    protected final Predicate<T> filter;

    //For use when we have a filter as we cannot accurately do a count query
    protected int count;
    protected int offsetCount;

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     */
    public StreamedSeroJsonBasicVORqlQuery(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), filter);
    }

    /**
     * Variant to use if the permissions can be enforced via the RQL/Database query
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     */
    public StreamedSeroJsonBasicVORqlQuery(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), null);
    }

    /**
     * Variant to use if permissions can be enforced via the RQL/Database query
     */
    public StreamedSeroJsonBasicVORqlQuery(SERVICE service, ConditionSortLimit conditions) {
        this(service, conditions, null);
    }

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     */
    public StreamedSeroJsonBasicVORqlQuery(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter) {
        this.service = service;
        this.conditions = conditions;
        this.filter = filter;
    }

    @Override
    public void writeArrayValues(JsonWriter writer) throws IOException {
        if(filter != null) {
            //Using memory filter
            Integer offset = conditions.getOffset();
            Integer limit = conditions.getLimit();
            service.customizedQuery(conditions.withNullLimitOffset(), (T item) -> {
                if (filter.test(item)) {
                    if ((offset == null || count >= offset) && (limit == null || offsetCount < limit)) {
                        try {
                            if (count > 0)
                                writer.append(',');
                            writer.indent();
                            writer.writeObject(item);
                        } catch (IOException | JsonException e) {
                            //TODO This can mangle the response, perhaps handle in exception handler to reset stream
                            //  also a nice way to cancel this query would be good as it will just keep throwing
                            // the exception if we don't cancel it.
                            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                        }
                        offsetCount++;
                    }
                    count++;
                }
            });
        }else {
            //No filter just run query
            service.customizedQuery(conditions, (T item) -> {
                try {
                    if (count > 0)
                        writer.append(',');
                    writer.indent();
                    writer.writeObject(item);
                    count++;
                } catch (IOException | JsonException e) {
                    //TODO This can mangle the response, perhaps handle in exception handler to reset stream
                    //  also a nice way to cancel this query would be good as it will just keep throwing
                    // the exception if we don't cancel it.
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                }
            });
        }
    }
}
