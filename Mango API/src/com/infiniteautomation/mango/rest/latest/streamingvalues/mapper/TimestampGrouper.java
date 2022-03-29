/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

import java.util.function.Function;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.db.iterators.GroupingSpliterator;
import com.infiniteautomation.mango.db.iterators.GroupingSpliterator.Combiner;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingMultiPointModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;

/**
 * Groups multiple {@link StreamingPointValueTimeModel} with the same timestamp into a single {@link StreamingMultiPointModel}.
 *
 * @author Jared Wiltshire
 */
public class TimestampGrouper implements Combiner<StreamingPointValueTimeModel, StreamingMultiPointModel> {

    private final Function<Long, Object> timeFormatter;

    public TimestampGrouper(Function<Long, Object> timeFormatter) {
        this.timeFormatter = timeFormatter;
    }

    @Override
    public @NonNull StreamingMultiPointModel combineValue(StreamingMultiPointModel group, StreamingPointValueTimeModel value) {
        if (group == null || group.getExactTimestamp() != value.getExactTimestamp()) {
            group = new StreamingMultiPointModel(value.getExactTimestamp(), timeFormatter.apply(value.getExactTimestamp()));
        }
        group.addPointValue(value.getDataPointXid(), value);
        return group;
    }

    public static Stream<StreamingMultiPointModel> groupByTimestamp(Stream<StreamingPointValueTimeModel> stream, Function<Long, Object> timeFormatter) {
        return GroupingSpliterator.group(stream, new TimestampGrouper(timeFormatter));
    }

}
