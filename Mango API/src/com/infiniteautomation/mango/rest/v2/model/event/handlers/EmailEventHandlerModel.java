/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.infiniteautomation.mango.rest.v2.model.mailingList.AddressEntryModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.EmailRecipientModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListEntryModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.UserEntryModel;
import com.infiniteautomation.mango.rest.v2.script.ScriptContextVariableModel;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.EmailEventHandlerDefinition;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;
import com.serotonin.m2m2.vo.mailingList.AddressEntry;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.UserEntry;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="EMAIL", parent=AbstractEventHandlerModel.class)
@JsonTypeName("EMAIL")
public class EmailEventHandlerModel extends AbstractEventHandlerModel {

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
    
    public EmailEventHandlerModel() { }

    public EmailEventHandlerModel(EmailEventHandlerVO vo) {
        super(vo);
    }
    
    @Override
    public AbstractEventHandlerVO<?> toVO() {
        EmailEventHandlerVO vo = (EmailEventHandlerVO) super.toVO();
        if(activeRecipients != null) {
            List<RecipientListEntryBean> beans = new ArrayList<>();
            for(EmailRecipientModel model : activeRecipients)
                beans.add(model.toBean());
            vo.setActiveRecipients(beans);
        }
        vo.setSendEscalation(sendEscalation);
        vo.setRepeatEscalations(repeatEscalations);
        vo.setEscalationDelayType(Common.TIME_PERIOD_CODES.getId(escalationDelayType));
        vo.setEscalationDelay(escalationDelay);
        
        if(escalationRecipients != null) {
            List<RecipientListEntryBean> beans = new ArrayList<>();
            for(EmailRecipientModel model : escalationRecipients)
                beans.add(model.toBean());
            vo.setEscalationRecipients(beans);
        }
        vo.setSendInactive(sendInactive);
        vo.setInactiveOverride(inactiveOverride);
        if(inactiveRecipients != null) {
            List<RecipientListEntryBean> beans = new ArrayList<>();
            for(EmailRecipientModel model : inactiveRecipients)
                beans.add(model.toBean());
            vo.setInactiveRecipients(beans);
        }
        
        vo.setIncludeSystemInfo(includeSystemInfo);
        if(includePointValueCount != null)
            vo.setIncludePointValueCount(includePointValueCount);
        
        vo.setIncludeLogfile(includeLogfile);
        vo.setCustomTemplate(customTemplate);
        
        vo.setScript(script);
        if(scriptPermissions != null) {
            ScriptPermissions permissions = new ScriptPermissions();
            String permissionsString = Permissions.implodePermissionGroups(scriptPermissions);
            permissions.setDataSourcePermissions(permissionsString);
            permissions.setDataPointSetPermissions(permissionsString);
            permissions.setDataPointReadPermissions(permissionsString);
            vo.setScriptPermissions(permissions);
        }
        
        if(scriptContext != null) {
            List<IntStringPair> additionalContext = new ArrayList<>();
            for(ScriptContextVariableModel var : scriptContext) {
                Integer id = DataPointDao.getInstance().getIdByXid(var.getXid());
                if(id != null) {
                    additionalContext.add(new IntStringPair(id, var.getVariableName()));
                }
            }
            vo.setAdditionalContext(additionalContext);
        }
        
        return vo;
    }
    
    @Override
    public void fromVO(AbstractEventHandlerVO<?> vo) {
        super.fromVO(vo);
        EmailEventHandlerVO handler = (EmailEventHandlerVO)vo;
        
        if(handler.getActiveRecipients() != null) {
            this.activeRecipients = new ArrayList<>();
            for(RecipientListEntryBean bean : handler.getActiveRecipients()) {
                switch(bean.getRecipientType()) {
                    case EmailRecipient.TYPE_ADDRESS:
                        activeRecipients.add(new AddressEntryModel((AddressEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_USER:
                        activeRecipients.add(new UserEntryModel((UserEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_MAILING_LIST:
                        activeRecipients.add(new MailingListEntryModel((MailingList)bean.createEmailRecipient()));
                    default:
                        throw new ShouldNeverHappenException("Unsupported recipient type: " + bean.createEmailRecipient().getRecipientType());
                            
                }
            }
        }
        
        this.sendEscalation = handler.isSendEscalation();
        this.escalationDelayType = Common.TIME_PERIOD_CODES.getCode(handler.getEscalationDelayType());
        this.repeatEscalations = handler.isRepeatEscalations();
        this.escalationDelay = handler.getEscalationDelay();
        
        if(handler.getEscalationRecipients() != null) {
            this.escalationRecipients = new ArrayList<>();
            for(RecipientListEntryBean bean : handler.getActiveRecipients()) {
                switch(bean.getRecipientType()) {
                    case EmailRecipient.TYPE_ADDRESS:
                        escalationRecipients.add(new AddressEntryModel((AddressEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_USER:
                        escalationRecipients.add(new UserEntryModel((UserEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_MAILING_LIST:
                        escalationRecipients.add(new MailingListEntryModel((MailingList)bean.createEmailRecipient()));
                    default:
                        throw new ShouldNeverHappenException("Unsupported recipient type: " + bean.createEmailRecipient().getRecipientType());
                            
                }
            }
        }
        
        this.sendInactive = handler.isSendInactive();
        this.inactiveOverride = handler.isInactiveOverride();

        if(handler.getInactiveRecipients() != null) {
            this.inactiveRecipients = new ArrayList<>();
            for(RecipientListEntryBean bean : handler.getActiveRecipients()) {
                switch(bean.getRecipientType()) {
                    case EmailRecipient.TYPE_ADDRESS:
                        inactiveRecipients.add(new AddressEntryModel((AddressEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_USER:
                        inactiveRecipients.add(new UserEntryModel((UserEntry) bean.createEmailRecipient()));
                        break;
                    case EmailRecipient.TYPE_MAILING_LIST:
                        inactiveRecipients.add(new MailingListEntryModel((MailingList)bean.createEmailRecipient()));
                    default:
                        throw new ShouldNeverHappenException("Unsupported recipient type: " + bean.createEmailRecipient().getRecipientType());
                            
                }
            }
        }
        this.includeSystemInfo = handler.isIncludeSystemInfo();
        this.includePointValueCount = handler.getIncludePointValueCount() == 0 ? null : handler.getIncludePointValueCount();
        this.includeLogfile = handler.isIncludeLogfile();
        this.customTemplate = handler.getCustomTemplate();
        
        this.script = handler.getScript();

        if(handler.getScriptPermissions() != null) {
            this.scriptPermissions = handler.getScriptPermissions().getPermissionsSet();
        }
        if(handler.getAdditionalContext() != null) {
            this.scriptContext = new ArrayList<>();
            for(IntStringPair var : handler.getAdditionalContext()) {
                String xid = DataPointDao.getInstance().getXidById(var.getKey());
                if(xid != null) {
                    this.scriptContext.add(new ScriptContextVariableModel(xid, var.getValue()));
                }
            }
        }
    }
    
    @Override
    protected AbstractEventHandlerVO<?> newVO() {
        EmailEventHandlerVO handler = new EmailEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(EmailEventHandlerDefinition.TYPE_NAME));
        return handler;
    }

}
