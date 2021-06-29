/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

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
