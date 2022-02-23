/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.AllStatisticsModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.ValueModel;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.pointvalue.AggregateValue;
import com.serotonin.m2m2.db.dao.pointvalue.NumericAggregate;
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
public class AggregateValueMapper extends AbstractStreamMapper<SeriesValueTime<? extends AggregateValue>> {

    public AggregateValueMapper(StreamMapperBuilder options) {
        super(options);
    }

    @Override
    public StreamingPointValueTimeModel apply(SeriesValueTime<? extends AggregateValue> value) {
        DataPointVO point = lookupPoint(value.getSeriesId());
        RollupEnum rollup = rollup(point);
        AggregateValue aggregate = value.getValue();

        StreamingPointValueTimeModel model = new StreamingPointValueTimeModel(point.getXid(), value.getTime());

        if (rollup == RollupEnum.ALL) {
            AllStatisticsModel allStatisticsModel = getAllRollup(point, aggregate);
            model.setAllStatistics(allStatisticsModel);
        } else {
            ValueModel rollupValue = getRollupValue(point, aggregate, rollup);
            model.setValue(rollupValue);
        }

        for (PointValueField field : fields) {
            switch (field) {
                case TIMESTAMP:
                    model.setTimestamp(formatTime(value.getTime()));
                    break;
                case CACHED:
                    model.setCached(false);
                    break;
                case BOOKEND:
                    model.setBookend(false);
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
                case ANNOTATION:
                    // can't annotate rollup values
                case VALUE:
                case RENDERED:
                case RAW:
                    // handled above by getRollupValue()
                    break;
                default:
                    throw new IllegalStateException("Unknown field: " + field);
            }
        }
        return model;
    }

    private AllStatisticsModel getAllRollup(DataPointVO point, AggregateValue stats) {
        AllStatisticsModel all = new AllStatisticsModel();
        all.setCount(stats.getCount());
        all.setFirst(getRollupValue(point, stats, RollupEnum.FIRST));
        all.setLast(getRollupValue(point, stats, RollupEnum.LAST));
        all.setStart(getRollupValue(point, stats, RollupEnum.START));
        return all;
    }

    private ValueModel getRollupValue(DataPointVO point, AggregateValue aggregate, RollupEnum rollup) {
        Object rawValue;

        switch (rollup) {
            case FIRST:
                rawValue = aggregate.getFirstValue();
                break;
            case LAST:
                rawValue = aggregate.getLastValue();
                break;
            case COUNT:
                rawValue = aggregate.getCount();
                break;
            case START:
                rawValue = aggregate.getStartValue();
                break;
            case AVERAGE:
                rawValue = extractNumeric(aggregate, NumericAggregate::getAverage);
                break;
            case DELTA:
                rawValue = extractNumeric(aggregate, NumericAggregate::getDelta);
                break;
            case MINIMUM:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMinimumValue);
                break;
            case MAXIMUM:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMaximumValue);
                break;
            case SUM:
                rawValue = extractNumeric(aggregate, NumericAggregate::getSum);
                break;
            case INTEGRAL:
                rawValue = extractNumeric(aggregate, NumericAggregate::getIntegral);
                break;
            case ARITHMETIC_MEAN:
                rawValue = extractNumeric(aggregate, NumericAggregate::getArithmeticMean);
                break;
            case MINIMUM_IN_PERIOD:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMinimumInPeriod);
                break;
            case MAXIMUM_IN_PERIOD:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMaximumInPeriod);
                break;
            case ALL:
                // fall through, not supported here
            default:
                throw new IllegalArgumentException("Unsupported rollup: " + rollup);
        }

        ValueModel model = new ValueModel();
        if (fields.contains(PointValueField.VALUE)) {
            Object convertedValue = extractValue(point, rawValue);
            model.setValue(convertedValue);
        }
        if (fields.contains(PointValueField.RAW)) {
            model.setRaw(rawValue instanceof DataValue ? ((DataValue) rawValue).getObjectValue() : rawValue);
        }
        if (fields.contains(PointValueField.RENDERED)) {
            String rendered;
            if (rawValue instanceof DataValue) {
                // the text renderer converts numeric values to the appropriate unit before rendering
                rendered = point.getTextRenderer().getText((DataValue) rawValue, TextRenderer.HINT_FULL);
            } else if (rawValue instanceof Double && point.getPointLocator().getDataType() == DataType.NUMERIC) {
                rendered = point.getTextRenderer().getText((double) rawValue, TextRenderer.HINT_FULL);
            } else if (rawValue == null) {
                rendered = RENDERED_NULL_STRING;
            } else {
                rendered = String.valueOf(rawValue);
            }
            model.setRendered(rendered);
        }
        return model;
    }

    private <X> X extractNumeric(AggregateValue value, Function<NumericAggregate, X> extractor) {
        if (value instanceof NumericAggregate) {
            return extractor.apply((NumericAggregate) value);
        }
        return null;
    }

}
