/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.infiniteautomation.serial.vo.SerialPointLocatorModel;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class SerialPointLocatorModelMapping implements RestModelMapping<SerialPointLocatorVO, SerialPointLocatorModel> {

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

}
