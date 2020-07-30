/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * @author Terry Packer
 *
 */
public class HtmlHttpMessageConverter extends StringHttpMessageConverter{

	
	public HtmlHttpMessageConverter(){
		ArrayList<MediaType> types = new ArrayList<MediaType>();
		types.add(MediaType.TEXT_HTML);
		this.setSupportedMediaTypes(types);
	}
}
