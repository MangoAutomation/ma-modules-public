/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class EventQueryByMaintenanceEventRql {

    @ApiModelProperty("RQL to query for Maintenance events")
    private String maintenanceEventsRql;

    @ApiModelProperty("If present will query events for this active state")
    private Boolean active;
    @ApiModelProperty("Order the event query results one of [asc,desc,null]")
    private String order;
    @ApiModelProperty("Limit the event query results")
    private Integer limit;

    public String getMaintenanceEventsRql() {
        return maintenanceEventsRql;
    }
    public void setMaintenanceEventsRql(String maintenanceEventsRql) {
        this.maintenanceEventsRql = maintenanceEventsRql;
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
