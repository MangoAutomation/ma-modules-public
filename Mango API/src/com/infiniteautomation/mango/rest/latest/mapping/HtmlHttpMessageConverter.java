/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;

/**
 * @author Terry Packer
 *
 */
@Component
public class HtmlHttpMessageConverter extends StringHttpMessageConverter{
	public HtmlHttpMessageConverter(){
		this.setSupportedMediaTypes(List.of(MediaType.TEXT_HTML));
	}
}
