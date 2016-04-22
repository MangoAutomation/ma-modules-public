/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkVO extends AbstractVO<PointLinkVO> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String XID_PREFIX = "PL_";

    public static final int EVENT_UPDATE = 1;
    public static final int EVENT_CHANGE = 2;

    public static ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(EVENT_UPDATE, "UPDATE", "pointLinks.event.update");
        EVENT_CODES.addElement(EVENT_CHANGE, "CHANGE", "pointLinks.event.change");
    }

    private int id = Common.NEW_ID;
    private String xid;
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
    @JsonProperty
    private ScriptPermissions scriptPermissions = new ScriptPermissions(Common.getUser());

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

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
        if (sourcePointId == 0)
            response.addContextualMessage("sourcePointId", "pointLinks.validate.sourceRequired");
        if (targetPointId == 0)
            response.addContextualMessage("targetPointId", "pointLinks.validate.targetRequired");
        if (sourcePointId == targetPointId)
            response.addContextualMessage("targetPointId", "pointLinks.validate.samePoint");
        this.scriptPermissions.validate(response, Common.getUser());
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        DataPointDao dataPointDao = new DataPointDao();

        writer.writeEntry("xid", xid);

        DataPointVO dp = dataPointDao.getDataPoint(sourcePointId);
        if (dp != null)
            writer.writeEntry("sourcePointId", dp.getXid());

        dp = dataPointDao.getDataPoint(targetPointId);
        if (dp != null)
            writer.writeEntry("targetPointId", dp.getXid());

        writer.writeEntry("event", EVENT_CODES.getCode(event));
        writer.writeEntry("logLevel", ScriptLog.LOG_LEVEL_CODES.getCode(logLevel));

    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        DataPointDao dataPointDao = new DataPointDao();

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

    }
}
