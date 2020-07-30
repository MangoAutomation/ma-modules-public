/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.workitem;

import java.util.List;

import com.serotonin.m2m2.util.timeout.RejectedTaskStats;

/**
 * Container for lists of information on tasks rejected from the High and Medium thread pools.
 *
 * @author Terry Packer
 */
public class BackgroundProcessingRejectedTaskStats {

    private List<RejectedTaskStats> highPriorityRejectedTaskStats;
    private List<RejectedTaskStats> mediumPriorityRejectedTaskStats;
    /**
     * @return the highPriorityRejectedTaskStats
     */
    public List<RejectedTaskStats> getHighPriorityRejectedTaskStats() {
        return highPriorityRejectedTaskStats;
    }
    /**
     * @param highPriorityRejectedTaskStats the highPriorityRejectedTaskStats to set
     */
    public void setHighPriorityRejectedTaskStats(
            List<RejectedTaskStats> highPriorityRejectedTaskStats) {
        this.highPriorityRejectedTaskStats = highPriorityRejectedTaskStats;
    }
    /**
     * @return the mediumPriorityRejectedTaskStats
     */
    public List<RejectedTaskStats> getMediumPriorityRejectedTaskStats() {
        return mediumPriorityRejectedTaskStats;
    }
    /**
     * @param mediumPriorityRejectedTaskStats the mediumPriorityRejectedTaskStats to set
     */
    public void setMediumPriorityRejectedTaskStats(
            List<RejectedTaskStats> mediumPriorityRejectedTaskStats) {
        this.mediumPriorityRejectedTaskStats = mediumPriorityRejectedTaskStats;
    }
}
