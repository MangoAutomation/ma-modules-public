/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.mbus.MBusDataSourceDefinition;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;
import com.serotonin.m2m2.mbus.rest.MBusDataSourceModel;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelJacksonMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;

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
