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
 *
 * @author Terry Packer
 */
public class StreamedVORqlQuery<T extends AbstractVO, R extends Record, TABLE extends Table<R>, DAO extends AbstractVoDao<T, R, TABLE>, SERVICE extends AbstractVOService<T, DAO>> extends StreamedBasicVORqlQuery<T, R, TABLE, DAO, SERVICE> {

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     */
    public StreamedVORqlQuery(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), filter);
    }

    /**
     * Variant to use if the permissions can be enforced via the RQL/Database query
     * @param subSelectMap - can be null
     * @param fieldMap - can be null
     * @param valueConverterMap - can be null
     */
    public StreamedVORqlQuery(SERVICE service, ASTNode rql, Map<String, RQLSubSelectCondition> subSelectMap, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap) {
        this(service, service.rqlToCondition(rql, subSelectMap, fieldMap, valueConverterMap), null);
    }

    /**
     * Variant to use if permissions can be enforced via the RQL/Database query
     */
    public StreamedVORqlQuery(SERVICE service, ConditionSortLimit conditions) {
        this(service, conditions, null);
    }

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     */
    public StreamedVORqlQuery(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter) {
        super(service, conditions, filter);
    }

}
