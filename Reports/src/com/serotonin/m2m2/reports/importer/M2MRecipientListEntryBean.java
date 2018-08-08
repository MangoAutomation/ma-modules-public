/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.IOException;
import java.io.Serializable;

import com.infiniteautomation.mango.spring.dao.UserDao;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.AddressEntry;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.UserEntry;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;

/**
 * @author Terry Packer
 *
 */
public class M2MRecipientListEntryBean implements Serializable, JsonSerializable {
    
	private static final long serialVersionUID = -1;

    private int recipientType;
    private int referenceId;
    private String referenceAddress;

    public EmailRecipient createEmailRecipient() {
        switch (recipientType) {
        case EmailRecipient.TYPE_MAILING_LIST:
            MailingList ml = new MailingList();
            ml.setId(referenceId);
            return ml;
        case EmailRecipient.TYPE_USER:
            UserEntry u = new UserEntry();
            u.setUserId(referenceId);
            return u;
        case EmailRecipient.TYPE_ADDRESS:
            AddressEntry a = new AddressEntry();
            a.setAddress(referenceAddress);
            return a;
        }
        throw new ShouldNeverHappenException("Unknown email recipient type: " + recipientType);
    }

    public String getReferenceAddress() {
        return referenceAddress;
    }

    public void setReferenceAddress(String address) {
        referenceAddress = address;
    }

    public int getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(int typeId) {
        recipientType = typeId;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int refId) {
        referenceId = refId;
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("recipientType", EmailRecipient.TYPE_CODES.getCode(recipientType));
        if (recipientType == EmailRecipient.TYPE_MAILING_LIST)
            writer.writeEntry("mailingList", MailingListDao.instance.getMailingList(referenceId).getXid());
        else if (recipientType == EmailRecipient.TYPE_USER)
            writer.writeEntry("username", UserDao.instance.getUser(referenceId).getUsername());
        else if (recipientType == EmailRecipient.TYPE_ADDRESS)
            writer.writeEntry("address", referenceAddress);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("recipientType");
        if (text == null)
            throw new TranslatableJsonException("emport.error.recipient.missing", "recipientType",
                    EmailRecipient.TYPE_CODES.getCodeList());

        recipientType = EmailRecipient.TYPE_CODES.getId(text);
        if (recipientType == -1)
            throw new TranslatableJsonException("emport.error.recipient.invalid", "recipientType", text,
                    EmailRecipient.TYPE_CODES.getCodeList());

        if (recipientType == EmailRecipient.TYPE_MAILING_LIST) {
            text = jsonObject.getString("mailingList");
            if (text == null)
                throw new TranslatableJsonException("emport.error.recipient.missing.reference", "mailingList");

            MailingList ml = MailingListDao.instance.getMailingList(text);
            if (ml == null)
                throw new TranslatableJsonException("emport.error.recipient.invalid.reference", "mailingList", text);

            referenceId = ml.getId();
        }
        else if (recipientType == EmailRecipient.TYPE_USER) {
            text = jsonObject.getString("username");
            if (text == null)
                throw new TranslatableJsonException("emport.error.recipient.missing.reference", "username");

            User user = UserDao.instance.getUser(text);
            if (user == null)
                throw new TranslatableJsonException("emport.error.recipient.invalid.reference", "user", text);

            referenceId = user.getId();
        }
        else if (recipientType == EmailRecipient.TYPE_ADDRESS) {
            referenceAddress = jsonObject.getString("address");
            if (referenceAddress == null)
                throw new TranslatableJsonException("emport.error.recipient.missing.reference", "address");
        }
    }

	/**
	 * @param legacyDao
	 * @return
	 */
	public RecipientListEntryBean convert(M2MReportDao legacyDao) {
		RecipientListEntryBean bean = new RecipientListEntryBean();
		bean.setRecipientType(recipientType);
		
		switch(recipientType){
			case EmailRecipient.TYPE_USER:
				String username = legacyDao.getUsername(referenceId);
				User user = UserDao.instance.getUser(username);
				if(user != null)
					bean.setReferenceId(user.getId());
				else
					throw new ShouldNeverHappenException("User " + username + " not found in Mango.");
				break;
			case EmailRecipient.TYPE_ADDRESS:
				bean.setReferenceAddress(referenceAddress);
				break;
			case EmailRecipient.TYPE_MAILING_LIST:
				String listXid = legacyDao.getMailingListXid(referenceId);
				MailingList list = MailingListDao.instance.getMailingList(listXid);
				if(list != null)
					bean.setReferenceId(list.getId());
				else
					throw new ShouldNeverHappenException("Mailing list with XID: " + listXid + " not found in Mango.");
				break;
		}
		
		return bean;
	}

	/**
	 * @param jsonWriter
	 * @param writer
	 * @param legacyDao
	 * @throws JsonException 
	 * @throws IOException 
	 */
	public void jsonWrite(JsonWriter jsonWriter,M2MReportDao legacyDao) throws IOException, JsonException {
		jsonWriter.indent();
		jsonWriter.append("{");
		jsonWriter.increaseIndent();

		writeEntry("recipientType", EmailRecipient.TYPE_CODES.getCode(recipientType), jsonWriter, true);
		jsonWriter.append(",");
		
        if (recipientType == EmailRecipient.TYPE_MAILING_LIST)
            writeEntry("mailingList", legacyDao.getMailingListXid(referenceId), jsonWriter, true);
        else if (recipientType == EmailRecipient.TYPE_USER)
            writeEntry("username", legacyDao.getUsername(referenceId), jsonWriter, true);
        else if (recipientType == EmailRecipient.TYPE_ADDRESS)
            writeEntry("address", referenceAddress, jsonWriter, true);
        
		jsonWriter.decreaseIndent();
		jsonWriter.indent();
		jsonWriter.append("}");
	}
	
	private void writeEntry(String name, String value, JsonWriter writer, boolean quote) throws IOException{
		writer.indent();
		writer.quote(name);
		writer.append(':');
		if(quote)
			writer.quote(value);
		else
			writer.append(value);
	}
}