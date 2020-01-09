/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.backgroundProcessing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.Validatable;

/**
 * @author Terry Packer
 *
 */
public abstract class ThreadPoolSettingsModel implements Validatable {

    @JsonProperty
    protected Integer corePoolSize;

    @JsonProperty
    protected Integer maximumPoolSize;

    @JsonProperty
    protected Integer activeCount; //Number of active Threads

    @JsonProperty
    protected Integer largestPoolSize; //Largest the pool has been

    public ThreadPoolSettingsModel(){ }


    /**
     * @param corePoolSize
     * @param maximumPoolSize
     * @param activeCount
     * @param largestPoolSize
     */
    public ThreadPoolSettingsModel(Integer corePoolSize, Integer maximumPoolSize,
            Integer activeCount, Integer largestPoolSize) {
        super();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.activeCount = activeCount;
        this.largestPoolSize = largestPoolSize;
    }


    public Integer getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
    }

    public Integer getLargestPoolSize() {
        return largestPoolSize;
    }

    public void setLargestPoolSize(Integer largestPoolSize) {
        this.largestPoolSize = largestPoolSize;
    }

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     *
     * @param result
     * @param currentCorePoolSize
     * @param currentMaxPoolSize
     */
    protected void validate(ProcessResult result, int currentCorePoolSize, int currentMaxPoolSize) {

        if((getCorePoolSize() != null) && (getCorePoolSize() < 1)) {
            result.addContextualMessage("corePoolSize", "validate.greaterThanZero");
        }

        //Compare both if they are being added
        if((getMaximumPoolSize() != null) && (getCorePoolSize() != null) && (getMaximumPoolSize() < getCorePoolSize())){
            result.addContextualMessage("maximumPoolSize", "validate.maxGreaterThanMin");
        }

        //Compare the max pool size to the existing if we aren't changing the Core size
        if((getCorePoolSize() == null) && (getMaximumPoolSize() != null)){
            if(getMaximumPoolSize() < currentCorePoolSize){
                setCorePoolSize(currentCorePoolSize);
                result.addContextualMessage("maximumPoolSize", "validate.maxGreaterThanMin");
            }
        }

        //Compare the max pool size to the existing if we aren't changing the Core size
        if((getMaximumPoolSize() == null) && (getCorePoolSize() != null)){

            if(getCorePoolSize() > currentMaxPoolSize){
                setMaximumPoolSize(currentMaxPoolSize);
                result.addContextualMessage("corePoolSize", "vvalidate.minLessThanMax");
            }
        }
    }

}
