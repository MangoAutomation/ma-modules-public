/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.query;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.db.dao.QueryParameter;
import com.serotonin.m2m2.db.dao.SortOption;

/**
 * @author Terry Packer
 *
 */
public class QueryModel {
	
	@JsonProperty
	private List<QueryParameter> query;
	@JsonProperty
	private List<SortOption> sort;
	@JsonProperty
	private Integer offset;
	@JsonProperty
	private Integer limit;
	@JsonProperty
	private boolean useOr;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param useOr
	 */
	public QueryModel(List<QueryParameter> query, List<SortOption> sort,
			Integer offset, Integer limit, boolean useOr) {
		super();
		this.query = query;
		this.sort = sort;
		this.offset = offset;
		this.limit = limit;
		this.useOr = useOr;
	}
	
	public QueryModel(){
		this.limit = 100;
		this.useOr = false;
	}

	public List<QueryParameter> getQuery() {
		return query;
	}

	public void setQuery(List<QueryParameter> query) {
		this.query = query;
	}

	public List<SortOption> getSort() {
		return sort;
	}

	public void setSort(List<SortOption> sort) {
		this.sort = sort;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public boolean isUseOr() {
		return useOr;
	}

	public void setUseOr(boolean useOr) {
		this.useOr = useOr;
	}
	
}
