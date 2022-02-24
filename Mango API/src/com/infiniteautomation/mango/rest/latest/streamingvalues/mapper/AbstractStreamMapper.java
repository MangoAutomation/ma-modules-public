/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.measure.converter.UnitConverter;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Base class for mappers, uses options from the REST API parameters to map a stream of point values.
 *
 * @param <T> input type
 * @author Jared Wiltshire
 */
@NonNull
public abstract class AbstractStreamMapper<T> implements Function<T, StreamingPointValueTimeModel> {

    public static final String RENDERED_NULL_STRING = "—";

    private final Map<Integer, DataPointVO> dataPoints;
    protected final Set<PointValueField> fields;
    private final DateTimeFormatter dateTimeFormatter;
    private final ZoneId zoneId;
    private final RollupEnum rollup;

    protected AbstractStreamMapper(StreamMapperBuilder options) {
        this.dataPoints = Collections.unmodifiableMap(options.dataPoints);
        this.fields = Collections.unmodifiableSet(options.fieldSet);
        this.dateTimeFormatter = options.dateTimeFormatter;
        this.zoneId = options.zoneId;
        this.rollup = options.rollup;
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

    protected Object formatTime(long timestamp) {
        if (dateTimeFormatter != null) {
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

    protected Object extractValue(DataPointVO point, Object value) {
        if (point.getPointLocator().getDataType() == DataType.NUMERIC) {
            if (value instanceof Double) {
                return convertValue(point, (double) value);
            } else if (value instanceof NumericValue) {
                return convertValue(point, ((NumericValue) value).getDoubleValue());
            }
        }

        if (value instanceof DataValue) {
            return ((DataValue) value).getObjectValue();
        }

        return value;
    }

    protected double convertValue(DataPointVO point, double value) {
        UnitConverter converter = point.getUnit().getConverterTo(point.getRenderedUnit());
        return converter.convert(value);
    }
}
