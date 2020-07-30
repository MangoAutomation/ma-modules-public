/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event;

import io.swagger.annotations.ApiModelProperty;

/**
 * Container for getting events based on RQL queries on the source type table
 *  and then the events table restricted to the sources found in the first query
 * @author Terry Packer
 */
public class EventQueryBySourceType {

    @ApiModelProperty("Source type, valid options: DATA_POINT, DATA_SOURCE")
    private String sourceType;
    @ApiModelProperty("RQL to query for sources to filter event query")
    private String sourceRql;
    @ApiModelProperty("RQL to query events")
    private String eventsRql;

    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    public String getSourceRql() {
        return sourceRql;
    }
    public void setSourceRql(String sourceRql) {
        this.sourceRql = sourceRql;
    }
    public String getEventsRql() {
        return eventsRql;
    }
    public void setEventsRql(String eventsRql) {
        this.eventsRql = eventsRql;
    }
}
