/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.EnvCanDataSourceModel;
import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.envcan.EnvCanDataSourceDefinition;
import com.serotonin.m2m2.envcan.EnvCanDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class EnvCanDataSourceModelMapping implements RestModelJacksonMapping<EnvCanDataSourceVO, EnvCanDataSourceModel> {

    @Override
    public Class<? extends EnvCanDataSourceVO> fromClass() {
        return EnvCanDataSourceVO.class;
    }

    @Override
    public Class<? extends EnvCanDataSourceModel> toClass() {
        return EnvCanDataSourceModel.class;
    }

    @Override
    public EnvCanDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new EnvCanDataSourceModel((EnvCanDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return EnvCanDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
