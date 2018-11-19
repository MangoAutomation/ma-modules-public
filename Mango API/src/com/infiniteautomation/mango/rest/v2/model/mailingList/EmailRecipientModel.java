/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(subTypes= {UserEntryModel.class, AddressEntryModel.class}, discriminator="recipientType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="recipientType")
@JsonSubTypes({
    //TODO Mailing list type?
    @JsonSubTypes.Type(value = UserEntryModel.class, name="USER"),
    @JsonSubTypes.Type(value = AddressEntryModel.class, name="ADDRESS"),
})
public abstract class EmailRecipientModel {

    public abstract EmailRecipient fromModel();
    
    public abstract RecipientListEntryBean toBean();
    
}
