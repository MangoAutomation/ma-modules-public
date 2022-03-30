/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Functions;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Used to build any {@link AbstractStreamMapper} using parameters from the REST controller.
 *
 * @author Jared Wiltshire
 */
@NonNull
public class StreamMapperBuilder {
    Map<Integer, DataPointVO> dataPoints = Collections.emptyMap();
    EnumSet<PointValueField> fieldSet = EnumSet.of(PointValueField.TIMESTAMP, PointValueField.VALUE);
    DateTimeFormatter dateTimeFormatter = null;
    ZoneId zoneId;
    RollupEnum rollup;
    Locale locale = Locale.getDefault();
    TimestampSource timestampSource = TimestampSource.PERIOD_START_TIME;

    public <T> T build(Function<StreamMapperBuilder, T> constructor) {
        return constructor.apply(this);
    }

    public StreamMapperBuilder withFields(@Nullable PointValueField[] fields) {
        if (fields != null && fields.length > 0) {
            this.fieldSet = EnumSet.copyOf(Arrays.asList(fields));
        }
        return this;
    }

    public StreamMapperBuilder withDateTimeFormat(@Nullable String dateTimeFormat) {
        if (dateTimeFormat != null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        }
        return this;
    }

    public StreamMapperBuilder withTimezone(@Nullable String timezone, ZonedDateTime... alternatives) {
        if (timezone != null) {
            this.zoneId = ZoneId.of(timezone);
        } else {
            for (var alternative : alternatives) {
                if (alternative != null) {
                    this.zoneId = alternative.getZone();
                    break;
                }
            }
        }
        return this;
    }

    public StreamMapperBuilder withDataPoint(DataPointVO dataPoint) {
        this.dataPoints = Map.of(dataPoint.getSeriesId(), dataPoint);
        return this;
    }

    public StreamMapperBuilder withDataPoints(Collection<? extends DataPointVO> dataPoints) {
        this.dataPoints = dataPoints.stream()
                .collect(Collectors.toUnmodifiableMap(DataPointVO::getSeriesId, Functions.identity()));
        return this;
    }

    public StreamMapperBuilder withRollup(RollupEnum rollup) {
        this.rollup = rollup;
        return this;
    }

    public StreamMapperBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public StreamMapperBuilder withTimestampSource(TimestampSource timestampSource) {
        this.timestampSource = timestampSource;
        return this;
    }
}
