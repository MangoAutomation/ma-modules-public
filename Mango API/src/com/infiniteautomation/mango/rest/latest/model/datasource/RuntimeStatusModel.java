/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.datasource;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.util.ILifecycleState;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
public class RuntimeStatusModel {

    ILifecycleState state = ILifecycleState.TERMINATED;
    
    @ApiModelProperty("Most recent polls, up to 11 maximum")
    List<PollStatus> latestPolls;
    
    @ApiModelProperty("Most recent aborted polls, up to 11 maximum")
    List<PollStatus> latestAbortedPolls;

    List<ActiveEventTypeModel> activeEventTypes = Collections.emptyList();

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

    public ILifecycleState getState() {
        return state;
    }

    public void setState(ILifecycleState state) {
        this.state = state;
    }

    public List<ActiveEventTypeModel> getActiveEventTypes() {
        return activeEventTypes;
    }

    public void setActiveEventTypes(List<ActiveEventTypeModel> activeEventTypes) {
        this.activeEventTypes = activeEventTypes;
    }

    public static final class ActiveEventTypeModel {
        TranslatableMessage description;
        AlarmLevels alarmLevel;

        public ActiveEventTypeModel() {
        }

        public ActiveEventTypeModel(TranslatableMessage description, AlarmLevels alarmLevel) {
            this.description = description;
            this.alarmLevel = alarmLevel;
        }

        public TranslatableMessage getDescription() {
            return description;
        }

        public void setDescription(TranslatableMessage description) {
            this.description = description;
        }

        public AlarmLevels getAlarmLevel() {
            return alarmLevel;
        }

        public void setAlarmLevel(AlarmLevels alarmLevel) {
            this.alarmLevel = alarmLevel;
        }
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
