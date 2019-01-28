/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.SerialDataSourceModel;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class SerialDataSourceModelMapping implements RestModelMapping<SerialDataSourceVO, SerialDataSourceModel> {

    @Override
    public Class<? extends SerialDataSourceVO> fromClass() {
        return SerialDataSourceVO.class;
    }

    @Override
    public Class<? extends SerialDataSourceModel> toClass() {
        return SerialDataSourceModel.class;
    }

    @Override
    public SerialDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new SerialDataSourceModel((SerialDataSourceVO)from);
    }

}
