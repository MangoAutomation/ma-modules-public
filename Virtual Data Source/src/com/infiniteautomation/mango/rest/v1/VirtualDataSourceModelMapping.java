/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.virtual.vo.VirtualDataSourceModel;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

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
    public VirtualDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new VirtualDataSourceModel((VirtualDataSourceVO)from);
    }

}
