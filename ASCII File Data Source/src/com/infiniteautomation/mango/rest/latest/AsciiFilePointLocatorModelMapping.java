/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.infiniteautomation.mango.rest.latest.model.AsciiFilePointLocatorModel;
import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.vo.permission.PermissionHolder;


/**
 * This class is used for the v2 Model Mapper but replicates the
 * functionality of the v1 mapper exactly
 * @author Terry Packer
 *
 */
@Component
public class AsciiFilePointLocatorModelMapping implements RestModelJacksonMapping<AsciiFilePointLocatorVO, AsciiFilePointLocatorModel> {

    @Override
    public Class<? extends AsciiFilePointLocatorVO> fromClass() {
        return AsciiFilePointLocatorVO.class;
    }

    @Override
    public Class<? extends AsciiFilePointLocatorModel> toClass() {
        return AsciiFilePointLocatorModel.class;
    }

    @Override
    public AsciiFilePointLocatorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AsciiFilePointLocatorModel((AsciiFilePointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return AsciiFilePointLocatorModel.TYPE_NAME;
    }
}
