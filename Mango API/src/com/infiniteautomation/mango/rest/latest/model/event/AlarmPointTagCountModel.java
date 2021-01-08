/*
 * *
 *  * Copyright (C) 2021 RadixIot Software. All rights reserved.
 *  * @Author Terry Packer
 *
 */

package com.infiniteautomation.mango.rest.latest.model.event;

import java.util.Date;
import java.util.Map;

import com.infiniteautomation.mango.spring.service.EventInstanceService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;

public class AlarmPointTagCountModel {

    private final String xid;
    private final String name;
    private final String deviceName;
    private final TranslatableMessage message;
    private final AlarmLevels alarmLevel;
    private final int count;
    private final Date latestActive;
    private final Date latestRtn;
    private final Map<String,String> tags;

    public AlarmPointTagCountModel(EventInstanceService.AlarmPointTagCount count) {
        this.xid = count.getXid();
        this.name = count.getName();
        this.deviceName = count.getDeviceName();
        this.message = count.getMessage();
        this.alarmLevel = count.getAlarmLevel();
        this.count = count.getCount();
        if(count.getLatestActiveTs() != null) {
            this.latestActive = new Date(count.getLatestActiveTs());
        }else{
            this.latestActive = null;
        }

        if(count.getLatestRtnTs() != null){
            this.latestRtn = new Date(count.getLatestRtnTs());
        }else{
            this.latestRtn = null;
        }
        this.tags = count.getTags();
    }

    public String getXid() {
        return xid;
    }

    public String getName() {
        return name;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public TranslatableMessage getMessage() {
        return message;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public int getCount() {
        return count;
    }

    public Date getLatestActive() {
        return latestActive;
    }

    public Date getLatestRtn() {
        return latestRtn;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public boolean isActive() {
        if(latestActive != null && latestRtn != null) {
            return latestRtn.before(latestActive);
        }else {
            return false;
        }
    }

    public long getLatestActiveDuration() {
        if(latestActive != null && latestRtn != null) {
            if(isActive()) {
                return Common.timer.currentTimeMillis() - latestActive.getTime();
            }else {
                return latestRtn.getTime() - latestActive.getTime();
            }
        }else {
            return 0;
        }
    }
}
