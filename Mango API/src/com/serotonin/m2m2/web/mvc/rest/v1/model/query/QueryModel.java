/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infiniteautomation.mango.db.query.QueryComparison;
import com.infiniteautomation.mango.db.query.SortOption;

/**
 * @author Terry Packer
 *
 */
public class QueryModel {
	
	@JsonProperty
	private List<QueryComparison> orComparisons;
	@JsonProperty
	private List<QueryComparison> andComparisons;
	@JsonProperty
	private List<SortOption> sort;
	@JsonProperty
	private Integer offset;
	@JsonProperty
	private Integer limit;
	
	/**
	 * @param query
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param useOr
	 */
	public QueryModel(List<QueryComparison> orComparisons, List<QueryComparison> andComparisons, List<SortOption> sort,
			Integer offset, Integer limit) {
		super();
		this.orComparisons = orComparisons;
		this.andComparisons = andComparisons;
		this.sort = sort;
		this.offset = offset;
		this.limit = limit;
	}
	
	public QueryModel(){
		this.limit = 100;
		this.orComparisons = new ArrayList<QueryComparison>();
		this.andComparisons = new ArrayList<QueryComparison>();
	}

	public List<QueryComparison> getOrComparisons() {
		return orComparisons;
	}

	public void setOrComparisons(List<QueryComparison> orComparisons) {
		this.orComparisons = orComparisons;
	}
	
	public List<QueryComparison> getAndComparisons() {
		return andComparisons;
	}

	public void setAndComparisons(List<QueryComparison> andComparisons) {
		this.andComparisons = andComparisons;
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

	public List<QueryComparison> getAllComparisons(){
		List<QueryComparison> all = new ArrayList<QueryComparison>(this.orComparisons.size() + this.andComparisons.size());
		all.addAll(this.orComparisons);
		all.addAll(this.andComparisons);
		return all;
	}
	
}
