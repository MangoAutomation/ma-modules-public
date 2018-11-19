/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;

/**
 * @author Terry Packer
 *
 */
public class MailingListEntryModel extends EmailRecipientModel {

    private String xid;
    
    public MailingListEntryModel() { }
    public MailingListEntryModel(MailingList list) {
        this.xid = list.getXid();
    }
    
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
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
