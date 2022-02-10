/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.infiniteautomation.mango.db.iterators.GroupingSpliterator;
import com.infiniteautomation.mango.db.iterators.GroupingSpliterator.Combiner;

/**
 * Combines {@link StreamPointValueTimeModel} with the same timestamp into a single {@link MultiPointModel}.
 *
 * @author Jared Wiltshire
 */
public class StreamPointValueTimeModelCombiner implements Combiner<StreamPointValueTimeModel, MultiPointModel> {

    @Override
    public @NonNull MultiPointModel combineValue(MultiPointModel group, StreamPointValueTimeModel value) {
        if (group == null || group.getExactTimestamp() != value.pointValueTime.getTime()) {
            group = new MultiPointModel(value.pointValueTime.getTime(), value.getTimestamp());
        }
        group.putPointValue(value.point.getXid(), value);
        return group;
    }

    public static Stream<MultiPointModel> groupByTimestamp(Stream<StreamPointValueTimeModel> stream) {
        return GroupingSpliterator.group(stream, new StreamPointValueTimeModelCombiner());
    }

}
