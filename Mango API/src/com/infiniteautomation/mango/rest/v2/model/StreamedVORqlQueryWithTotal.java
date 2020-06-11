/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Field;

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.spring.db.AbstractTableDefinition;
import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.serotonin.m2m2.db.dao.AbstractVoDao;
import com.serotonin.m2m2.vo.AbstractVO;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
public class StreamedVORqlQueryWithTotal<T extends AbstractVO, TABLE extends AbstractTableDefinition, DAO extends AbstractVoDao<T, TABLE>, SERVICE extends AbstractVOService<T, TABLE, DAO>> extends StreamedBasicVORqlQueryWithTotal<T, TABLE, DAO, SERVICE> {

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param service
     * @param rql
     * @param fieldMap
     * @param valueConverterMap
     * @param filter
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.rqlToCondition(rql, fieldMap, valueConverterMap), filter, toModel);
    }

    /**
     * Variant to use if the permissions can be enforced via the RQL/Database query
     * @param service
     * @param rql
     * @param fieldMap
     * @param valueConverterMap
     * @param toModel
     */
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Function<T, ?> toModel) {
        this(service, service.rqlToCondition(rql, fieldMap, valueConverterMap), null, toModel);
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
