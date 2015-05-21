/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.help;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class RelatedHelpItemModel {
	
	@JsonProperty
	private String id;
	@JsonProperty
	private String title;

	public RelatedHelpItemModel() {}
	

	/**
	 * @param id
	 * @param title
	 */
	public RelatedHelpItemModel(String id, String title) {
		super();
		this.id = id;
		this.title = title;
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
}
