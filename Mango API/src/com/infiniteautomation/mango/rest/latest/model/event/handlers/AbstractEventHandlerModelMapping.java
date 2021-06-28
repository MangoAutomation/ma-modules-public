/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

public interface AbstractEventHandlerModelMapping<T extends AbstractEventHandlerVO> extends RestModelJacksonMapping<T, AbstractEventHandlerModel<T>> {

    AbstractEventHandlerModel<T> mapHandler(T o, PermissionHolder user, RestModelMapper mapper);

    @Override
    default AbstractEventHandlerModel<T> map(Object o, PermissionHolder user, RestModelMapper mapper) {
        //noinspection unchecked
        return mapHandler((T) o, user, mapper);
    }

    @Override
    default T unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        //noinspection unchecked
        AbstractEventHandlerModel<T> model = (AbstractEventHandlerModel<T>) from;
        return model.toVO();
    }

    @Override
    default T unmapInto(Object from, T into, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        //noinspection unchecked
        AbstractEventHandlerModel<T> model = (AbstractEventHandlerModel<T>) from;
        model.readInto(into);
        return into;
    }
}
