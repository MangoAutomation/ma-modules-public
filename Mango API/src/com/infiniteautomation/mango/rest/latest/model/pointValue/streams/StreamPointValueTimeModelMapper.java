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
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
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

    Map<Integer, DataPointVO> dataPoints = Collections.emptyMap();
    EnumSet<PointValueField> fieldSet = EnumSet.of(PointValueField.TIMESTAMP, PointValueField.VALUE);
    DateTimeFormatter dateTimeFormatter = null;
    ZoneId zoneId;
    RollupEnum rollup;

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

    public StreamPointValueTimeModelMapper withTimezone(@Nullable String timezone, ZonedDateTime... alternatives) {
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

    public StreamPointValueTimeModelMapper withDataPoint(DataPointVO dataPoint) {
        this.dataPoints = Map.of(dataPoint.getSeriesId(), dataPoint);
        return this;
    }

    public StreamPointValueTimeModelMapper withDataPoints(Collection<? extends DataPointVO> dataPoints) {
        this.dataPoints = dataPoints.stream()
                .collect(Collectors.toUnmodifiableMap(DataPointVO::getSeriesId, Functions.identity()));
        return this;
    }

    public StreamPointValueTimeModelMapper withRollup(RollupEnum rollup) {
        this.rollup = rollup;
        return this;
    }

    private ZoneId zoneId() {
        ZoneId zoneId = this.zoneId;
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
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
        StreamPointValueTimeModel model = new StreamPointValueTimeModel(point, v.getTime());
        for (PointValueField field : fieldSet) {
            switch (field) {
                case VALUE: {
                    DataValue value = v.getValue();
                    if (point.getPointLocator().getDataType() == DataType.NUMERIC) {
                        double convertedValue = convertValue(point, value.getDoubleValue());
                        model.setValue(convertedValue);
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
                        if (point.getPointLocator().getDataType() == DataType.NUMERIC) {
                            double convertedValue = convertValue(point, value.getDoubleValue());
                            model.setRendered(point.getTextRenderer().getText(convertedValue, TextRenderer.HINT_FULL));
                        } else {
                            model.setRendered(point.getTextRenderer().getText(v.getValue(), TextRenderer.HINT_FULL));
                        }
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

    private double getRollupValue(AnalogStatistics stats) {
        // TODO
        switch (rollup) {
            case AVERAGE:
                return stats.getAverage();
            case ARITHMETIC_MEAN:
                return stats.getArithmeticMean();
            case MINIMUM_IN_PERIOD:
                return stats.getMinimumInPeriod();
            case MAXIMUM_IN_PERIOD:
                return stats.getMaximumInPeriod();
            default:
                throw new IllegalStateException("Unknown rollup: " + rollup);
        }
    }

    private double convertValue(DataPointVO point, double value) {
        if (point.getRenderedUnit() != Unit.ONE) {
            UnitConverter converter = point.getUnit().getConverterTo(point.getRenderedUnit());
            return converter.convert(value);
        }
        return value;
    }

    public StreamPointValueTimeModel mapAnalogStatistics(DataPointVO point, AnalogStatistics stats) {
        // TODO add series id to AnalogStatistics object
        StreamPointValueTimeModel model = new StreamPointValueTimeModel(point, stats.getPeriodStartTime());
        for (PointValueField field : fieldSet) {
            switch (field) {
                case VALUE: {
                    double convertedValue = convertValue(point, getRollupValue(stats));
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
                    double convertedValue = convertValue(point, getRollupValue(stats));
                    model.setRendered(point.getTextRenderer().getText(convertedValue, TextRenderer.HINT_FULL));
                    break;
                }
                case RAW:
                    model.setRaw(getRollupValue(stats));
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
