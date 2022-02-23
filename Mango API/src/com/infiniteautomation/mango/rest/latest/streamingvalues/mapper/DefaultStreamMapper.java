/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.ValueModel;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Maps {@link IdPointValueTime} to a {@link StreamingPointValueTimeModel}.
 *
 * @author Jared Wiltshire
 */
@NonNull
public class DefaultStreamMapper extends AbstractStreamMapper<IdPointValueTime> {

    public DefaultStreamMapper(StreamMapperBuilder options) {
        super(options);
    }

    @Override
    public StreamingPointValueTimeModel apply(IdPointValueTime v) {
        DataPointVO point = lookupPoint(v.getSeriesId());
        StreamingPointValueTimeModel model = new StreamingPointValueTimeModel(point.getXid(), v.getTime());

        model.setValue(getValue(point, v.getValue()));

        for (PointValueField field : fields) {
            switch (field) {
                case TIMESTAMP:
                    model.setTimestamp(formatTime(v.getTime()));
                    break;
                case ANNOTATION:
                    if (v instanceof IAnnotated) {
                        model.setAnnotation(((IAnnotated) v).getSourceMessage());
                    }
                    break;
                case CACHED:
                    model.setCached(v.isFromCache());
                    break;
                case BOOKEND:
                    model.setBookend(v.isBookend());
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
                case VALUE:
                case RENDERED:
                case RAW:
                    // handled above by getValue()
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported field: " + field);
            }
        }
        return model;
    }

    private ValueModel getValue(DataPointVO point, DataValue rawValue) {
        ValueModel model = new ValueModel();
        if (fields.contains(PointValueField.VALUE)) {
            Object convertedValue = extractValue(point, rawValue);
            model.setValue(convertedValue);
        }
        if (fields.contains(PointValueField.RAW)) {
            model.setRaw(rawValue == null ? null : rawValue.getObjectValue());
        }
        if (fields.contains(PointValueField.RENDERED)) {
            if (rawValue != null) {
                // the text renderer converts numeric values to the appropriate unit before rendering
                model.setRendered(point.getTextRenderer().getText(rawValue, TextRenderer.HINT_FULL));
            } else {
                model.setRendered(RENDERED_NULL_STRING);
            }
        }
        return model;
    }
}
