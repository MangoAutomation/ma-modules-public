/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.VirtualDataSourceModel;
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class VirtualDataSourceModelMapping implements RestModelJacksonMapping<VirtualDataSourceVO, VirtualDataSourceModel> {

    @Override
    public Class<? extends VirtualDataSourceVO> fromClass() {
        return VirtualDataSourceVO.class;
    }

    @Override
    public Class<? extends VirtualDataSourceModel> toClass() {
        return VirtualDataSourceModel.class;
    }

    @Override
    public VirtualDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new VirtualDataSourceModel((VirtualDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return VirtualDataSourceDefinition.TYPE_NAME;
    }

}
