/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.envcan.EnvCanPointLocatorModel;
import com.serotonin.m2m2.envcan.EnvCanPointLocatorVO;
import com.serotonin.m2m2.vo.User;


/**
 * This class is used for the v2 Model Mapper but replicates the 
 * functionality of the v1 mapper exactly
 * @author Terry Packer
 *
 */
@Component
public class EnvCanPointLocatorModelMapping implements RestModelJacksonMapping<EnvCanPointLocatorVO, EnvCanPointLocatorModel> {

    @Override
    public Class<? extends EnvCanPointLocatorVO> fromClass() {
        return EnvCanPointLocatorVO.class;
    }

    @Override
    public Class<? extends EnvCanPointLocatorModel> toClass() {
        return EnvCanPointLocatorModel.class;
    }

    @Override
    public EnvCanPointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new EnvCanPointLocatorModel((EnvCanPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return EnvCanPointLocatorModel.TYPE_NAME;
    }

}
