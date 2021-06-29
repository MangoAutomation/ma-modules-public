/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.mango.rest.latest.model.AsciiFileDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AsciiFileDataSourceModelMapping implements RestModelJacksonMapping<AsciiFileDataSourceVO, AsciiFileDataSourceModel> {

    @Override
    public Class<? extends AsciiFileDataSourceVO> fromClass() {
        return AsciiFileDataSourceVO.class;
    }

    @Override
    public Class<? extends AsciiFileDataSourceModel> toClass() {
        return AsciiFileDataSourceModel.class;
    }

    @Override
    public AsciiFileDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AsciiFileDataSourceModel((AsciiFileDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
