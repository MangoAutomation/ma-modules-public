/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.mango.rest.v2.model.AsciiFileDataSourceModel;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class AsciiFileDataSourceModelMapping implements RestModelMapping<AsciiFileDataSourceVO, AsciiFileDataSourceModel> {

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

}
