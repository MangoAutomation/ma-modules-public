/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.pointLinks;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.rt.script.ScriptPermissions;

/**
 * @author Terry Packer
 *
 */
public class PointLinkModel extends AbstractVoModel<PointLinkVO> {
    
    
    public PointLinkModel() {
        
    }
    public PointLinkModel(PointLinkVO vo) {
        super(vo);
    }
    
    private String sourcePointXid;
    private String targetPointXid;
    private String script;
    private String event;
    private boolean writeAnnotation;
    private boolean disabled;
    private String logLevel;
    private String scriptPermissions;
    private float logSize;
    private int logCount;
    
    /**
     * @return the sourcePointXid
     */
    public String getSourcePointXid() {
        return sourcePointXid;
    }
    /**
     * @param sourcePointXid the sourcePointXid to set
     */
    public void setSourcePointXid(String sourcePointXid) {
        this.sourcePointXid = sourcePointXid;
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
     * @return the event
     */
    public String getEvent() {
        return event;
    }
    /**
     * @param event the event to set
     */
    public void setEvent(String event) {
        this.event = event;
    }
    /**
     * @return the writeAnnotation
     */
    public boolean isWriteAnnotation() {
        return writeAnnotation;
    }
    /**
     * @param writeAnnotation the writeAnnotation to set
     */
    public void setWriteAnnotation(boolean writeAnnotation) {
        this.writeAnnotation = writeAnnotation;
    }
    /**
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }
    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    /**
     * @return the logLevel
     */
    public String getLogLevel() {
        return logLevel;
    }
    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    /**
     * @return the scriptPermissions
     */
    public String getScriptPermissions() {
        return scriptPermissions;
    }
    /**
     * @param scriptPermissions the scriptPermissions to set
     */
    public void setScriptPermissions(String scriptPermissions) {
        this.scriptPermissions = scriptPermissions;
    }
    /**
     * @return the logSize
     */
    public float getLogSize() {
        return logSize;
    }
    /**
     * @param logSize the logSize to set
     */
    public void setLogSize(float logSize) {
        this.logSize = logSize;
    }
    /**
     * @return the logCount
     */
    public int getLogCount() {
        return logCount;
    }
    /**
     * @param logCount the logCount to set
     */
    public void setLogCount(int logCount) {
        this.logCount = logCount;
    }
    @Override
    public PointLinkVO toVO() {
        PointLinkVO vo = super.toVO();
        vo.setSourcePointId(DataPointDao.getInstance().getIdByXid(sourcePointXid));
        vo.setTargetPointId(DataPointDao.getInstance().getIdByXid(targetPointXid));
        vo.setScript(script);
        vo.setEvent(PointLinkVO.EVENT_CODES.getId(event));
        vo.setWriteAnnotation(writeAnnotation);
        vo.setDisabled(disabled);
        vo.setLogLevel(ScriptLog.LOG_LEVEL_CODES.getId(logLevel));
        ScriptPermissions permissions = new ScriptPermissions();
        permissions.setDataSourcePermissions(scriptPermissions);
        permissions.setDataPointReadPermissions(scriptPermissions);
        permissions.setDataPointSetPermissions(scriptPermissions);
        permissions.setCustomPermissions(scriptPermissions);
        vo.setScriptPermissions(permissions);
        vo.setLogSize(logSize);
        vo.setLogCount(logCount);
        return vo;
    }
    
    @Override
    public void fromVO(PointLinkVO vo) {
        super.fromVO(vo);
        this.sourcePointXid = DataPointDao.getInstance().getXidById(vo.getSourcePointId());
        this.targetPointXid = DataPointDao.getInstance().getXidById(vo.getTargetPointId());
        this.script = vo.getScript();
        this.event = PointLinkVO.EVENT_CODES.getCode(vo.getEvent());
        this.writeAnnotation = vo.isWriteAnnotation();
        this.disabled = vo.isDisabled();
        this.logLevel = ScriptLog.LOG_LEVEL_CODES.getCode(vo.getLogLevel());
        ScriptPermissions permissions = vo.getScriptPermissions();
        if(permissions != null) {
            this.scriptPermissions = permissions.getPermissions();
        }
        this.logSize = vo.getLogSize();
        this.logCount = vo.getLogCount();
    }
    
    @Override
    protected PointLinkVO newVO() {
        return new PointLinkVO();
    }
}
