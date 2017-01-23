/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.audit;

import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractBasicVoModel;

/**
 * @author Terry Packer
 *
 */
public class AuditEventInstanceModel extends AbstractBasicVoModel<AuditEventInstanceVO>{

	/**
	 * @param data
	 */
	public AuditEventInstanceModel(AuditEventInstanceVO data) {
		super(data);
	}
	
	public AuditEventInstanceModel(){
		super(new AuditEventInstanceVO());
	}

	public String getTypeName() {
		return this.data.getTypeName();
	}
	public void setTypeName(String typeName) {
		this.data.setTypeName(typeName);
	}
	public String getAlarmLevel() {
		return AlarmLevels.CODES.getCode(this.data.getAlarmLevel());
	}
	public void setAlarmLevel(String alarmLevel) {
		this.data.setAlarmLevel(AlarmLevels.CODES.getId(alarmLevel));
	}
	public String getUsername() {
		User u = UserDao.instance.get(this.data.getUserId());
		if(u != null)
			return u.getUsername();
		else
			return null;
	}
	public void setUsername(String username) {
		User u = UserDao.instance.getUser(username);
		if(u != null)
			this.data.setUserId(u.getId());
	}
	public String getChangeType() {
		return AuditEventInstanceVO.CHANGE_TYPE_CODES.getCode(this.data.getChangeType());
	}
	public void setChangeType(String changeType) {
		this.data.setChangeType(AuditEventInstanceVO.CHANGE_TYPE_CODES.getId(changeType));
	}
	public int getObjectId() {
		return this.data.getObjectId();
	}
	public void setObjectId(int objectId) {
		this.data.setObjectId(objectId);
	}
	public long getTimestamp() {
		return this.data.getTimestamp();
	}
	public void setTimestamp(long timestamp) {
		this.data.setTimestamp(timestamp);
	}
	public JsonObject getContext() {
		return this.data.getContext();
	}
	public void setContext(JsonObject context) {
		this.data.setContext(context);
	}
	public String getMessage() {
		TranslatableMessage m = this.data.getMessage();
		if(m != null)
			return m.translate(Common.getTranslations());
		else
			return null;
	}
	public void setMessage(String message) {
		this.data.setMessage(new TranslatableMessage("common.default", message));
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return AuditEventInstanceModelDefinition.TYPE_NAME;
	}

}
