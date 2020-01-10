/**
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.audit;


import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonTypeReader;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;

/**
 * @author Terry Packer
 *
 */
public class AuditEventInstanceModel {

    private String typeName;
    private AlarmLevels alarmLevel;
    private int userId;
    private String username;
    private String changeType;
    private int objectId;
    private long timestamp;
    private TranslatableMessage message;
    @JsonRawValue
    private String context;

    public AuditEventInstanceModel(AuditEventInstanceVO vo) {
        fromVO(vo);
    }

    public AuditEventInstanceModel() { }

    public void fromVO(AuditEventInstanceVO vo) {
        this.typeName = vo.getTypeName();
        this.alarmLevel = vo.getAlarmLevel();
        this.userId = vo.getUserId();
        this.username = UserDao.getInstance().getXidById(userId);
        this.changeType = AuditEventInstanceVO.CHANGE_TYPE_CODES.getCode(vo.getChangeType());
        this.objectId = vo.getObjectId();
        this.timestamp = vo.getTimestamp();
        this.message = vo.getMessage();
        this.context = convertContext(vo.getContext());
    }

    public AuditEventInstanceVO toVO() {
        AuditEventInstanceVO vo = new AuditEventInstanceVO();
        vo.setTypeName(typeName);
        vo.setAlarmLevel(alarmLevel);
        vo.setUserId(userId);
        vo.setChangeType(AuditEventInstanceVO.CHANGE_TYPE_CODES.getId(changeType));
        vo.setObjectId(objectId);
        vo.setTimestamp(timestamp);
        vo.setMessage(message);
        vo.setContext(unwrapContext(context));
        return vo;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public TranslatableMessage getMessage() {
        return message;
    }

    public void setMessage(TranslatableMessage message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    private JsonObject unwrapContext(String json) {

        try {
            JsonTypeReader reader = new JsonTypeReader(json);
            JsonValue value = reader.read();
            return value.toJsonObject();
        }catch(Exception e) {
            throw new BadRequestException(e);
        }

    }

    private String convertContext(JsonObject context) {
        //Since the JsonData table can contain JSON within the context, return raw JSON all the time here
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(Common.JSON_CONTEXT, stringWriter);
        writer.setPrettyIndent(3);
        writer.setPrettyOutput(true);
        try {
            writer.writeObject(context);
            return stringWriter.toString();
        }
        catch (JsonException e) {
            throw new ShouldNeverHappenException(e);
        }
        catch (IOException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

}
