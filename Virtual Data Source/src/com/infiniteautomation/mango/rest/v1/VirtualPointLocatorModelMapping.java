/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.virtual.vo.model.VirtualPointLocatorModel;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class VirtualPointLocatorModelMapping implements RestModelMapping<VirtualPointLocatorVO, VirtualPointLocatorModel> {

    @Override
    public Class<? extends VirtualPointLocatorVO> fromClass() {
        return VirtualPointLocatorVO.class;
    }

    @Override
    public Class<? extends VirtualPointLocatorModel> toClass() {
        return VirtualPointLocatorModel.class;
    }

    @Override
    public VirtualPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new VirtualPointLocatorModel((VirtualPointLocatorVO)from);
    }

}
