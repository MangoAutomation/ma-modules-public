/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vmstat.VMStatPointLocatorModel;
import com.serotonin.m2m2.vmstat.VMStatPointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelJacksonMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;

/**
 * @author Terry Packer
 *
 */
@Component
public class VMStatPointLocatorModelMapping implements RestModelJacksonMapping<VMStatPointLocatorVO, VMStatPointLocatorModel> {

    @Override
    public Class<? extends VMStatPointLocatorVO> fromClass() {
        return VMStatPointLocatorVO.class;
    }

    @Override
    public Class<? extends VMStatPointLocatorModel> toClass() {
        return VMStatPointLocatorModel.class;
    }

    @Override
    public VMStatPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new VMStatPointLocatorModel((VMStatPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return VMStatPointLocatorModel.TYPE_NAME;
    }

}
