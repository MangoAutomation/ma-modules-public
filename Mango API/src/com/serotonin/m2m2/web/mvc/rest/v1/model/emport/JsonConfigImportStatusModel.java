package com.serotonin.m2m2.web.mvc.rest.v1.model.emport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
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

	/**
	 * Strip the messages from the result of an ImportBackgroundTask
	 * @param result
	 */
	public void updateMessages(ProcessResult result){
		Translations translations = Common.getTranslations();
		
		for(ProcessMessage message : result.getMessages()){
			if(StringUtils.isEmpty(message.getContextKey())){
				//Generic Message
				this.genericMessages.add(message.getGenericMessage().translate(translations));
			}else{
				switch(message.getLevel()){
				case info:
					this.validationMessages.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.INFORMATION, message.getContextKey()));
					break;
				case warning:
					this.validationMessages.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.WARNING, message.getContextKey()));
					break;
				case error:
					this.validationMessages.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.ERROR, message.getContextKey()));
					break;
				}
			}
		}
	}
	
}
