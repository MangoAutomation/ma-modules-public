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
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
public class StreamedVORqlQueryWithTotal<T extends AbstractVO, TABLE extends AbstractTableDefinition, DAO extends AbstractDao<T, TABLE>, SERVICE extends AbstractVOService<T, TABLE, DAO>> extends StreamedBasicVORqlQueryWithTotal<T, TABLE, DAO, SERVICE> {

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions) {
        this(service, conditions, item -> true, Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, boolean loadRelational) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Function<T, ?> toModel) {
        this(service, conditions, item -> true, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql,  Map<String, Function<Object, Object>> valueConverterMap) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> true, Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, PermissionHolder holder) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> true, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), filter, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), filter, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Field<?>> fieldMap, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, fieldMap, valueConverterMap), filter, toModel);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter, Function<T, ?> toModel) {
        super(service, conditions, filter, toModel);
    }
}
