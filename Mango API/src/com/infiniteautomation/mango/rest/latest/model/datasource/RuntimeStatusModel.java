/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.datasource;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
public class RuntimeStatusModel {
    
    @ApiModelProperty("Most recent polls, up to 11 maximum")
    List<PollStatus> latestPolls;
    
    @ApiModelProperty("Most recent aborted polls, up to 11 maximum")
    List<PollStatus> latestAbortedPolls;
    
    /**
     * @return the latestPolls
     */
    public List<PollStatus> getLatestPolls() {
        return latestPolls;
    }

    /**
     * @param latestPolls the latestPolls to set
     */
    public void setLatestPolls(List<PollStatus> latestPolls) {
        this.latestPolls = latestPolls;
    }

    /**
     * @return the latestAbortedPolls
     */
    public List<PollStatus> getLatestAbortedPolls() {
        return latestAbortedPolls;
    }

    /**
     * @param latestAbortedPolls the latestAbortedPolls to set
     */
    public void setLatestAbortedPolls(List<PollStatus> latestAbortedPolls) {
        this.latestAbortedPolls = latestAbortedPolls;
    }



    public static final class PollStatus {
        
        private Date startTime;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Long duration;
        private boolean aborted;
        
        public PollStatus() { }
        
        public PollStatus(Date startTime, Long duration) {
            this.startTime = startTime;
            if(duration >= 0)
                this.duration = duration;
            else
                this.aborted = true;
        }
        
        /**
         * @return the startTime
         */
        public Date getStartTime() {
            return startTime;
        }
        /**
         * @param startTime the startTime to set
         */
        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }
        /**
         * @return the duration
         */
        public Long getDuration() {
            return duration;
        }
        /**
         * @param duration the duration to set
         */
        public void setDuration(Long duration) {
            this.duration = duration;
        }
        /**
         * @return the aborted
         */
        public boolean isAborted() {
            return aborted;
        }
        /**
         * @param aborted the aborted to set
         */
        public void setAborted(boolean aborted) {
            this.aborted = aborted;
        }
    }

}
