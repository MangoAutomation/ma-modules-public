/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.spring.db.AbstractBasicTableDefinition;
import com.infiniteautomation.mango.spring.service.AbstractBasicVOService;
import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.vo.AbstractBasicVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
public class StreamedBasicVORqlQueryWithTotal<T extends AbstractBasicVO, TABLE extends AbstractBasicTableDefinition, DAO extends AbstractBasicDao<T, TABLE>, SERVICE extends AbstractBasicVOService<T, TABLE, DAO>> implements StreamedArrayWithTotal {

    protected final SERVICE service;
    protected final ConditionSortLimit conditions;
    protected final Function<T, ?> toModel;
    protected final Predicate<T> filter;

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions) {
        this(service, conditions, item -> true, Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, boolean loadRelational) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Function<T, ?> toModel) {
        this(service, conditions, item -> true, toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql,  Map<String, Function<Object, Object>> valueConverterMap) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> true, Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, PermissionHolder holder) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> service.hasReadPermission(holder, item), Function.identity());
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> true, toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, PermissionHolder holder, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), item -> service.hasReadPermission(holder, item), toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql), filter, toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ASTNode rql, Map<String, Function<Object, Object>> valueConverterMap, Predicate<T> filter, Function<T, ?> toModel) {
        this(service, service.getDao().rqlToCondition(rql, valueConverterMap), filter, toModel);
    }

    public StreamedBasicVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter, Function<T, ?> toModel) {
        this.service = service;
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
        return service.customizedCount(conditions);
    }

    private class StreamedVOArray implements JSONStreamedArray {
        @Override
        public void writeArrayValues(JsonGenerator jgen) throws IOException {
            service.customizedQuery(conditions, (T item, int index) -> {
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
