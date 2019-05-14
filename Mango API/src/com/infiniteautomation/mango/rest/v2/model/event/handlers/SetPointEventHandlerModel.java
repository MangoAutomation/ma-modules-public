/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptModel.ScriptContextVariableModel;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.SetPointEventHandlerDefinition;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.event.SetPointEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=SetPointEventHandlerDefinition.TYPE_NAME, parent=AbstractEventHandlerModel.class)
@JsonTypeName(SetPointEventHandlerDefinition.TYPE_NAME)
public class SetPointEventHandlerModel extends AbstractEventHandlerModel<SetPointEventHandlerVO> {

    private String targetPointXid;
    
    private String activeAction;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object activeValueToSet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String activePointXid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String activeScript;
    
    private String inactiveAction;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object inactiveValueToSet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inactivePointXid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inactiveScript;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<String> scriptPermissions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ScriptContextVariableModel> scriptContext;
    
    public SetPointEventHandlerModel() { }

    public SetPointEventHandlerModel(SetPointEventHandlerVO vo) {
        fromVO(vo);
    }
    
    @Override
    public String getHandlerType() {
        return SetPointEventHandlerDefinition.TYPE_NAME;
    }
    
    /**
     * @return the targetPointXid
     */
    public String getTargetPointXid() {
        return targetPointXid;
    }

    /**
     * @param targetPointXid the targetPointXid to set
     */
    public void setTargetPointXid(String targetPointXid) {
        this.targetPointXid = targetPointXid;
    }

    /**
     * @return the activeAction
     */
    public String getActiveAction() {
        return activeAction;
    }

    /**
     * @param activeAction the activeAction to set
     */
    public void setActiveAction(String activeAction) {
        this.activeAction = activeAction;
    }

    /**
     * @return the activeValueToSet
     */
    public Object getActiveValueToSet() {
        return activeValueToSet;
    }

    /**
     * @param activeValueToSet the activeValueToSet to set
     */
    public void setActiveValueToSet(Object activeValueToSet) {
        this.activeValueToSet = activeValueToSet;
    }

    /**
     * @return the activePointXid
     */
    public String getActivePointXid() {
        return activePointXid;
    }

    /**
     * @param activePointXid the activePointXid to set
     */
    public void setActivePointXid(String activePointXid) {
        this.activePointXid = activePointXid;
    }

    /**
     * @return the activeScript
     */
    public String getActiveScript() {
        return activeScript;
    }

    /**
     * @param activeScript the activeScript to set
     */
    public void setActiveScript(String activeScript) {
        this.activeScript = activeScript;
    }

    /**
     * @return the inactiveAction
     */
    public String getInactiveAction() {
        return inactiveAction;
    }

    /**
     * @param inactiveAction the inactiveAction to set
     */
    public void setInactiveAction(String inactiveAction) {
        this.inactiveAction = inactiveAction;
    }

    /**
     * @return the inactiveValueToSet
     */
    public Object getInactiveValueToSet() {
        return inactiveValueToSet;
    }

    /**
     * @param inactiveValueToSet the inactiveValueToSet to set
     */
    public void setInactiveValueToSet(Object inactiveValueToSet) {
        this.inactiveValueToSet = inactiveValueToSet;
    }

    /**
     * @return the inactivePointXid
     */
    public String getInactivePointXid() {
        return inactivePointXid;
    }

    /**
     * @param inactivePointXid the inactivePointXid to set
     */
    public void setInactivePointXid(String inactivePointXid) {
        this.inactivePointXid = inactivePointXid;
    }

    /**
     * @return the inactiveScript
     */
    public String getInactiveScript() {
        return inactiveScript;
    }

    /**
     * @param inactiveScript the inactiveScript to set
     */
    public void setInactiveScript(String inactiveScript) {
        this.inactiveScript = inactiveScript;
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

    @Override
    public SetPointEventHandlerVO toVO() {
        SetPointEventHandlerVO vo = (SetPointEventHandlerVO)super.toVO();
        Integer targetId = DataPointDao.getInstance().getIdByXid(targetPointXid);
        if(targetId != null)
            vo.setTargetPointId(targetId);
        vo.setActiveAction(SetPointEventHandlerVO.SET_ACTION_CODES.getId(activeAction));
        if(activeValueToSet != null)
            vo.setActiveValueToSet(activeValueToSet.toString());
        if(activePointXid != null) {
            Integer activePointId = DataPointDao.getInstance().getIdByXid(activePointXid);
            if(activePointId != null)
                vo.setActivePointId(activePointId);
        }
        vo.setActiveScript(activeScript);
        
        vo.setInactiveAction(SetPointEventHandlerVO.SET_ACTION_CODES.getId(inactiveAction));
        if(inactiveValueToSet != null)
            vo.setInactiveValueToSet(inactiveValueToSet.toString());
        if(inactivePointXid != null) {
            Integer inactivePointId = DataPointDao.getInstance().getIdByXid(inactivePointXid);
            if(inactivePointId != null)
                vo.setInactivePointId(inactivePointId);
        }
        vo.setInactiveScript(inactiveScript);
        ScriptPermissions permissions = new ScriptPermissions(scriptPermissions);
        vo.setScriptPermissions(permissions);
        
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
        return vo;
    }
    
    @Override
    public void fromVO(SetPointEventHandlerVO vo) {
        super.fromVO(vo);
        SetPointEventHandlerVO handler = (SetPointEventHandlerVO)vo;
        DataPointVO target = DataPointDao.getInstance().get(handler.getTargetPointId());
        this.activeAction = SetPointEventHandlerVO.SET_ACTION_CODES.getCode(handler.getActiveAction());

        if(target != null) {
            this.targetPointXid = target.getXid();
            if(handler.getActiveAction() == SetPointEventHandlerVO.SET_ACTION_STATIC_VALUE) {
                DataValue value = DataValue.stringToValue(handler.getActiveValueToSet(), target.getPointLocator().getDataTypeId());
                this.activeValueToSet = value.getObjectValue();
            }
            if(handler.getInactiveAction() == SetPointEventHandlerVO.SET_ACTION_STATIC_VALUE) {
                DataValue value = DataValue.stringToValue(handler.getInactiveValueToSet(), target.getPointLocator().getDataTypeId());
                this.inactiveValueToSet = value.getObjectValue();
            }
        }
        
        if(handler.getActiveAction() == SetPointEventHandlerVO.SET_ACTION_POINT_VALUE)
            this.activePointXid = DataPointDao.getInstance().getXidById(handler.getActivePointId());
        if(handler.getInactiveAction() == SetPointEventHandlerVO.SET_ACTION_POINT_VALUE)
            this.inactivePointXid = DataPointDao.getInstance().getXidById(handler.getInactivePointId());
        
        this.inactiveAction = SetPointEventHandlerVO.SET_ACTION_CODES.getCode(handler.getInactiveAction());
        this.activeScript = handler.getActiveScript();
        this.inactiveScript = handler.getInactiveScript();

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
    protected SetPointEventHandlerVO newVO() {
        SetPointEventHandlerVO handler = new SetPointEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(SetPointEventHandlerDefinition.TYPE_NAME));
        return handler;
    }

}
