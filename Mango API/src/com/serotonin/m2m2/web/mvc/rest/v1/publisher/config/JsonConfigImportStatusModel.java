package com.serotonin.m2m2.web.mvc.rest.v1.publisher.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;

public class JsonConfigImportStatusModel {

	private JsonConfigImportStateEnum state;
	private float progress;
	private List<RestValidationMessage> validationMessages;
	private List<String> genericMessages;
	
	public JsonConfigImportStatusModel(JsonConfigImportStateEnum state, float progress){
		this.progress = progress;
		this.state = state;
	}
	
	public JsonConfigImportStatusModel(JsonConfigImportStateEnum state, ProcessResult result, float progress){
		this(state, progress);
		this.progress = progress;
		this.validationMessages = new ArrayList<RestValidationMessage>();
		this.genericMessages = new ArrayList<String>();
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
	
}
