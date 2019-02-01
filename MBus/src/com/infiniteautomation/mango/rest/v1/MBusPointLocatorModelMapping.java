/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.mbus.MBusPointLocatorModel;
import com.serotonin.m2m2.mbus.MBusPointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class MBusPointLocatorModelMapping implements RestModelMapping<MBusPointLocatorVO, MBusPointLocatorModel> {

    @Override
    public Class<? extends MBusPointLocatorVO> fromClass() {
        return MBusPointLocatorVO.class;
    }

    @Override
    public Class<? extends MBusPointLocatorModel> toClass() {
        return MBusPointLocatorModel.class;
    }

    @Override
    public MBusPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new MBusPointLocatorModel((MBusPointLocatorVO)from);
    }

}
