package com.serotonin.m2m2.web.mvc.rest.v1.model.emport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.MangoRestTemporaryResourceModel;

public class JsonConfigImportStatusModel extends MangoRestTemporaryResourceModel{

	private String owner;
	private JsonConfigImportStateEnum state;
	private float progress;
	private Date start;
	private Date finish;
	private List<RestValidationMessage> validationMessages;
	private List<String> genericMessages;
	
	public JsonConfigImportStatusModel(String resourceId, String username, Date start){
		super(resourceId);
		this.owner = username;
		this.start = start;
		this.progress = 0f;
		this.state = JsonConfigImportStateEnum.RUNNING;
		this.validationMessages = new ArrayList<RestValidationMessage>();
		this.genericMessages = new ArrayList<String>();
	}
	
	public JsonConfigImportStatusModel(String resourceId, String username, Date start, Date finish, 
			float progress, JsonConfigImportStateEnum state, 
			List<RestValidationMessage> validation,
			List<String> generic){
		super(resourceId);
		this.owner = username;
		this.start = start;
		this.progress = progress;
		this.state = state;
		this.validationMessages = validation;
		this.genericMessages = generic;
	}

	public JsonConfigImportStatusModel(){
		this.validationMessages = new ArrayList<RestValidationMessage>();
		this.genericMessages = new ArrayList<String>();
	}
	
	public JsonConfigImportStateEnum getState() {
		return state;
	}

	public void setState(JsonConfigImportStateEnum state) {
		this.state = state;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getFinish() {
		return finish;
	}

	public void setFinish(Date finish) {
		this.finish = finish;
	}

	public List<RestValidationMessage> getValidationMessages() {
		return validationMessages;
	}

	public void setValidationMessages(List<RestValidationMessage> validationMessages) {
		this.validationMessages = validationMessages;
	}

	public List<String> getGenericMessages() {
		return genericMessages;
	}

	public void setGenericMessages(List<String> genericMessages) {
		this.genericMessages = genericMessages;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
