/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

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
 *
 * @author Terry Packer
 */
public class StreamedSeroJsonVORqlQuery<T extends AbstractVO, TABLE extends AbstractTableDefinition, DAO extends AbstractVoDao<T, TABLE>, SERVICE extends AbstractVOService<T, TABLE, DAO>> extends StreamedSeroJsonBasicVORqlQuery<T, TABLE, DAO, SERVICE> {

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param service
     * @param rql
     * @param fieldMap
     * @param valueConverterMap
     * @param filter
     */
    public StreamedSeroJsonVORqlQuery(SERVICE service, ASTNode rql, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter) {
        this(service, service.rqlToCondition(rql, fieldMap, valueConverterMap), filter);
    }

    /**
     * Variant to use if the permissions can be enforced via the RQL/Database query
     * @param service
     * @param rql
     * @param fieldMap
     * @param valueConverterMap
     */
    public StreamedSeroJsonVORqlQuery(SERVICE service, ASTNode rql, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap) {
        this(service, service.rqlToCondition(rql, fieldMap, valueConverterMap), null);
    }

    /**
     * Variant to use if permissions can be enforced via the RQL/Database query
     * @param service
     * @param conditions
     */
    public StreamedSeroJsonVORqlQuery(SERVICE service, ConditionSortLimit conditions) {
        this(service, conditions, null);
    }

    /**
     * Use if permissions cannot be enforced in the RQL/Database query, this will perform a full query and count the results while respecting the limit.
     * @param service
     * @param conditions
     * @param filter
     */
    public StreamedSeroJsonVORqlQuery(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter) {
        super(service, conditions, filter);
    }

}
