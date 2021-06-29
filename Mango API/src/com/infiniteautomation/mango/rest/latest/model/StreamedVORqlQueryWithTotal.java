/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.db.query.RQLSubSelectCondition;
import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.serotonin.m2m2.db.dao.AbstractVoDao;
import com.serotonin.m2m2.vo.AbstractVO;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
public class StreamedVORqlQueryWithTotal<T extends AbstractVO, R extends Record, TABLE extends Table<R>, DAO extends AbstractVoDao<T, R, TABLE>, SERVICE extends AbstractVOService<T, DAO>> extends StreamedBasicVORqlQueryWithTotal<T, R, TABLE, DAO, SERVICE> {

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param service
     * @param rql
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     * @param filter
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), filter, toModel);
    }

    /**
     * Variant to use if the permissions can be enforced via the RQL/Database query
     * @param service
     * @param rql
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Function<T, ?> toModel) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), null, toModel);
    }

    /**
     * Variant to use if permissions can be enforced via the RQL/Database query
     * @param service
     * @param conditions
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Function<T, ?> toModel) {
        this(service, conditions, null, toModel);
    }

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param service
     * @param conditions
     * @param filter
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter, Function<T, ?> toModel) {
        super(service, conditions, filter, toModel);
    }
}
