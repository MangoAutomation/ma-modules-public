/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.RecipientListEntryBean;

/**
 * @author Terry Packer
 *
 */
public class MailingListEntryModel extends EmailRecipientModel {

    private String xid;
    private String name;
    
    public MailingListEntryModel() { }
    public MailingListEntryModel(MailingList list) {
        MailingList fullList = MailingListDao.getInstance().getFull(list.getReferenceId());
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
    public EmailRecipient fromModel() {
        MailingList vo =  MailingListDao.getInstance().getFullByXid(xid);
        return vo;
    }
    
    @Override
    public RecipientListEntryBean toBean() {
        RecipientListEntryBean bean = new RecipientListEntryBean();
        bean.setRecipientType(EmailRecipient.TYPE_MAILING_LIST);
        MailingList vo =  MailingListDao.getInstance().getFullByXid(xid);
        if(vo != null)
            bean.setReferenceId(vo.getId());
        return bean;
    }
    
}
