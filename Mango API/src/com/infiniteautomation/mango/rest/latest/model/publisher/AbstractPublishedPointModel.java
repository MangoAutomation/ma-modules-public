/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.publisher;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractPublishedPointModel.MODEL_TYPE)
public abstract class AbstractPublishedPointModel<T extends PublishedPointVO> {
    public static final String MODEL_TYPE = "modelType";

    protected Integer id;
    protected String xid;
    protected String name;
    protected boolean enabled;
    protected String dataPointXid;
    protected String publisherXid;
    protected Map<String, String> dataPointTags;

    /**
     * Return the TYPE_NAME for the point's model
     */
    public abstract String getModelType();

    @ApiModelProperty(value ="ID of object in database")
    public Integer getId(){
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @ApiModelProperty(value = "XID of object", required = false)
    public String getXid(){
        return xid;
    }

    public void setXid(String xid){
        this.xid = xid;
    }

    @ApiModelProperty(value = "Name of object", required = false)
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @ApiModelProperty(value ="Is the point enabled")
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDataPointXid() {
        return dataPointXid;
    }
    
    public void setDataPointXid(String dataPointXid) {
        this.dataPointXid = dataPointXid;
    }

    public String getPublisherXid() {
        return publisherXid;
    }

    public void setPublisherXid(String publisherXid) {
        this.publisherXid = publisherXid;
    }

    @ApiModelProperty(value ="Tags for source data point, including name and device tags")
    public Map<String, String> getDataPointTags() {
        return dataPointTags;
    }

    public void setDataPointTags(Map<String, String> dataPointTags) {
        this.dataPointTags = dataPointTags;
    }
}
