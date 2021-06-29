/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.workitem;

import java.util.List;

import com.serotonin.timer.OrderedTaskInfo;

/**
 * Container for information on tasks that have ordered queues and are
 * currently running in the High and Medium Thread Pools
 *
 * @author Terry Packer
 */
public class BackgroundProcessingRunningStats {

    private List<OrderedTaskInfo> highPriorityOrderedQueueStats;
    private List<OrderedTaskInfo> mediumPriorityOrderedQueueStats;
    
    /**
     * @return the highPriorityOrderedQueueStats
     */
    public List<OrderedTaskInfo> getHighPriorityOrderedQueueStats() {
        return highPriorityOrderedQueueStats;
    }
    /**
     * @param highPriorityOrderedQueueStats the highPriorityOrderedQueueStats to set
     */
    public void setHighPriorityOrderedQueueStats(List<OrderedTaskInfo> highPriorityOrderedQueueStats) {
        this.highPriorityOrderedQueueStats = highPriorityOrderedQueueStats;
    }
    /**
     * @return the mediumPriorityOrderedQueueStats
     */
    public List<OrderedTaskInfo> getMediumPriorityOrderedQueueStats() {
        return mediumPriorityOrderedQueueStats;
    }
    /**
     * @param mediumPriorityOrderedQueueStats the mediumPriorityOrderedQueueStats to set
     */
    public void setMediumPriorityOrderedQueueStats(
            List<OrderedTaskInfo> mediumPriorityOrderedQueueStats) {
        this.mediumPriorityOrderedQueueStats = mediumPriorityOrderedQueueStats;
    }
    
    
}
