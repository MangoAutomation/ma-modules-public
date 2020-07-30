/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.MBusDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.mbus.MBusDataSourceDefinition;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MBusDataSourceModelMapping implements RestModelJacksonMapping<MBusDataSourceVO, MBusDataSourceModel> {

    @Override
    public Class<? extends MBusDataSourceVO> fromClass() {
        return MBusDataSourceVO.class;
    }

    @Override
    public Class<? extends MBusDataSourceModel> toClass() {
        return MBusDataSourceModel.class;
    }

    @Override
    public MBusDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MBusDataSourceModel((MBusDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
