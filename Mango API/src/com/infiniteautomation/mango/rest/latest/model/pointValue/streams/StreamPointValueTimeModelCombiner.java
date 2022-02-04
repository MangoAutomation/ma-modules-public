/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import com.infiniteautomation.mango.db.iterators.GroupingSpliterator.Combiner;

/**
 * Combines {@link StreamPointValueTimeModel} with the same timestamp into a single {@link MultiPointModel}.
 *
 * @author Jared Wiltshire
 */
public class StreamPointValueTimeModelCombiner implements Combiner<StreamPointValueTimeModel, MultiPointModel> {

    @Override
    public MultiPointModel combineValue(MultiPointModel group, StreamPointValueTimeModel value) {
        if (group == null) {
            group = new MultiPointModel(value.pointValueTime.getTime());
        } else if (group.getTimestamp() != value.pointValueTime.getTime()) {
            return null;
        }
        group.putPointValue(value.point.getXid(), value);
        return group;
    }

}
