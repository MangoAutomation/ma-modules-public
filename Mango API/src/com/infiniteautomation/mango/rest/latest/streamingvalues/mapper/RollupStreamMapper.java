/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Maps {@link AnalogStatistics} to a {@link StreamingPointValueTimeModel}, for use with rollups.
 *
 * @author Jared Wiltshire
 */
@NonNull
public class RollupStreamMapper extends AbstractStreamMapper<AnalogStatistics, StreamingPointValueTimeModel> {

    public RollupStreamMapper(StreamMapperBuilder options) {
        super(options);
    }

    @Override
    public StreamingPointValueTimeModel apply(AnalogStatistics stats) {
        DataPointVO point = lookupPoint(stats.getSeriesId());
        RollupEnum rollup = rollup(point);
        StreamingPointValueTimeModel model = new StreamingPointValueTimeModel(point.getXid(), stats.getPeriodStartTime());
        for (PointValueField field : fields()) {
            switch (field) {
                case VALUE: {
                    double convertedValue = convertValue(point, getRollupValue(stats, rollup));
                    model.setValue(convertedValue);
                    break;
                }
                case TIMESTAMP: {
                    model.setTimestamp(formatTime(stats.getPeriodStartTime()));
                    break;
                }
                case ANNOTATION:
                    break;
                case CACHED:
                    model.setCached(false);
                    break;
                case BOOKEND:
                    model.setBookend(false);
                    break;
                case RENDERED: {
                    double convertedValue = convertValue(point, getRollupValue(stats, rollup));
                    model.setRendered(point.getTextRenderer().getText(convertedValue, TextRenderer.HINT_FULL));
                    break;
                }
                case RAW:
                    model.setRaw(getRollupValue(stats, rollup));
                    break;
                case XID:
                    model.setXid(point.getXid());
                    break;
                case NAME:
                    model.setName(point.getName());
                    break;
                case DEVICE_NAME:
                    model.setDeviceName(point.getDeviceName());
                    break;
                case DATA_SOURCE_NAME:
                    model.setDataSourceName(point.getDataSourceName());
                    break;
                default:
                    throw new IllegalStateException("Unknown field: " + field);
            }
        }
        return model;
    }

}
