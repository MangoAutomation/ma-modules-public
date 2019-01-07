/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value = "User", description = "User Data Model", parent = AbstractRestModel.class)
@CSVEntity
@JsonPropertyOrder({ "username", "email" })
public class UserModel extends AbstractRestModel<User> {

    @ApiModelProperty(value = "Messages for validation of data", required = false)
    @JsonProperty("validationMessages")
    private List<RestValidationMessage> messages;

    // By default, any passwords set via the REST API are just plain text
    // You can however set the hashAlgorithm property to BCRYPT, SHA-1 etc to send a pre-hashed password via the API
    private String hashAlgorithm = User.PLAIN_TEXT_ALGORITHM;
    private String password = null;

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

    //TODO Fix up the CSV Stuff so we can have only Setters
    @CSVColumnGetter(order=1, header="password")
    @JsonIgnore
    public String getPasswordForCsv(){
        return "";
    }

    public String getPassword() {
        //return data.getPassword();
        // dont return password hashes over the REST API, security issue
        return "";
    }

    public boolean isOldHashAlgorithm() {
        //New Users have null passwords
        if(data.getPassword() == null)
            return false;

        String algorithm = Common.extractHashAlgorithm(data.getPassword());

        if (User.LOCKED_ALGORITHM.equals(algorithm)) {
            // not old algorithm, just locked
            return false;
        }

        String defaultAlgorithm = Common.getHashAlgorithm();
        return !defaultAlgorithm.equals(algorithm);
    }

    @CSVColumnSetter(order=1, header="password")
    @JsonSetter("password")
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.password = password;
            data.setPasswordHash(this.hashAlgorithm, this.password);
        }
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

    @CSVColumnGetter(order=3, header="phone")
    @JsonGetter("phone")
    public String getPhone() {
        return data.getPhone();
    }

    @CSVColumnSetter(order=3, header="phone")
    @JsonSetter("phone")
    public void setPhone(String phone) {
        data.setPhone(phone);
    }

    @CSVColumnGetter(order=4, header="disabled")
    @JsonGetter("disabled")
    public Boolean getDisabled() {
        return data.isDisabled();
    }

    @CSVColumnSetter(order=4, header="disabled")
    @JsonSetter("disabled")
    public void setDisabled(Boolean disabled) {
        data.setDisabled(disabled);
    }

    @CSVColumnGetter(order=5, header="permissions")
    @JsonGetter("permissions")
    public String getPermissions() {
        return data.getPermissions();
    }

    @CSVColumnSetter(order=5, header="permissions")
    @JsonSetter("permissions")
    public void setPermissions(String permissions) {
        data.setPermissions(permissions);
    }

    @CSVColumnGetter(order=6, header="homeUrl")
    @JsonGetter("homeUrl")
    public String getHomeUrl() {
        return data.getHomeUrl();
    }

    @CSVColumnSetter(order=6, header="homeUrl")
    @JsonSetter("homeUrl")
    public void setHomeUrl(String homeUrl) {
        data.setHomeUrl(homeUrl);
    }

    @CSVColumnGetter(order=7, header="receiveAlarmEmails")
    @JsonGetter("receiveAlarmEmails")
    public AlarmLevels getReceiveAlarmEmails() {
        return this.data.getReceiveAlarmEmails();
    }

    @CSVColumnSetter(order=7, header="receiveAlarmEmails")
    @JsonSetter("receiveAlarmEmails")
    public void setReceiveAlarmEmails(AlarmLevels level) {
        data.setReceiveAlarmEmails(level);
    }

    @CSVColumnGetter(order=8, header="timezone")
    @JsonGetter("timezone")
    public String getTimezone() {
        return data.getTimezone();
    }

    @CSVColumnSetter(order=8, header="timezone")
    @JsonSetter("timezone")
    public void setTimezone(String zone) {
        data.setTimezone(zone);
    }

    @CSVColumnGetter(order=9, header="systemTimezone")
    @JsonGetter("systemTimezone")
    public String getSystemTimezone() {
        return TimeZone.getDefault().getID();
    }

    @CSVColumnSetter(order=9, header="systemTimezone")
    @JsonSetter("systemTimezone")
    public void setSystemTimezone(String zone) {
        // no op
    }

    @CSVColumnGetter(order=10, header="muted")
    @JsonGetter("muted")
    public Boolean getMuted() {
        return data.isMuted();
    }

    @CSVColumnSetter(order=10, header="muted")
    @JsonSetter("muted")
    public void setMuted(Boolean muted) {
        data.setMuted(muted);
    }

    @CSVColumnGetter(order=11, header="admin")
    @JsonGetter("admin")
    public Boolean isAdmin() {
        return data.isAdmin();
    }

    @CSVColumnGetter(order=12, header="receiveOwnAuditEvents")
    @JsonGetter("receiveOwnAuditEvents")
    public Boolean getReceiveOwnAuditEvents() {
        return data.isReceiveOwnAuditEvents();
    }

    @CSVColumnSetter(order=12, header="receiveOwnAuditEvents")
    @JsonSetter("receiveOwnAuditEvents")
    public void setReceiveOwnAuditEvents(Boolean receiveOwnAuditEvents) {
        data.setReceiveOwnAuditEvents(receiveOwnAuditEvents);
    }

    @CSVColumnGetter(order=13, header="name")
    @JsonGetter("name")
    public String getName() {
        return data.getName();
    }

    @CSVColumnSetter(order=13, header="name")
    @JsonSetter("name")
    public void setName(String name) {
        data.setName(name);
    }

    @CSVColumnGetter(order=14, header="locale")
    @JsonGetter("locale")
    public String getLocale() {
        return data.getLocale();
    }

    @CSVColumnSetter(order=14, header="locale")
    @JsonSetter("locale")
    public void setLocale(String locale) {
        data.setLocale(locale);
    }

    @CSVColumnGetter(order=15, header="systemLocale")
    @JsonGetter("systemLocale")
    public String getSystemLocale() {
        return Common.getLocale().toLanguageTag();
    }

    @CSVColumnSetter(order=15, header="systemLocale")
    @JsonSetter("systemLocale")
    public void setSystemLocale(String locale) {
        // no op
    }

    @CSVColumnGetter(order=16, header="id")
    @JsonGetter("id")
    public int getId() {
        return data.getId();
    }

    @CSVColumnSetter(order=16, header="id")
    @JsonSetter("id")
    public void setId(int id) {
        // no op
    }

    @CSVColumnGetter(order=17, header="passwordLocked")
    @JsonGetter("passwordLocked")
    public boolean isPasswordLocked() {
        return data.isPasswordLocked();
    }

    @CSVColumnSetter(order=17, header="passwordLocked")
    @JsonSetter("passwordLocked")
    public void setPasswordLocked(boolean passwordLocked) {
        // no op
    }

    @CSVColumnGetter(order=18, header="hashAlgorithm")
    @JsonIgnore
    public String getHashAlgorithmForCSV() {
        // just going to return empty for the CSV, field wont be visible in JSON
        return "";
    }

    @CSVColumnSetter(order=18, header="hashAlgorithm")
    @JsonSetter("hashAlgorithm")
    public void setHashAlgorithm(String hashAlgorithm) {
        // only set the hash algorithm if it its not empty, otherwise just keep default of PLAINTEXT
        if (hashAlgorithm != null && !hashAlgorithm.isEmpty()) {
            this.hashAlgorithm = hashAlgorithm;
            String password = this.password != null ? this.password : "";
            data.setPasswordHash(this.hashAlgorithm, password);
        }
    }

    public Date getLastLogin() {
        long lastLogin = data.getLastLogin();

        // 0 means user has never logged in
        if (lastLogin == 0) {
            return null;
        }

        return new Date(lastLogin);
    }

    public Date getLastPasswordChange() {
        return new Date(data.getPasswordChangeTimestamp());
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
            if(this.messages == null)
                this.messages = new ArrayList<RestValidationMessage>();
            //Add our messages to the list
            for(ProcessMessage message : validation.getMessages()){
                this.messages.add(new RestValidationMessage(
                        message.getContextualMessage(),
                        RestMessageLevel.ERROR,
                        message.getContextKey()
                        ));
            }
            return false;
        }else{
            return true; //Validated ok
        }
    }

    public void addValidationMessage(ProcessMessage message){
        if(this.messages == null)
            this.messages = new ArrayList<RestValidationMessage>();
        this.messages.add((new RestValidationMessage(
                message.getContextualMessage(),
                RestMessageLevel.ERROR,
                message.getContextKey()
                )));
    }
}
