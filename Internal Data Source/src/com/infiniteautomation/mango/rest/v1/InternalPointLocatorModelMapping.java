/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.internal.InternalPointLocatorModel;
import com.serotonin.m2m2.internal.InternalPointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class InternalPointLocatorModelMapping implements RestModelMapping<InternalPointLocatorVO, InternalPointLocatorModel> {

    @Override
    public Class<? extends InternalPointLocatorVO> fromClass() {
        return InternalPointLocatorVO.class;
    }

    @Override
    public Class<? extends InternalPointLocatorModel> toClass() {
        return InternalPointLocatorModel.class;
    }

    @Override
    public InternalPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new InternalPointLocatorModel((InternalPointLocatorVO)from);
    }

}
