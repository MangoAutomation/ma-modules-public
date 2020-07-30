/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Helper to load data point and its tags into model always
 *
 * @author Terry Packer
 *
 */
public abstract class AbstractPointEventDetectorModelMapping<T extends AbstractPointEventDetectorVO, M extends AbstractPointEventDetectorModel<T>> implements RestModelJacksonMapping<T, M> {


    protected M loadDataPoint(T detector, M model, PermissionHolder user, RestModelMapper mapper) {
        DataPointVO dp = DataPointDao.getInstance().get(detector.getSourceId());
        if(dp != null) {
            dp.setTags(DataPointTagsDao.getInstance().getTagsForDataPointId(dp.getId()));
            model.setDataPoint(mapper.map(dp, DataPointModel.class, user));
        }
        return model;
    }

}
