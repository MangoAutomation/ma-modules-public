/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.mailingList;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.vo.mailingList.MailingList;

import io.swagger.annotations.ApiModelProperty;

/**
 * Model for Users who have read permissions on the mailing list
 *
 * @author Terry Packer
 *
 */
public class MailingListWithRecipientsModel extends MailingListModel {

    @ApiModelProperty("recipients are only shown for Users with read permissions for the mailing list")
    private List<EmailRecipientModel> recipients;

    public MailingListWithRecipientsModel() { }
    public MailingListWithRecipientsModel(MailingList vo) {
        super(vo);
    }

    /**
     * @return the recipients
     */
    public List<EmailRecipientModel> getRecipients() {
        return recipients;
    }

    /**
     * @param recipients the recipients to set
     */
    public void setRecipients(List<EmailRecipientModel> recipients) {
        this.recipients = recipients;
    }

    @Override
    public void fromVO(MailingList vo) {
        super.fromVO(vo);
    }

    @Override
    public MailingList toVO() {
        MailingList vo = super.toVO();
        if(vo.getEntries() == null)
            vo.setEntries(new ArrayList<>());
        if(recipients != null)
            for(EmailRecipientModel entry : recipients) {
                vo.getEntries().add(entry.fromModel());
            }
        return vo;
    }
}
