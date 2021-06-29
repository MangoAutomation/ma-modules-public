/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * This test is designed to set a different series Id on a point than it's default of being the point id
 *  should pickup any bugs where DataPointVO.id is used instead of DataPointVO.seriesId
 *
 * @author Terry Packer
 */
public class MultiPointSeriesIdStatisticsStreamTest extends MultiPointStatisticsStreamTest {

    @Override
    protected DataPointVO createDataPoint(int dataSourceId, int dataType, int defaultCacheSize) {
        DataPointVO vo = super.createDataPoint(dataSourceId, dataType, defaultCacheSize);

        //Change series id
        int seriesId = DataPointDao.getInstance().insertNewTimeSeries();
        while(vo.getId() == seriesId) {
            seriesId = DataPointDao.getInstance().insertNewTimeSeries();
        }
        vo.setSeriesId(seriesId);

        DataPointDao.getInstance().update(vo.getId(), vo);
        return vo;
    }

}
