/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.mbus.MBusPointLocatorModel;
import com.serotonin.m2m2.mbus.MBusPointLocatorVO;
import com.serotonin.m2m2.vo.User;


/**
 * This class is used for the v2 Model Mapper but replicates the 
 * functionality of the v1 mapper exactly
 * @author Terry Packer
 *
 */
@Component
public class MBusPointLocatorModelMapping implements RestModelJacksonMapping<MBusPointLocatorVO, MBusPointLocatorModel> {

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

    @Override
    public String getTypeName() {
        return MBusPointLocatorModel.TYPE_NAME;
    }

}
