/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.ValueTimeModel;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
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
    public StreamingPointValueTimeModel apply(IdPointValueTime valueTime) {
        DataPointVO point = lookupPoint(valueTime.getSeriesId());
        StreamingPointValueTimeModel model = new StreamingPointValueTimeModel(point.getXid(), valueTime.getTime());

        model.setValue(getValue(point, valueTime));
        if (fields.contains(PointValueField.CACHED)) {
            model.setCached(valueTime.isFromCache());
        }
        if (fields.contains(PointValueField.BOOKEND)) {
            model.setBookend(valueTime.isBookend());
        }
        if (fields.contains(PointValueField.ANNOTATION)) {
            if (valueTime instanceof IAnnotated) {
                model.setAnnotation(((IAnnotated) valueTime).getSourceMessage());
            }
        }
        return copyPointPropertiesToModel(point, model);
    }

    private ValueTimeModel getValue(DataPointVO point, PointValueTime valueTime) {
        DataValue rawValue = valueTime.getValue();

        ValueTimeModel model = new ValueTimeModel();
        if (fields.contains(PointValueField.TIMESTAMP)) {
            model.setTimestamp(formatTime(valueTime.getTime()));
        }
        if (fields.contains(PointValueField.VALUE)) {
            Object convertedValue = extractValue(point, rawValue);
            model.setValue(convertedValue);
        }
        if (fields.contains(PointValueField.RAW)) {
            model.setRaw(rawValue);
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
