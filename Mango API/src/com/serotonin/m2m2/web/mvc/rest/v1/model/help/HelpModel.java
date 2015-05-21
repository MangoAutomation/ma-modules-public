/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.help;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class HelpModel {

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String title;
	
	@JsonProperty
	private String content;
	
	@JsonProperty
	private List<RelatedHelpItemModel> relatedList;
	
	public HelpModel() {}

	/**
	 * @param id
	 * @param title
	 * @param content
	 * @param relatedList
	 */
	public HelpModel(String id, String title, String content,
			List<RelatedHelpItemModel> relatedList) {
		super();
		this.id = id;
		this.title = title;
		this.content = content;
		this.relatedList = relatedList;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<RelatedHelpItemModel> getRelatedList() {
		return relatedList;
	}

	public void setRelatedList(List<RelatedHelpItemModel> relatedList) {
		this.relatedList = relatedList;
	}
	
}
