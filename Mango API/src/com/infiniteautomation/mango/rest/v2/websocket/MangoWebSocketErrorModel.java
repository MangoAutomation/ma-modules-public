/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class MangoWebSocketErrorModel {

	@JsonProperty("type")
	private MangoWebSocketErrorType type;
	
	@JsonProperty("message")
	private String message;
	
	public MangoWebSocketErrorModel(MangoWebSocketErrorType type, String message){
		this.type = type;
		this.message = message;
	}
	
	public MangoWebSocketErrorModel(){
		
	}
	
	

	public MangoWebSocketErrorType getType() {
		return type;
	}

	public void setType(MangoWebSocketErrorType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	
}
