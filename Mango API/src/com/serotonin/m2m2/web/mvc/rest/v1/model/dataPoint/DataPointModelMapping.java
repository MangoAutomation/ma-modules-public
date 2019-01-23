/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataPointModelMapping implements RestModelMapping<DataPointVO, DataPointModel> {

    @Override
    public Class<? extends DataPointVO> fromClass() {
        return DataPointVO.class;
    }

    @Override
    public Class<? extends DataPointModel> toClass() {
        return DataPointModel.class;
    }

    @Override
    public DataPointModel map(Object from, User user, RestModelMapper mapper) {
        DataPointVO vo = (DataPointVO)from;
        DataPointModel model = new DataPointModel(vo);
        PointLocatorModel<?> pointLocatorModel = mapper.map(vo.getPointLocator(), PointLocatorModel.class, user);
        model.setPointLocator(pointLocatorModel);
        return model;
    }

}
