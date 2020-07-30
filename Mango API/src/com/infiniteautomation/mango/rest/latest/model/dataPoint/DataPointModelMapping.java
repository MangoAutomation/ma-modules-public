/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer.BaseTextRendererModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataPointModelMapping implements RestModelMapping<DataPointVO, DataPointModel>{

    @Override
    public Class<? extends DataPointVO> fromClass() {
        return DataPointVO.class;
    }

    @Override
    public Class<? extends DataPointModel> toClass() {
        return DataPointModel.class;
    }

    @Override
    public DataPointModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        DataPointVO vo = (DataPointVO)from;

        //First get the point locator
        AbstractPointLocatorModel<?> pointLocatorModel = mapper.map(vo.getPointLocator(), AbstractPointLocatorModel.class, user);
        DataPointModel model = new DataPointModel(vo);
        model.setPointLocator(pointLocatorModel);
        BaseTextRendererModel<?> textRenderer = mapper.map(vo.getTextRenderer(), BaseTextRendererModel.class, user);
        model.setTextRenderer(textRenderer);

        return model;
    }

    @Override
    public DataPointVO unmap(Object from, PermissionHolder user, RestModelMapper mapper)
            throws ValidationException {
        // TODO Auto-generated method stub
        return RestModelMapping.super.unmap(from, user, mapper);
    }

    @Override
    public DataPointVO unmapInto(Object from, DataPointVO into, PermissionHolder user, RestModelMapper mapper)
            throws ValidationException {
        // TODO Auto-generated method stub
        return RestModelMapping.super.unmap(from, user, mapper);
    }


}
