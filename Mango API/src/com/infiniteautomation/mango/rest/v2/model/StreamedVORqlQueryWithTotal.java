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
import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
public class StreamedVORqlQueryWithTotal<T extends AbstractVO<T>, DAO extends AbstractDao<T>, SERVICE extends AbstractVOService<T, DAO>> implements StreamedArrayWithTotal {
    private final SERVICE service;
    private final ConditionSortLimit conditions;
    private final Function<T, ?> toModel;
    private final Predicate<T> filter;
    private final boolean loadRelational;
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, boolean loadRelational) {
        this(service, conditions, item -> true, Function.identity(), loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, boolean loadRelational) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), Function.identity(), loadRelational);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Function<T, ?> toModel, boolean loadRelational) {
        this(service, conditions, item -> true, toModel, loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, PermissionHolder holder, Function<T, ?> toModel, boolean loadRelational) {
        this(service, conditions, item -> service.hasReadPermission(holder, item), toModel, loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, boolean loadRelational) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, Function.identity(), loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder, boolean loadRelational) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), Function.identity(), loadRelational);
    }

    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, Function<T, ?> toModel, boolean loadRelational) {
        this(service, service.getDao().rqlToCondition(rql), item -> true, toModel, loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ASTNode rql, PermissionHolder holder, Function<T, ?> toModel, boolean loadRelational) {
        this(service, service.getDao().rqlToCondition(rql), item -> service.hasReadPermission(holder, item), toModel, loadRelational);
    }
    
    public StreamedVORqlQueryWithTotal(SERVICE service, ConditionSortLimit conditions, Predicate<T> filter, Function<T, ?> toModel, boolean loadRelational) {
        this.service = service;
        this.conditions = conditions;
        this.toModel = toModel;
        this.filter = filter;
        this.loadRelational = loadRelational;
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
            if(loadRelational)
                service.customizedQueryFull(conditions, (T item, int index) -> {
                    if (filter.test(item)) {
                        try {
                            jgen.writeObject(toModel.apply(item));
                        } catch (IOException e) {
                            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                        }
                    }
                });
            else
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
