/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.user;

import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonView;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.mapping.JsonViews;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 * 
 */
@ApiModel(value = "User", description = "User Data Model", parent = AbstractRestModel.class)
@CSVEntity
@JsonPropertyOrder({ "username", "email" })
public class UserModel extends AbstractRestModel<User> {
	
	//TODO Make the JSON Views work, it currently does nothing
	@ApiModelProperty(value = "Messages for validation of data", required = false)
	@JsonProperty("validationMessages")
	@JsonView(JsonViews.Validation.class) //Only show in validation views (NOT WORKING YET)
	private List<RestValidationMessage> messages;
	
    public UserModel() {
        super(new User());
    }

    public UserModel(User user) {
        super(user);
    }

    @ApiModelProperty(value = "Username of User", required = true)
    @CSVColumnGetter(order=0, header="username")
    @JsonGetter("username")
    public String getUsername() {
        return data.getUsername();
    }

    @CSVColumnSetter(order=0, header="username")
    @JsonSetter("username")
    public void setUsername(String username) {
        data.setUsername(username);
    }

    //TODO This is not working yet
    // the idea is that the password will only be
    // available in the Test View
    @CSVColumnGetter(order=1, header="password")
    @JsonGetter("password")
    public String getPassword() {
        return ""; //data.getPassword(); 
    }
    @CSVColumnSetter(order=1, header="password")
    @JsonSetter("password")
    public void setPassword(String password) {
        data.setPassword(password);
    }

    @CSVColumnGetter(order=2, header="email")
    @JsonGetter("email")
    public String getEmail() {
        return data.getEmail();
    }
    
    @CSVColumnSetter(order=2, header="email")
    @JsonSetter("email")
    public void setEmail(String email) {
        data.setEmail(email);
    }

    @CSVColumnGetter(order=3, header="disabled")
    @JsonGetter("disabled")
    public Boolean getDisabled() {
        return data.isDisabled();
    }

    @CSVColumnSetter(order=3, header="disabled")
    @JsonSetter("disabled")
    public void setDisabled(Boolean disabled) {
        data.setDisabled(disabled);
    }

    @CSVColumnGetter(order=4, header="permissions")
    @JsonGetter("permissions")
    public String getPermissions() {
        return data.getPermissions();
    }

    @CSVColumnSetter(order=4, header="permissions")
    @JsonSetter("permissions")
    public void setPermissions(String permissions) {
        data.setPermissions(permissions);
    }

    @CSVColumnGetter(order=5, header="homeUrl")
    @JsonGetter("homeUrl")
    public String getHomeUrl() {
        return data.getHomeUrl();
    }

    @CSVColumnSetter(order=5, header="homeUrl")
    @JsonSetter("homeUrl")
    public void setHomeUrl(String homeUrl) {
        data.setHomeUrl(homeUrl);
    }

    @CSVColumnGetter(order=6, header="receiveAlarmEmails")
    @JsonGetter("receiveAlarmEmails")
    public String getReceiveAlarmEmails() {
    	return AlarmLevels.CODES.getCode(this.data.getReceiveAlarmEmails());
    }

    @CSVColumnSetter(order=6, header="receiveAlarmEmails")
    @JsonSetter("receiveAlarmEmails")
    public void setReceiveAlarmEmails(String level) {
        data.setReceiveAlarmEmails(AlarmLevels.CODES.getId(level));
    }

    @CSVColumnGetter(order=7, header="timezone")
    @JsonGetter("timezone")
    public String getTimezone() {
        return data.getTimezone();
    }

    @CSVColumnSetter(order=7, header="timezone")
    @JsonSetter("timezone")
    public void setTimezone(String zone) {
        data.setTimezone(zone);
    }

    @CSVColumnGetter(order=8, header="systemTimezone")
    @JsonGetter("systemTimezone")
    public String getSystemTimezone() {
        return TimeZone.getDefault().getID();
    }

    @CSVColumnSetter(order=8, header="systemTimezone")
    @JsonSetter("systemTimezone")
    public void setSystemTimezone(String zone) {
        // no op
    }

    @CSVColumnGetter(order=9, header="muted")
    @JsonGetter("muted")
    public Boolean getMuted() {
        return data.isMuted();
    }

    @CSVColumnSetter(order=9, header="muted")
    @JsonSetter("muted")
    public void setMuted(Boolean muted) {
        data.setMuted(muted);
    }

    
    
	public List<RestValidationMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<RestValidationMessage> messages) {
		this.messages = messages;
	}

	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel#validate(com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult)
	 */
	@Override
	public boolean validate(){
		ProcessResult validation = new ProcessResult();
		this.data.validate(validation);
		
		if(validation.getHasMessages()){
			//Add our messages to the list
			for(ProcessMessage message : validation.getMessages()){
				this.messages.add(new RestValidationMessage(
						message.getContextualMessage().translate(Common.getTranslations()),
						RestMessageLevel.ERROR,
						message.getContextKey()
						));
			}
			return false;
		}else{
			return true; //Validated ok
		}
	}
}
