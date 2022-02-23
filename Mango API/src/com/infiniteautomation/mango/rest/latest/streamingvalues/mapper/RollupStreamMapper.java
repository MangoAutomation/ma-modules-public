/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.pointvalue.AggregateValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.SeriesValueTime;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Maps {@link SeriesValueTime} containing a {@link AggregateValue} to a {@link StreamingPointValueTimeModel}, for use with rollups.
 *
 * @author Jared Wiltshire
 */
@NonNull
public class RollupStreamMapper extends AbstractStreamMapper<SeriesValueTime<? extends AggregateValue>> {

    public RollupStreamMapper(StreamMapperBuilder options) {
        super(options);
    }

    @Override
    public StreamingPointValueTimeModel apply(SeriesValueTime<? extends AggregateValue> value) {
        DataPointVO point = lookupPoint(value.getSeriesId());
        RollupEnum rollup = rollup(point);
        AggregateValue aggregate = value.getValue();

        StreamingPointValueTimeModel model = new StreamingPointValueTimeModel(point.getXid(), value.getTime());
        for (PointValueField field : fields()) {
            switch (field) {
                case VALUE: {
                    Object rollupValue = getRollupValue(aggregate, rollup);
                    model.setValue(extractValue(point, rollupValue));
                    break;
                }
                case TIMESTAMP: {
                    model.setTimestamp(formatTime(value.getTime()));
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
                    Object rollupValue = getRollupValue(aggregate, rollup);
                    if (rollupValue instanceof DataValue) {
                        // the text renderer converts numeric values to the appropriate unit before rendering
                        model.setRendered(point.getTextRenderer().getText((DataValue) rollupValue, TextRenderer.HINT_FULL));
                    } else if (point.getPointLocator().getDataType() == DataType.NUMERIC && rollupValue instanceof Double) {
                        // the text renderer converts numeric values to the appropriate unit before rendering
                        model.setRendered(point.getTextRenderer().getText((double) rollupValue, TextRenderer.HINT_FULL));
                    } else if (rollupValue == null) {
                        model.setRendered("-");
                    } else {
                        model.setRendered(rollupValue.toString());
                    }
                    break;
                }
                case RAW:
                    model.setRaw(getRollupValue(aggregate, rollup));
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
