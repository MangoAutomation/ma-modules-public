/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.MBusDataSourceModel;
import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.mbus.MBusDataSourceDefinition;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;
import com.serotonin.m2m2.vo.User;

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
    public MBusDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new MBusDataSourceModel((MBusDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
