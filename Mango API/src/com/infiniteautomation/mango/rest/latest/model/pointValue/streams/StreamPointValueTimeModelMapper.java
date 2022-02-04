/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Functions;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Jared Wiltshire
 */
@NonNull
public class StreamPointValueTimeModelMapper implements Function<IdPointValueTime, StreamPointValueTimeModel> {

    ZonedDateTime from;
    ZonedDateTime to;
    Map<Integer, DataPointVO> dataPoints = Collections.emptyMap();
    EnumSet<PointValueField> fieldSet = EnumSet.of(PointValueField.TIMESTAMP, PointValueField.VALUE);
    DateTimeFormatter dateTimeFormatter = null;
    ZoneId zoneId;

    public StreamPointValueTimeModelMapper withFrom(@Nullable ZonedDateTime from) {
        this.from = from;
        return this;
    }

    public StreamPointValueTimeModelMapper withTo(@Nullable ZonedDateTime to) {
        this.to = to;
        return this;
    }

    public StreamPointValueTimeModelMapper withFields(@Nullable PointValueField[] fields) {
        if (fields != null && fields.length > 0) {
            this.fieldSet = EnumSet.copyOf(Arrays.asList(fields));
        }
        return this;
    }

    public StreamPointValueTimeModelMapper withDateTimeFormat(@Nullable String dateTimeFormat) {
        if (dateTimeFormat != null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        }
        return this;
    }

    public StreamPointValueTimeModelMapper withTimezone(@Nullable String timezone) {
        if (timezone != null) {
            this.zoneId = ZoneId.of(timezone);
        }
        return this;
    }

    public StreamPointValueTimeModelMapper withDataPoint(DataPointVO dataPoint) {
        this.dataPoints = Map.of(dataPoint.getSeriesId(), dataPoint);
        return this;
    }

    public StreamPointValueTimeModelMapper withDataPoints(Collection<? extends DataPointVO> dataPoints) {
        this.dataPoints = dataPoints.stream()
                .collect(Collectors.toUnmodifiableMap(DataPointVO::getSeriesId, Functions.identity()));
        return this;
    }

    private ZoneId zoneId() {
        ZoneId zoneId = this.zoneId;
        if (zoneId == null) {
            if (from != null) {
                zoneId = from.getZone();
            } else if (to != null) {
                zoneId = to.getZone();
            } else {
                zoneId = ZoneId.systemDefault();
            }
        }
        return zoneId;
    }

    private Object formatTime(long timestamp) {
        if (dateTimeFormatter != null) {
            return dateTimeFormatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId()));
        }
        return timestamp;
    }

    @Override
    public StreamPointValueTimeModel apply(IdPointValueTime v) {
        DataPointVO point = Objects.requireNonNull(dataPoints.get(v.getSeriesId()));
        StreamPointValueTimeModel model = new StreamPointValueTimeModel(point, v);
        for (PointValueField field : fieldSet) {
            switch (field) {
                case VALUE: {
                    DataValue value = v.getValue();
                    if (point.getPointLocator().getDataType() == DataType.NUMERIC && point.getRenderedUnit() != Unit.ONE) {
                        UnitConverter converter = point.getUnit().getConverterTo(point.getRenderedUnit());
                        model.setValue(converter.convert(value.getDoubleValue()));
                    } else {
                        model.setValue(value.getObjectValue());
                    }
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
                        model.setRendered(point.getTextRenderer().getText(v.getValue(), TextRenderer.HINT_FULL));
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
                    throw new IllegalStateException("Unknown field: " + field);
            }
        }
        return model;
    }
}
