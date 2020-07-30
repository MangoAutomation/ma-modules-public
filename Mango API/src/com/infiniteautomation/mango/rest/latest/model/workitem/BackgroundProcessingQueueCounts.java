/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.workitem;

import java.util.Map;

/**
 * Container for all instance counts for the 3 Work Item Queues
 *
 * @author Terry Packer
 */
public class BackgroundProcessingQueueCounts {

    private Map<String, Integer> highPriorityServiceQueueClassCounts;
    private Map<String, Integer> mediumPriorityServiceQueueClassCounts;
    private Map<String, Integer> lowPriorityServiceQueueClassCounts;
    
    /**
     * @return the highPriorityServiceQueueClassCounts
     */
    public Map<String, Integer> getHighPriorityServiceQueueClassCounts() {
        return highPriorityServiceQueueClassCounts;
    }
    /**
     * @param highPriorityServiceQueueClassCounts the highPriorityServiceQueueClassCounts to set
     */
    public void setHighPriorityServiceQueueClassCounts(
            Map<String, Integer> highPriorityServiceQueueClassCounts) {
        this.highPriorityServiceQueueClassCounts = highPriorityServiceQueueClassCounts;
    }
    /**
     * @return the mediumPriorityServiceQueueClassCounts
     */
    public Map<String, Integer> getMediumPriorityServiceQueueClassCounts() {
        return mediumPriorityServiceQueueClassCounts;
    }
    /**
     * @param mediumPriorityServiceQueueClassCounts the mediumPriorityServiceQueueClassCounts to set
     */
    public void setMediumPriorityServiceQueueClassCounts(
            Map<String, Integer> mediumPriorityServiceQueueClassCounts) {
        this.mediumPriorityServiceQueueClassCounts = mediumPriorityServiceQueueClassCounts;
    }
    /**
     * @return the lowPriorityServiceQueueClassCounts
     */
    public Map<String, Integer> getLowPriorityServiceQueueClassCounts() {
        return lowPriorityServiceQueueClassCounts;
    }
    /**
     * @param lowPriorityServiceQueueClassCounts the lowPriorityServiceQueueClassCounts to set
     */
    public void setLowPriorityServiceQueueClassCounts(
            Map<String, Integer> lowPriorityServiceQueueClassCounts) {
        this.lowPriorityServiceQueueClassCounts = lowPriorityServiceQueueClassCounts;
    }
}
