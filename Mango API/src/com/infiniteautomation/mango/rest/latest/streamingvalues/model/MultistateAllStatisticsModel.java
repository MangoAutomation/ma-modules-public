/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import java.util.List;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * Used for data points with MULTISTATE and BINARY data type.
 *
 * @author Jared Wiltshire
 */
public class MultistateAllStatisticsModel extends AllStatisticsModel {
    List<StartsAndRuntimeModel> startsAndRuntimes;

    public List<StartsAndRuntimeModel> getStartsAndRuntimes() {
        return startsAndRuntimes;
    }

    public void setStartsAndRuntimes(List<StartsAndRuntimeModel> startsAndRuntimes) {
        this.startsAndRuntimes = startsAndRuntimes;
    }

    public static class StartsAndRuntimeModel {
        double proportion;
        String rendered;
        long runtime;
        int starts;
        DataValue value;

        public StartsAndRuntimeModel() {
        }

        public StartsAndRuntimeModel(DataValue value, String rendered, int starts, long runtime, double proportion) {
            this.proportion = proportion;
            this.rendered = rendered;
            this.runtime = runtime;
            this.starts = starts;
            this.value = value;
        }

        public double getProportion() {
            return proportion;
        }

        public void setProportion(double proportion) {
            this.proportion = proportion;
        }

        public String getRendered() {
            return rendered;
        }

        public void setRendered(String rendered) {
            this.rendered = rendered;
        }

        public long getRuntime() {
            return runtime;
        }

        public void setRuntime(long runtime) {
            this.runtime = runtime;
        }

        public int getStarts() {
            return starts;
        }

        public void setStarts(int starts) {
            this.starts = starts;
        }

        public DataValue getValue() {
            return value;
        }

        public void setValue(DataValue value) {
            this.value = value;
        }
    }
}
