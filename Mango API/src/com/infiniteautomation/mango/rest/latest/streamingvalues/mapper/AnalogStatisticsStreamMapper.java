/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Maps {@link AnalogStatistics} to a {@link IdPointValueTime}, for use with rollups.
 *
 * @author Jared Wiltshire
 */
@NonNull
public class AnalogStatisticsStreamMapper extends AbstractStreamMapper<AnalogStatistics, IdPointValueTime> {

    public AnalogStatisticsStreamMapper(StreamMapperBuilder options) {
        super(options);
    }

    @Override
    public IdPointValueTime apply(AnalogStatistics stats) {
        DataPointVO point = lookupPoint(stats.getSeriesId());
        RollupEnum rollup = rollup(point);
        double convertedValue = convertValue(point, getRollupValue(stats, rollup));
        return new IdPointValueTime(stats.getSeriesId(), new NumericValue(convertedValue), stats.getPeriodStartTime());
    }

}
