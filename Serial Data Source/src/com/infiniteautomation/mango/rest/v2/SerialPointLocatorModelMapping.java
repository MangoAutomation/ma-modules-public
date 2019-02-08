/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.serial.vo.SerialPointLocatorModel;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.vo.User;


/**
 * This class is used for the v2 Model Mapper but replicates the 
 * functionality of the v1 mapper exactly
 * @author Terry Packer
 *
 */
@Component
public class SerialPointLocatorModelMapping implements RestModelJacksonMapping<SerialPointLocatorVO, SerialPointLocatorModel> {

    @Override
    public Class<? extends SerialPointLocatorVO> fromClass() {
        return SerialPointLocatorVO.class;
    }

    @Override
    public Class<? extends SerialPointLocatorModel> toClass() {
        return SerialPointLocatorModel.class;
    }

    @Override
    public SerialPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new SerialPointLocatorModel((SerialPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return SerialPointLocatorModel.TYPE_NAME;
    }
}
