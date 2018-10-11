/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
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
        for(EmailRecipientModel entry : entries) {
            vo.getEntries().add(entry.fromModel());
        }
        
        return vo;
    }
    
    @Override
    protected MailingList newVO() {
        return new MailingList();
    }

}
