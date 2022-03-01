/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.AllStatisticsModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.MultistateAllStatisticsModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.MultistateAllStatisticsModel.StartsAndRuntimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.NumericAllModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.ValueTimeModel;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.pointvalue.AggregateValue;
import com.serotonin.m2m2.db.dao.pointvalue.NumericAggregate;
import com.serotonin.m2m2.db.dao.pointvalue.StartsAndRuntimeAggregate;
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
            ValueTimeModel rollupValue = getRollupValue(point, aggregate, rollup);
            model.setValueModel(rollupValue);
        }

        if (fields.contains(PointValueField.CACHED)) {
            model.setCached(false);
        }
        if (fields.contains(PointValueField.BOOKEND)) {
            model.setBookend(false);
        }
        return copyPointPropertiesToModel(point, model);
    }

    private AllStatisticsModel getAllRollup(DataPointVO point, AggregateValue stats) {
        AllStatisticsModel all;
        if (stats instanceof NumericAggregate) {
            NumericAllModel model = new NumericAllModel();
            model.setAccumulator(getRollupValue(point, stats, RollupEnum.ACCUMULATOR));
            model.setAverage(getRollupValue(point, stats, RollupEnum.AVERAGE));
            model.setDelta(getRollupValue(point, stats, RollupEnum.DELTA));
            model.setIntegral(getRollupValue(point, stats, RollupEnum.INTEGRAL));
            model.setMaximum(getRollupValue(point, stats, RollupEnum.MAXIMUM));
            model.setMinimum(getRollupValue(point, stats, RollupEnum.MINIMUM));
            model.setSum(getRollupValue(point, stats, RollupEnum.SUM));
            model.setMaximumInPeriod(getRollupValue(point, stats, RollupEnum.MAXIMUM_IN_PERIOD));
            model.setMinimumInPeriod(getRollupValue(point, stats, RollupEnum.MINIMUM_IN_PERIOD));
            model.setArithmeticMean(getRollupValue(point, stats, RollupEnum.ARITHMETIC_MEAN));
            all = model;
        } else if (stats instanceof StartsAndRuntimeAggregate) {
            MultistateAllStatisticsModel model = new MultistateAllStatisticsModel();
            var startsStats = ((StartsAndRuntimeAggregate) stats);
            var startsModel = startsStats.getData().stream().map(start -> {
                String rendered = renderValue(point, start.getDataValue());
                return new StartsAndRuntimeModel(start.getDataValue(), rendered, start.getStarts(),
                        start.getRuntime(), start.getProportion());
            }).collect(Collectors.toUnmodifiableList());
            model.setStartsAndRuntimes(startsModel);
            all = model;
        } else {
            all = new AllStatisticsModel();
        }
        if (fields.contains(PointValueField.TIMESTAMP)) {
            all.setTimestamp(formatTime(stats.getPeriodStartTime()));
        }
        all.setCount(stats.getCount());
        all.setFirst(getRollupValue(point, stats, RollupEnum.FIRST));
        all.setLast(getRollupValue(point, stats, RollupEnum.LAST));
        all.setStart(getRollupValue(point, stats, RollupEnum.START));
        return all;
    }

    private ValueTimeModel getRollupValue(DataPointVO point, AggregateValue aggregate, RollupEnum rollup) {
        Object rawValue;
        Long timestamp = aggregate.getPeriodStartTime();

        switch (rollup) {
            case FIRST:
                rawValue = aggregate.getFirstValue();
                timestamp = aggregate.getFirstTime();
                break;
            case LAST:
                rawValue = aggregate.getLastValue();
                timestamp = aggregate.getLastTime();
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
            case ACCUMULATOR:
                rawValue = extractNumeric(aggregate, NumericAggregate::getAccumulator);
                break;
            case MINIMUM:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMinimumValue);
                timestamp = extractNumeric(aggregate, NumericAggregate::getMinimumTime);
                break;
            case MAXIMUM:
                rawValue = extractNumeric(aggregate, NumericAggregate::getMaximumValue);
                timestamp = extractNumeric(aggregate, NumericAggregate::getMaximumTime);
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

        ValueTimeModel model = new ValueTimeModel();
        if (fields.contains(PointValueField.TIMESTAMP)) {
            model.setTimestamp(formatTime(timestamp));
        }
        if (fields.contains(PointValueField.VALUE)) {
            Object convertedValue;
            if (rollup == RollupEnum.INTEGRAL && rawValue != null) {
                convertedValue = point.getIntegralConverter().convert((double) rawValue);
            } else {
                convertedValue = convertValue(point, rawValue);
            }
            model.setValue(convertedValue);
        }
        if (fields.contains(PointValueField.RAW)) {
            model.setRaw(rawValue);
        }
        if (fields.contains(PointValueField.RENDERED)) {
            String rendered;
            if (rollup == RollupEnum.INTEGRAL && rawValue != null) {
                rendered = point.createIntegralRenderer().getText((double) rawValue, TextRenderer.HINT_FULL, locale);
            } else {
                rendered = getRenderedValue(point, rawValue);
            }
            model.setRendered(rendered);
        }
        return model;
    }

    private String getRenderedValue(DataPointVO point, Object rawValue) {
        String result;
        if (rawValue instanceof DataValue || rawValue == null) {
            // the text renderer converts numeric values to the appropriate unit before rendering
            result = renderValue(point, (DataValue) rawValue);
        } else if (rawValue instanceof Double && point.getPointLocator().getDataType() == DataType.NUMERIC) {
            result = renderValue(point, (double) rawValue);
        } else {
            result = String.valueOf(rawValue);
        }
        return result;
    }

    private <X> X extractNumeric(AggregateValue value, Function<NumericAggregate, X> extractor) {
        if (value instanceof NumericAggregate) {
            return extractor.apply((NumericAggregate) value);
        }
        return null;
    }

}
