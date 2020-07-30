/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.SerialDataSourceModel;
import com.infiniteautomation.serial.SerialDataSourceDefinition;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class SerialDataSourceModelMapping implements RestModelJacksonMapping<SerialDataSourceVO, SerialDataSourceModel> {

    @Override
    public Class<? extends SerialDataSourceVO> fromClass() {
        return SerialDataSourceVO.class;
    }

    @Override
    public Class<? extends SerialDataSourceModel> toClass() {
        return SerialDataSourceModel.class;
    }

    @Override
    public SerialDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new SerialDataSourceModel((SerialDataSourceVO)from);
    }
    @Override
    public String getTypeName() {
        return SerialDataSourceDefinition.DATA_SOURCE_TYPE;
    }
}
