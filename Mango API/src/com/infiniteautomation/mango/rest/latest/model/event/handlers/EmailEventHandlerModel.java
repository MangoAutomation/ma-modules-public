/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptModel.ScriptContextVariableModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.EmailRecipientModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.EmailEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;
import com.serotonin.m2m2.vo.role.Role;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=EmailEventHandlerDefinition.TYPE_NAME, parent=AbstractEventHandlerModel.class)
@JsonTypeName(EmailEventHandlerDefinition.TYPE_NAME)
public class EmailEventHandlerModel extends AbstractEventHandlerModel<EmailEventHandlerVO> {

    private List<EmailRecipientModel> activeRecipients;
    private boolean sendEscalation;
    private boolean repeatEscalations;
    private String escalationDelayType;
    private int escalationDelay;
    private List<EmailRecipientModel> escalationRecipients;
    private boolean sendInactive;
    private boolean inactiveOverride;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EmailRecipientModel> inactiveRecipients;
    private boolean includeSystemInfo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer includePointValueCount;
    private boolean includeLogfile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String customTemplate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ScriptContextVariableModel> scriptContext;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<String> scriptPermissions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String script;
    private String subject;

    public EmailEventHandlerModel() { }

    public EmailEventHandlerModel(EmailEventHandlerVO vo) {
        fromVO(vo);
    }

    @Override
    public String getHandlerType() {
        return EmailEventHandlerDefinition.TYPE_NAME;
    }

    /**
     * @return the activeRecipients
     */
    public List<EmailRecipientModel> getActiveRecipients() {
        return activeRecipients;
    }

    /**
     * @param activeRecipients the activeRecipients to set
     */
    public void setActiveRecipients(List<EmailRecipientModel> activeRecipients) {
        this.activeRecipients = activeRecipients;
    }

    /**
     * @return the sendEscalation
     */
    public boolean isSendEscalation() {
        return sendEscalation;
    }

    /**
     * @param sendEscalation the sendEscalation to set
     */
    public void setSendEscalation(boolean sendEscalation) {
        this.sendEscalation = sendEscalation;
    }

    /**
     * @return the repeatEscalations
     */
    public boolean isRepeatEscalations() {
        return repeatEscalations;
    }

    /**
     * @param repeatEscalations the repeatEscalations to set
     */
    public void setRepeatEscalations(boolean repeatEscalations) {
        this.repeatEscalations = repeatEscalations;
    }

    /**
     * @return the escalationDelayType
     */
    public String getEscalationDelayType() {
        return escalationDelayType;
    }

    /**
     * @param escalationDelayType the escalationDelayType to set
     */
    public void setEscalationDelayType(String escalationDelayType) {
        this.escalationDelayType = escalationDelayType;
    }

    /**
     * @return the escalationDelay
     */
    public int getEscalationDelay() {
        return escalationDelay;
    }

    /**
     * @param escalationDelay the escalationDelay to set
     */
    public void setEscalationDelay(int escalationDelay) {
        this.escalationDelay = escalationDelay;
    }

    /**
     * @return the escalationRecipients
     */
    public List<EmailRecipientModel> getEscalationRecipients() {
        return escalationRecipients;
    }

    /**
     * @param escalationRecipients the escalationRecipients to set
     */
    public void setEscalationRecipients(List<EmailRecipientModel> escalationRecipients) {
        this.escalationRecipients = escalationRecipients;
    }

    /**
     * @return the sendInactive
     */
    public boolean isSendInactive() {
        return sendInactive;
    }

    /**
     * @param sendInactive the sendInactive to set
     */
    public void setSendInactive(boolean sendInactive) {
        this.sendInactive = sendInactive;
    }

    /**
     * @return the inactiveOverride
     */
    public boolean isInactiveOverride() {
        return inactiveOverride;
    }

    /**
     * @param inactiveOverride the inactiveOverride to set
     */
    public void setInactiveOverride(boolean inactiveOverride) {
        this.inactiveOverride = inactiveOverride;
    }

    /**
     * @return the inactiveRecipients
     */
    public List<EmailRecipientModel> getInactiveRecipients() {
        return inactiveRecipients;
    }

    /**
     * @param inactiveRecipients the inactiveRecipients to set
     */
    public void setInactiveRecipients(List<EmailRecipientModel> inactiveRecipients) {
        this.inactiveRecipients = inactiveRecipients;
    }

    /**
     * @return the includeSystemInfo
     */
    public boolean isIncludeSystemInfo() {
        return includeSystemInfo;
    }

    /**
     * @param includeSystemInfo the includeSystemInfo to set
     */
    public void setIncludeSystemInfo(boolean includeSystemInfo) {
        this.includeSystemInfo = includeSystemInfo;
    }

    /**
     * @return the includePointValueCount
     */
    public Integer getIncludePointValueCount() {
        return includePointValueCount;
    }

    /**
     * @param includePointValueCount the includePointValueCount to set
     */
    public void setIncludePointValueCount(Integer includePointValueCount) {
        this.includePointValueCount = includePointValueCount;
    }

    /**
     * @return the includeLogfile
     */
    public boolean isIncludeLogfile() {
        return includeLogfile;
    }

    /**
     * @param includeLogfile the includeLogfile to set
     */
    public void setIncludeLogfile(boolean includeLogfile) {
        this.includeLogfile = includeLogfile;
    }

    /**
     * @return the customTemplate
     */
    public String getCustomTemplate() {
        return customTemplate;
    }

    /**
     * @param customTemplate the customTemplate to set
     */
    public void setCustomTemplate(String customTemplate) {
        this.customTemplate = customTemplate;
    }

    /**
     * @return the scriptContext
     */
    public List<ScriptContextVariableModel> getScriptContext() {
        return scriptContext;
    }

    /**
     * @param scriptContext the scriptContext to set
     */
    public void setScriptContext(List<ScriptContextVariableModel> scriptContext) {
        this.scriptContext = scriptContext;
    }

    /**
     * @return the scriptPermissions
     */
    public Set<String> getScriptPermissions() {
        return scriptPermissions;
    }

    /**
     * @param scriptPermissions the scriptPermissions to set
     */
    public void setScriptPermissions(Set<String> scriptPermissions) {
        this.scriptPermissions = scriptPermissions;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public EmailEventHandlerVO toVO() {
        EmailEventHandlerVO vo = super.toVO();
        if(activeRecipients != null) {
            List<MailingListRecipient> beans = new ArrayList<>();
            for(EmailRecipientModel model : activeRecipients)
                beans.add(model.fromModel());
            vo.setActiveRecipients(beans);
        }
        vo.setSendEscalation(sendEscalation);
        vo.setRepeatEscalations(repeatEscalations);
        vo.setEscalationDelayType(Common.TIME_PERIOD_CODES.getId(escalationDelayType));
        vo.setEscalationDelay(escalationDelay);

        if(escalationRecipients != null) {
            List<MailingListRecipient> beans = new ArrayList<>();
            for(EmailRecipientModel model : escalationRecipients)
                beans.add(model.fromModel());
            vo.setEscalationRecipients(beans);
        }
        vo.setSendInactive(sendInactive);
        vo.setInactiveOverride(inactiveOverride);
        if(inactiveRecipients != null) {
            List<MailingListRecipient> beans = new ArrayList<>();
            for(EmailRecipientModel model : inactiveRecipients)
                beans.add(model.fromModel());
            vo.setInactiveRecipients(beans);
        }

        vo.setIncludeSystemInfo(includeSystemInfo);
        if(includePointValueCount != null)
            vo.setIncludePointValueCount(includePointValueCount);

        vo.setIncludeLogfile(includeLogfile);
        vo.setCustomTemplate(customTemplate);

        vo.setScript(script);
        PermissionService service = Common.getBean(PermissionService.class);
        vo.setScriptRoles(new ScriptPermissions(service.explodeLegacyPermissionGroupsToRoles(scriptPermissions)));

        if(scriptContext != null) {
            List<IntStringPair> additionalContext = new ArrayList<>();
            for(ScriptContextVariableModel var : scriptContext) {
                Integer id = DataPointDao.getInstance().getIdByXid(var.getXid());
                if(id != null) {
                    additionalContext.add(new IntStringPair(id, var.getVariableName()));
                }else {
                    additionalContext.add(new IntStringPair(Common.NEW_ID, var.getVariableName()));
                }
            }
            vo.setAdditionalContext(additionalContext);
        }

        vo.setSubject(EmailEventHandlerVO.SUBJECT_INCLUDE_CODES.getId(subject));

        return vo;
    }

    @Override
    public void fromVO(EmailEventHandlerVO vo) {
        super.fromVO(vo);

        this.sendEscalation = vo.isSendEscalation();
        this.escalationDelayType = Common.TIME_PERIOD_CODES.getCode(vo.getEscalationDelayType());
        this.repeatEscalations = vo.isRepeatEscalations();
        this.escalationDelay = vo.getEscalationDelay();

        this.sendInactive = vo.isSendInactive();
        this.inactiveOverride = vo.isInactiveOverride();

        this.includeSystemInfo = vo.isIncludeSystemInfo();
        this.includePointValueCount = vo.getIncludePointValueCount() == 0 ? null : vo.getIncludePointValueCount();
        this.includeLogfile = vo.isIncludeLogfile();
        this.customTemplate = vo.getCustomTemplate();

        this.script = vo.getScript();

        if(vo.getScriptRoles() != null) {
            this.scriptPermissions = new HashSet<>();
            for(Role role : vo.getScriptRoles().getAllInheritedRoles()) {
                this.scriptPermissions.add(role.getXid());
            }
        }
        if(vo.getAdditionalContext() != null) {
            this.scriptContext = new ArrayList<>();
            for(IntStringPair var : vo.getAdditionalContext()) {
                String xid = DataPointDao.getInstance().getXidById(var.getKey());
                if(xid != null) {
                    this.scriptContext.add(new ScriptContextVariableModel(xid, var.getValue()));
                }
            }
        }
        this.subject = EmailEventHandlerVO.SUBJECT_INCLUDE_CODES.getCode(vo.getSubject());
    }

    @Override
    protected EmailEventHandlerVO newVO() {
        EmailEventHandlerVO handler = new EmailEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(EmailEventHandlerDefinition.TYPE_NAME));
        return handler;
    }

}
