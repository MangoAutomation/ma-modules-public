/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.script.ScriptError;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkVO extends AbstractVO<PointLinkVO> {

	private static final long serialVersionUID = 1L;

	public static final String XID_PREFIX = "PL_";

    public static final int EVENT_UPDATE = 1;
    public static final int EVENT_CHANGE = 2;
    public static final int EVENT_LOGGED = 3;

    public static ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(EVENT_UPDATE, "UPDATE", "dsEdit.pointEvent.update");
        EVENT_CODES.addElement(EVENT_CHANGE, "CHANGE", "dsEdit.pointEvent.change");
        EVENT_CODES.addElement(EVENT_LOGGED, "LOGGED", "dsEdit.pointEvent.logged");
    }

    private int sourcePointId;
    private int targetPointId;
    @JsonProperty
    private String script;
    private int event;
    @JsonProperty
    private boolean writeAnnotation;
    @JsonProperty
    private boolean disabled;
    private int logLevel = ScriptLog.LogLevel.NONE;
    private ScriptPermissions scriptPermissions = new ScriptPermissions();
    @JsonProperty
    private float logSize = 1.0f;
    @JsonProperty
    private int logCount = 5;

    public int getSourcePointId() {
        return sourcePointId;
    }

    public void setSourcePointId(int sourcePointId) {
        this.sourcePointId = sourcePointId;
    }

    public int getTargetPointId() {
        return targetPointId;
    }

    public void setTargetPointId(int targetPointId) {
        this.targetPointId = targetPointId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public boolean isWriteAnnotation() {
        return writeAnnotation;
    }

    public void setWriteAnnotation(boolean writeAnnotation) {
        this.writeAnnotation = writeAnnotation;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }
    
    public float getLogSize() {
        return logSize;
    }
    
    public void setLogSize(float logSize) {
        this.logSize = logSize;
    }

    public int getLogCount() {
        return logCount;
    }
    
    public void setLogCount(int logCount) {
        this.logCount = logCount;
    }
    
    public ScriptPermissions getScriptPermissions() {
		return scriptPermissions;
	}

	public void setScriptPermissions(ScriptPermissions scriptPermissions) {
		this.scriptPermissions = scriptPermissions;
	}
	
	@Override
    public String getTypeKey() {
        return "event.audit.pointLink";
    }

    public void validate(ProcessResult response) {
        super.validate(response);
        if (sourcePointId == 0)
            response.addContextualMessage("sourcePointId", "pointLinks.validate.sourceRequired");
        if (targetPointId == 0)
            response.addContextualMessage("targetPointId", "pointLinks.validate.targetRequired");
        if (sourcePointId == targetPointId)
            response.addContextualMessage("targetPointId", "pointLinks.validate.samePoint");
        this.scriptPermissions.validate(response, Common.getHttpUser());
        if(!StringUtils.isEmpty(script) && !response.getHasMessages()) {
            try {
                Common.getBean(MangoJavaScriptService.class).compile(script, true, scriptPermissions);
            } catch(ScriptError e) {
                response.addContextualMessage("script", "pointLinks.validate.scriptError", e.getMessage());
            }
        }
        if (!ScriptLog.LOG_LEVEL_CODES.isValidId(logLevel))
            response.addContextualMessage("logLevel", "validate.invalidValue");
        if (logSize <= 0)
            response.addContextualMessage("logSize", "validate.greaterThanZero");
        if (logCount <= 0)
            response.addContextualMessage("logCount", "validate.greaterThanZero");
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        DataPointDao dataPointDao = DataPointDao.getInstance();

        writer.writeEntry("xid", xid);

        DataPointVO dp = dataPointDao.getDataPoint(sourcePointId, false);
        if (dp != null)
            writer.writeEntry("sourcePointId", dp.getXid());

        dp = dataPointDao.getDataPoint(targetPointId, false);
        if (dp != null)
            writer.writeEntry("targetPointId", dp.getXid());

        writer.writeEntry("event", EVENT_CODES.getCode(event));
        writer.writeEntry("logLevel", ScriptLog.LOG_LEVEL_CODES.getCode(logLevel));
        writer.writeEntry("scriptPermissions", scriptPermissions == null ? null : scriptPermissions.getPermissions());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        DataPointDao dataPointDao = DataPointDao.getInstance();

        String xid = jsonObject.getString("sourcePointId");
        if (xid != null) {
            DataPointVO vo = dataPointDao.getDataPoint(xid);
            if (vo == null)
                throw new TranslatableJsonException("emport.error.missingPoint", xid);
            sourcePointId = vo.getId();
        }

        xid = jsonObject.getString("targetPointId");
        if (xid != null) {
            DataPointVO vo = dataPointDao.getDataPoint(xid);
            if (vo == null)
                throw new TranslatableJsonException("emport.error.missingPoint", xid);
            targetPointId = vo.getId();
        }

        String text = jsonObject.getString("event");
        if (text != null) {
            event = EVENT_CODES.getId(text);
            if (!EVENT_CODES.isValidId(event))
                throw new TranslatableJsonException("emport.error.link.invalid", "event", text,
                        EVENT_CODES.getCodeList());
        }
        text = jsonObject.getString("logLevel");
        if (text != null) {
            logLevel = ScriptLog.LOG_LEVEL_CODES.getId(text);
            if (logLevel == -1)
                throw new TranslatableJsonException("emport.error.invalid", "logLevel", text,
                		ScriptLog.LOG_LEVEL_CODES.getCodeList());
        }else{
        	logLevel = ScriptLog.LogLevel.NONE;
        }
        if(jsonObject.containsKey("scriptPermissions")) {
            Set<String> permissions = null;
            try{
                JsonObject o = jsonObject.getJsonObject("scriptPermissions");
                permissions = new HashSet<>();
                permissions.addAll(Permissions.explodePermissionGroups(o.getString("dataSourcePermissions")));
                permissions.addAll(Permissions.explodePermissionGroups(o.getString("dataPointSetPermissions")));
                permissions.addAll(Permissions.explodePermissionGroups(o.getString("dataPointReadPermissions")));
                permissions.addAll(Permissions.explodePermissionGroups(o.getString("customPermissions")));
                this.scriptPermissions = new ScriptPermissions(permissions);
            }catch(ClassCastException e) {
               //Munchy munch, not a legacy script permissions object 
            }
            if(permissions == null) {
                this.scriptPermissions = new ScriptPermissions(Permissions.explodePermissionGroups(jsonObject.getString("scriptPermissions")));
            }
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.AbstractVO#getDao()
	 */
	@Override
	protected AbstractDao<PointLinkVO> getDao() {
		return PointLinkDao.getInstance();
	}
}

