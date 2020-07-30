/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.mailingList;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;
import com.serotonin.m2m2.vo.mailingList.PhoneEntry;

import io.swagger.annotations.ApiModel;

/**
 *
 * @author Terry Packer
 */
/**
 * @author Terry Packer
 *
 */
@ApiModel(value="PHONE_NUMBER", parent=EmailRecipientModel.class)
@JsonTypeName("PHONE_NUMBER")
public class PhoneEntryModel extends EmailRecipientModel {

    private String phone;

    public PhoneEntryModel() { }

    public PhoneEntryModel(PhoneEntry entry) {
        this.phone = entry.getPhone();
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    @Override
    public MailingListRecipient fromModel() {
        PhoneEntry entry = new PhoneEntry();
        entry.setPhone(phone);
        return entry;
    }

}
