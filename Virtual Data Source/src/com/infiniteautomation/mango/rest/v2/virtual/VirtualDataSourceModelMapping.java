/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.virtual;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.VirtualDataSourceModel;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class VirtualDataSourceModelMapping implements RestModelMapping<VirtualDataSourceVO, VirtualDataSourceModel> {

    @Override
    public Class<? extends VirtualDataSourceVO> fromClass() {
        return VirtualDataSourceVO.class;
    }

    @Override
    public Class<? extends VirtualDataSourceModel> toClass() {
        return VirtualDataSourceModel.class;
    }

    @Override
    public VirtualDataSourceModel map(Object from, User user) {
        return new VirtualDataSourceModel((VirtualDataSourceVO)from);
    }

}
