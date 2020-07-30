/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.vo.mailingList.AddressEntry;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="ADDRESS", parent=EmailRecipientModel.class)
@JsonTypeName("ADDRESS")
public class AddressEntryModel extends EmailRecipientModel {

    private String address;

    public AddressEntryModel() { }

    public AddressEntryModel(AddressEntry entry) {
        this.address = entry.getAddress();
    }

    @JsonGetter("address")
    public String getAddress(){
        return address;
    }
    @JsonSetter("address")
    public void setAddress(String address){
        this.address = address;
    }

    @Override
    public MailingListRecipient fromModel() {
        AddressEntry entry = new AddressEntry();
        entry.setAddress(address);
        return entry;
    }

}
