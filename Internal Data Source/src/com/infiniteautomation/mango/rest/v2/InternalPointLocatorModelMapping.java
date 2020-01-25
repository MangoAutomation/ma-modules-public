/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.InternalPointLocatorModel;
import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.internal.InternalPointLocatorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;


/**
 * This class is used for the v2 Model Mapper but replicates the
 * functionality of the v1 mapper exactly
 * @author Terry Packer
 *
 */
@Component
public class InternalPointLocatorModelMapping implements RestModelJacksonMapping<InternalPointLocatorVO, InternalPointLocatorModel> {

    @Override
    public Class<? extends InternalPointLocatorVO> fromClass() {
        return InternalPointLocatorVO.class;
    }

    @Override
    public Class<? extends InternalPointLocatorModel> toClass() {
        return InternalPointLocatorModel.class;
    }

    @Override
    public InternalPointLocatorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new InternalPointLocatorModel((InternalPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return InternalPointLocatorModel.TYPE_NAME;
    }

}
