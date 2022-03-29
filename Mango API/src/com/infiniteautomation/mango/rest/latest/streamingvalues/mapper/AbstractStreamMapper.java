/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Base class for mappers, uses options from the REST API parameters to map a stream of point values.
 *
 * @param <T> input type
 * @author Jared Wiltshire
 */
@NonNull
public abstract class AbstractStreamMapper<T> implements Function<T, StreamingPointValueTimeModel> {

    public static final String MAPPER_ATTRIBUTE = "mango_point_value_stream_mapper";
    public static final String RENDERED_NULL_STRING = "—";

    protected final Map<Integer, DataPointVO> dataPoints;
    protected final Set<PointValueField> fields;
    protected final DateTimeFormatter dateTimeFormatter;
    protected final ZoneId zoneId;
    protected final RollupEnum rollup;
    protected final Locale locale;

    protected AbstractStreamMapper(StreamMapperBuilder options) {
        this.dataPoints = Collections.unmodifiableMap(options.dataPoints);
        this.fields = Collections.unmodifiableSet(options.fieldSet);
        this.dateTimeFormatter = options.dateTimeFormatter;
        this.zoneId = options.zoneId;
        this.rollup = options.rollup;
        this.locale = options.locale;

        // Mapper is used inside HttpMessageConverter, no way to pass it through directly so use request attribute
        RequestContextHolder.currentRequestAttributes()
                .setAttribute(MAPPER_ATTRIBUTE, this, RequestAttributes.SCOPE_REQUEST);
    }

    protected DataPointVO lookupPoint(int seriesId) {
        return Objects.requireNonNull(dataPoints.get(seriesId));
    }

    protected ZoneId zoneId() {
        ZoneId zoneId = this.zoneId;
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return zoneId;
    }

    public Object formatTime(Long timestamp) {
        if (dateTimeFormatter != null && timestamp != null) {
            return dateTimeFormatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId()));
        }
        return timestamp;
    }

    protected RollupEnum rollup(DataPointVO point) {
        if (this.rollup == RollupEnum.POINT_DEFAULT) {
            return RollupEnum.convertTo(point.getRollup());
        }
        return this.rollup;
    }

    /**
     * For NUMERIC points, converts a value from the raw value to the rendered unit value.
     * For non-NUMERIC points this method has no effect.
     *
     * @param point data point
     * @param value the raw value
     * @return the converted value if point is NUMERIC, otherwise the same value passed in.
     */
    protected Object convertValue(DataPointVO point, Object value) {
        if (point.getPointLocator().getDataType() == DataType.NUMERIC) {
            if (value instanceof Double) {
                return convertValue(point, (double) value);
            } else if (value instanceof NumericValue) {
                return convertValue(point, ((NumericValue) value).getDoubleValue());
            }
        }
        return value;
    }

    protected double convertValue(DataPointVO point, double value) {
        return point.getRenderedUnitConverter().convert(value);
    }

    protected StreamingPointValueTimeModel copyPointPropertiesToModel(DataPointVO point, StreamingPointValueTimeModel model) {
        if (fields.contains(PointValueField.XID)) {
            model.setXid(point.getXid());
        }
        if (fields.contains(PointValueField.NAME)) {
            model.setName(point.getName());
        }
        if (fields.contains(PointValueField.DEVICE_NAME)) {
            model.setDeviceName(point.getDeviceName());
        }
        if (fields.contains(PointValueField.DATA_SOURCE_NAME)) {
            model.setDataSourceName(point.getDataSourceName());
        }
        return model;
    }

    /**
     * Renders a value using the point's text renderer. The text renderer converts numeric values to the rendered
     * unit before rendering.
     *
     * @param point data point
     * @param value point value
     * @return rendered string
     */
    protected String renderValue(DataPointVO point, @Nullable DataValue value) {
        return value == null ?
                RENDERED_NULL_STRING :
                point.getTextRenderer().getText(value, TextRenderer.HINT_FULL, locale);
    }

    protected String renderValue(DataPointVO point, double value) {
        return point.getTextRenderer().getText(value, TextRenderer.HINT_FULL, locale);
    }

    public Map<Integer, DataPointVO> getDataPoints() {
        return dataPoints;
    }

    public Set<PointValueField> getFields() {
        return fields;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public boolean isTimestampFormatted() {
        return dateTimeFormatter != null;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public RollupEnum getRollup() {
        return rollup;
    }

    public Locale getLocale() {
        return locale;
    }
}
