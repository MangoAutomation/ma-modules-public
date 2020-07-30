/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class EventQueryByMaintenanceCriteria {

    @ApiModelProperty("xids of data sources linked to maintenance events, one of points or sources required")
    private String[] dataSourceXids;
    @ApiModelProperty("xids of data points linked to maintenance events, one of points or sources required")
    private String[] dataPointXids;

    @ApiModelProperty("If present will query events for this active state")
    private Boolean active;
    @ApiModelProperty("Order the event query results one of [asc,desc,null]")
    private String order;
    @ApiModelProperty("Limit the event query results")
    private Integer limit;

    public String[] getDataSourceXids() {
        return dataSourceXids;
    }
    public void setDataSourceXids(String[] dataSourceXids) {
        this.dataSourceXids = dataSourceXids;
    }
    public String[] getDataPointXids() {
        return dataPointXids;
    }
    public void setDataPointXids(String[] dataPointXids) {
        this.dataPointXids = dataPointXids;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public String getOrder() {
        return order;
    }
    public void setOrder(String order) {
        this.order = order;
    }
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }



}
