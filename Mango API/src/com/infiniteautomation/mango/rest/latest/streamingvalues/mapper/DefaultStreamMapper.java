/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
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
        for (PointValueField field : fields()) {
            switch (field) {
                case VALUE: {
                    model.setValue(extractValue(point, v.getValue()));
                    break;
                }
                case TIMESTAMP: {
                    model.setTimestamp(formatTime(v.getTime()));
                    break;
                }
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
                case RENDERED: {
                    DataValue value = v.getValue();
                    if (value == null) {
                        model.setRendered("-");
                    } else {
                        // the text renderer converts the value to the appropriate unit before rendering
                        model.setRendered(point.getTextRenderer().getText(value, TextRenderer.HINT_FULL));
                    }
                    break;
                }
                case RAW:
                    model.setRaw(v.getValue().getObjectValue());
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
                    throw new IllegalArgumentException("Unsupported field: " + field);
            }
        }
        return model;
    }

}
