/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.pointLinks;

import java.util.Set;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.util.log.LogLevel;

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
    private LogLevel logLevel;
    private Set<String> scriptPermissions;
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
    public LogLevel getLogLevel() {
        return logLevel;
    }
    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
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
        vo.setLogLevel(logLevel);
        vo.setScriptPermissions(new ScriptPermissions(scriptPermissions));
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
        this.logLevel = vo.getLogLevel();
        ScriptPermissions permissions = vo.getScriptPermissions();
        if(permissions != null) {
            this.scriptPermissions = permissions.getPermissionsSet();
        }
        this.logSize = vo.getLogSize();
        this.logCount = vo.getLogCount();
    }
    
    @Override
    protected PointLinkVO newVO() {
        return new PointLinkVO();
    }
}
