/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.envcan.EnvCanPointLocatorModel;
import com.serotonin.m2m2.envcan.EnvCanPointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelJacksonMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;

/**
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
