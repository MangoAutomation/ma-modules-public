/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.MailingListEntry;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;

/**
 * @author Terry Packer
 *
 */
public class MailingListEntryModel extends EmailRecipientModel {

    private String xid;
    private String name;

    public MailingListEntryModel() { }
    public MailingListEntryModel(MailingListEntry list) {
        MailingList fullList = MailingListDao.getInstance().get(list.getReferenceId());
        if(fullList != null) {
            this.xid = fullList.getXid();
            this.name = fullList.getName();
        }
    }

    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MailingListRecipient fromModel() {
        MailingListEntry entry = new MailingListEntry();
        Integer id = MailingListDao.getInstance().getIdByXid(xid);
        if(id != null) {
            entry.setMailingListId(id);
        }
        return entry;
    }

}
