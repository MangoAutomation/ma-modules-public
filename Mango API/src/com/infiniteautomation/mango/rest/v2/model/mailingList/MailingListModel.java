/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.scheduling.util.DailySchedule;
import com.infiniteautomation.mango.scheduling.util.ScheduleUtils;
import com.infiniteautomation.mango.scheduling.util.TimeValue;
import com.infiniteautomation.mango.scheduling.util.WeeklySchedule;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.mailingList.AddressEntry;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.UserEntry;

/**
 * @author Terry Packer
 *
 */
public class MailingListModel extends AbstractVoModel<MailingList> {

    private List<EmailRecipientModel> entries;
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
     * @return the entries
     */
    public List<EmailRecipientModel> getEntries() {
        return entries;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(List<EmailRecipientModel> entries) {
        this.entries = entries;
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
        this.inactiveSchedule = createWeeklySchedule(vo.getInactiveIntervals());
        if(vo.getEntries() != null && vo.getEntries().size() > 0) {
            this.entries = new ArrayList<>();
            for(EmailRecipient entry : vo.getEntries()) {
                EmailRecipientModel e;
                switch(entry.getRecipientType()) {
                    case EmailRecipient.TYPE_ADDRESS:
                        e = new AddressEntryModel((AddressEntry) entry);
                        break;
                    case EmailRecipient.TYPE_USER:
                        e = new UserEntryModel((UserEntry) entry);
                        break;
                    case EmailRecipient.TYPE_MAILING_LIST:
                    default:
                        throw new ShouldNeverHappenException("Unsupported recipient type: " + entry.getRecipientType());
                            
                }
                this.entries.add(e);
            }
        }
    }

    @Override
    public MailingList toVO() {
        MailingList vo = super.toVO();
        vo.setReceiveAlarmEmails(AlarmLevels.CODES.getId(receiveAlarmEmails));
        vo.setReadPermissions(readPermissions);
        vo.setEditPermissions(editPermissions);
        vo.setInactiveIntervals(createInactiveSchedule(inactiveSchedule));
        if(vo.getEntries() == null)
            vo.setEntries(new ArrayList<>());
        for(EmailRecipientModel entry : entries) {
            vo.getEntries().add(entry.fromModel());
        }
        
        return vo;
    }
    
    /**
     * @param inactiveSchedule2
     * @return
     */
    private Set<Integer> createInactiveSchedule(WeeklySchedule inactiveSchedule) {
        if(inactiveSchedule == null)
            return null;
        else {
            Set<Integer> intervals = null;
            if(inactiveSchedule.getOffsetCount() > 0) {
                intervals = new TreeSet<>();
                //Modify a copy of the weekly schedule to put Monday first
                List<DailySchedule> copy = new ArrayList<>(inactiveSchedule.getDailySchedules());
                DailySchedule sunday = copy.remove(0);
                copy.add(copy.size(), sunday);
                int day = 0;
                Integer lastInterval = null;
                boolean inactive = false;
                for(DailySchedule schedule : copy) {
                    for(String change : schedule.getChanges()) {
                        TimeValue time = ScheduleUtils.parseTimeValue(change);
                        //Compute the interval
                        int interval = (day * 96) + (time.getHour() * 4) + (time.getMinute() / 15);
                        if(lastInterval == null || interval - lastInterval == 1) {
                            intervals.add(interval);
                            inactive = true;
                        }else if(interval - lastInterval > 1) {
                            if(inactive) {
                                //Fill offsets
                                for(int i=lastInterval + 1; i<interval; i++) {
                                    intervals.add(i);
                                }
                                inactive = false;
                            }else {
                                //Just skipped a block and now are inactive
                                intervals.add(interval);
                                inactive = true;
                            }
                            
                        }
                        lastInterval = interval;
                    }
                    //TODO Should we reset each day?
                    lastInterval = null;
                    inactive = false;
                    day++;
                }
            }
            return intervals;
        }
    }
    
    /**
     * Convert inactive intervals into a schedule
     * 
     * @param inactiveIntervals
     * @return
     */
    private WeeklySchedule createWeeklySchedule(Set<Integer> inactiveIntervals) {
        if(inactiveIntervals == null)
            return null;
        else {
            //NOTE: Weekly schedules start on Sunday, Mailing List intervals start on Monday.
            //0 = midnight - 00:15 Monday
            //671=11:45-00:00 Sunday
            //96 in a day
            WeeklySchedule schedule = new WeeklySchedule();
            for(int i=0; i<7; i++)
                schedule.addDay(new DailySchedule());
            
            Integer lastInterval = null;
            for(Integer interval : inactiveIntervals) {
                int day = interval/96;
                int dailyOffset = interval - (day * 96);
                int startHr = (dailyOffset * 15)/60;
                int startMin = (dailyOffset * 15) % 60;
                
                if(lastInterval == null)
                    schedule.getDailySchedules().get(day).addChange(String.format("%02d:%02d", startHr, + startMin));
                else if(interval-lastInterval > 1) {
                    //Close last and add next
                    int lastDay = (lastInterval + 1)/96;
                    int lastDailyOffset = (lastInterval + 1) - (lastDay * 96);
                    int lastStartHr = (lastDailyOffset * 15)/60;
                    int lastStartMin = (lastDailyOffset * 15) % 60;
                    schedule.getDailySchedules().get(lastDay).addChange(String.format("%02d:%02d", lastStartHr, lastStartMin));
                    schedule.getDailySchedules().get(day).addChange(String.format("%02d:%02d", startHr, startMin));
                }
                lastInterval = interval;
            }
            //Sunday is last in the list, place it first
            DailySchedule sunday = schedule.getDailySchedules().remove(6);
            schedule.getDailySchedules().add(0, sunday);
            
            return schedule;
        }
    }
    
    
    @Override
    protected MailingList newVO() {
        return new MailingList();
    }

}
