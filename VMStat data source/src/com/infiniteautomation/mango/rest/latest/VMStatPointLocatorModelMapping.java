/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.VMStatPointLocatorModel;
import com.serotonin.m2m2.vmstat.VMStatPointLocatorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;


/**
 * This class is used for the v2 Model Mapper but replicates the
 * functionality of the v1 mapper exactly
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
    public VMStatPointLocatorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new VMStatPointLocatorModel((VMStatPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return VMStatPointLocatorModel.TYPE_NAME;
    }
}
