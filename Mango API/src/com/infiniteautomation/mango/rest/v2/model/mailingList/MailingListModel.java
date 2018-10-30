/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.scheduling.util.DailySchedule;
import com.infiniteautomation.mango.scheduling.util.ScheduleUtils;
import com.infiniteautomation.mango.scheduling.util.TimeValue;
import com.infiniteautomation.mango.scheduling.util.WeeklySchedule;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.mailingList.MailingList;

/**
 * @author Terry Packer
 *
 */
public class MailingListModel extends AbstractVoModel<MailingList> {

    private String receiveAlarmEmails;
    private Set<String> readPermissions;
    private Set<String> editPermissions;
    private WeeklySchedule inactiveSchedule;
    
    public MailingListModel() {
        super(new MailingList());
    }
    
    public MailingListModel(MailingList vo) {
        super(vo);
    }

    /**
     * @return the receiveAlarmEmails
     */
    public String getReceiveAlarmEmails() {
        return receiveAlarmEmails;
    }

    /**
     * @param receiveAlarmEmails the receiveAlarmEmails to set
     */
    public void setReceiveAlarmEmails(String receiveAlarmEmails) {
        this.receiveAlarmEmails = receiveAlarmEmails;
    }

    /**
     * @return the readPermissions
     */
    public Set<String> getReadPermissions() {
        return readPermissions;
    }

    /**
     * @param readPermissions the readPermissions to set
     */
    public void setReadPermissions(Set<String> readPermissions) {
        this.readPermissions = readPermissions;
    }

    /**
     * @return the editPermissions
     */
    public Set<String> getEditPermissions() {
        return editPermissions;
    }

    /**
     * @param editPermissions the editPermissions to set
     */
    public void setEditPermissions(Set<String> editPermissions) {
        this.editPermissions = editPermissions;
    }

    /**
     * @return the inactiveSchedule
     */
    public WeeklySchedule getInactiveSchedule() {
        return inactiveSchedule;
    }

    /**
     * @param inactiveSchedule the inactiveSchedule to set
     */
    public void setInactiveSchedule(WeeklySchedule inactiveSchedule) {
        this.inactiveSchedule = inactiveSchedule;
    }

    @Override
    public void fromVO(MailingList vo) {
        super.fromVO(vo);
        this.receiveAlarmEmails = AlarmLevels.CODES.getCode(vo.getReceiveAlarmEmails());
        this.readPermissions = vo.getReadPermissions();
        this.editPermissions = vo.getEditPermissions();
        this.inactiveSchedule = getInactiveIntervalsAsWeeklySchedule(vo.getInactiveIntervals());
    }

    @Override
    public MailingList toVO() {
        MailingList vo = super.toVO();
        vo.setReceiveAlarmEmails(AlarmLevels.CODES.getId(receiveAlarmEmails));
        vo.setReadPermissions(readPermissions);
        vo.setEditPermissions(editPermissions);
        //TODO Do we want to validate the schedule here as we can only validate offsets in the service?
//        ProcessResult result = new ProcessResult();
//        inactiveSchedule.validate(result);
//        if(!result.isValid())
//            throw new ValidationException(result);
        vo.setInactiveIntervals(weeklyScheduleToInactiveIntervals(inactiveSchedule));
        if(vo.getEntries() == null)
            vo.setEntries(new ArrayList<>());
        return vo;
    }
    
    /**
     * Convert a set of inactive intervals into a weekly schedule
     * @param inactiveIntervals
     * @return
     */
    private WeeklySchedule getInactiveIntervalsAsWeeklySchedule(Set<Integer> inactiveIntervals) {
        
        if(inactiveIntervals == null)
            return null;
        
        WeeklySchedule weeklySchedule = new WeeklySchedule();
        for(int k = 0; k < 7; k+=1) {
            weeklySchedule.addDay(new DailySchedule());
        }
        
        Integer[] inactive = new Integer[inactiveIntervals.size()];
        inactiveIntervals.toArray(inactive);
        Arrays.sort(inactive);
        
        int last = -2;
        for(Integer i : inactive) {
            if(i == null)
                continue;
            
            int dayIndex = i.intValue() / (4*24);
            int lastDayIndex;
            if(last != -2)
                lastDayIndex = last / (4*24);
            else
                lastDayIndex = dayIndex;
            
            if(last == i.intValue() - 1 && dayIndex == lastDayIndex) { //Still inactive
                last = i.intValue();
                continue;
            } else if (dayIndex != lastDayIndex) {
                if((last+1) % (4*24) != 0) {
                    int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                    int hr = (minute15 * 15) / 60;
                    int min = (minute15 * 15) % 60;
                    weeklySchedule.getDailySchedules().get(lastDayIndex).addChange(String.format("%02d:%02d", hr, min));
                }
                last = -2;
            }
            
            if(last != -2) {
                int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                int hr = (minute15 * 15) / 60;
                int min = (minute15 * 15) % 60;
                weeklySchedule.getDailySchedules().get(lastDayIndex).addChange(String.format("%02d:%02d", hr, min));
            }
            
            last = i.intValue();
            int minute15 = i.intValue() % (4*24);
            int hr = (minute15 * 15) / 60;
            int min = (minute15 * 15) % 60;
            weeklySchedule.getDailySchedules().get(dayIndex).addChange(String.format("%02d:%02d", hr, min));
        }
        
        if(last != -2 && last % (4*24) != 95) {
            int dayIndex = (last+1) / (4*24);
            int minute15 = (last+1) % (4*24);
            int hr = (minute15 * 15) / 60;
            int min = (minute15 * 15) % 60;
            weeklySchedule.getDailySchedules().get(dayIndex).addChange(String.format("%02d:%02d", hr, min));
        }
        
        //Re-Order putting Sunday first
        //Sunday is last in the list, place it first
        DailySchedule sunday = weeklySchedule.getDailySchedules().remove(6);
        weeklySchedule.getDailySchedules().add(0, sunday);
        
        return weeklySchedule;
    }
    
    /**
     * Convert a weekly schedule into a set of offsets.
     * 
     * Offsets Monday - Sunday
     * Schedule Sunday - Saturday
     * @param weeklySchedule
     * @return
     */
    private Set<Integer> weeklyScheduleToInactiveIntervals(WeeklySchedule weeklySchedule) {
        if(weeklySchedule == null)
            return null;
        
        //Modify a copy of the weekly schedule to put Monday first
        //TODO assert we have 7 days in the schedule
        List<DailySchedule> copy = new ArrayList<>(weeklySchedule.getDailySchedules());
        DailySchedule sunday = copy.remove(0);
        copy.add(copy.size(), sunday);
        
        Set<Integer> inactiveIntervals = new TreeSet<>();
        for(int k = 0; k < copy.size(); k+=1) {
            int baseInterval = k * 96 ; //milliseconds per day
            boolean deactivated = false;
            int lastInterval = -2;
            List<String> wsTimes = copy.get(k).getChanges();
            Integer[] times = new Integer[wsTimes.size()];
            int index = 0;
            for(String time : wsTimes) {
                TimeValue value = ScheduleUtils.parseTimeValue(time);
                //Compute offset from midnight
                times[index++] = (value.getHour() * 60 * 60 * 1000) + (value.getMinute() * 60 * 1000);
            }

            for(Integer i : times) {
                int thisInterval = i / 900000; //millis in 15m
                if(deactivated) {
                    while(lastInterval < thisInterval) {
                        inactiveIntervals.add(baseInterval+lastInterval++);
                    }
                    deactivated = false;
                } else {
                    lastInterval = thisInterval;
                    deactivated = true;
                }
            }
            
            if(deactivated) {
                while(lastInterval < 96) {
                    inactiveIntervals.add(baseInterval+lastInterval++);
                }
            }
        }
        return inactiveIntervals;
    }
    
    
    @Override
    protected MailingList newVO() {
        return new MailingList();
    }

}
