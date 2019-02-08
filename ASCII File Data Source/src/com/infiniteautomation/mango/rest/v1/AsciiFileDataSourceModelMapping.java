/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceModel;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelJacksonMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;

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
    public AsciiFileDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new AsciiFileDataSourceModel((AsciiFileDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
