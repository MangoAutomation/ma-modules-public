/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.IOException;
import java.util.List;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.ChangeComparable;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkVO implements ChangeComparable<PointLinkVO>, JsonSerializable {
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
    }

    @Override
    public void addProperties(List<TranslatableMessage> list) {
        DataPointDao dataPointDao = new DataPointDao();
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "pointLinks.source", dataPointDao.getExtendedPointName(sourcePointId));
        AuditEventType.addPropertyMessage(list, "pointLinks.target", dataPointDao.getExtendedPointName(targetPointId));
        AuditEventType.addPropertyMessage(list, "pointLinks.script", script);
        AuditEventType.addExportCodeMessage(list, "pointLinks.event", EVENT_CODES, event);
        AuditEventType.addPropertyMessage(list, "pointLinks.writeAnnotation", writeAnnotation);
        AuditEventType.addPropertyMessage(list, "common.disabled", disabled);
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, PointLinkVO from) {
        DataPointDao dataPointDao = new DataPointDao();
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointLinks.source",
                        dataPointDao.getExtendedPointName(from.sourcePointId),
                        dataPointDao.getExtendedPointName(sourcePointId));
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointLinks.target",
                        dataPointDao.getExtendedPointName(from.targetPointId),
                        dataPointDao.getExtendedPointName(targetPointId));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointLinks.script", from.script, script);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointLinks.event", EVENT_CODES, from.event, event);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointLinks.writeAnnotation", from.writeAnnotation,
                writeAnnotation);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.disabled", from.disabled, disabled);
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
    }
}
