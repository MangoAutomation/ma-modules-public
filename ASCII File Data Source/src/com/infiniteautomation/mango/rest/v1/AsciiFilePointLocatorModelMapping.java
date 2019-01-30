/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorModel;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class AsciiFilePointLocatorModelMapping implements RestModelMapping<AsciiFilePointLocatorVO, AsciiFilePointLocatorModel> {

    @Override
    public Class<? extends AsciiFilePointLocatorVO> fromClass() {
        return AsciiFilePointLocatorVO.class;
    }

    @Override
    public Class<? extends AsciiFilePointLocatorModel> toClass() {
        return AsciiFilePointLocatorModel.class;
    }

    @Override
    public AsciiFilePointLocatorModel map(Object from, User user, RestModelMapper mapper) {
        return new AsciiFilePointLocatorModel((AsciiFilePointLocatorVO)from);
    }

}
