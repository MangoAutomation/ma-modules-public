/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.AbstractVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractVoModel<VO extends AbstractVO> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String xid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String name;

    protected AbstractVoModel() { }

    @ApiModelProperty(value ="ID of object in database")
    @JsonGetter("id")
    public Integer getId(){
        return id;
    }

    @ApiModelProperty(value = "XID of object", required = false)
    @JsonGetter("xid")
    public String getXid(){
        return xid;
    }

    @JsonSetter("xid")
    public void setXid(String xid){
        this.xid = xid;
    }

    @ApiModelProperty(value = "Name of object", required = false)
    @JsonGetter("name")
    public String getName(){
        return name;
    }

    @JsonSetter("name")
    public void setName(String name){
        this.name = name;
    }

    public void fromVO(VO vo) {
        id = vo.getId();
        xid = vo.getXid();
        name = vo.getName();
    }

    /**
     * Create a vo from our fields
     * @return
     */
    public VO toVO() throws ValidationException {
        VO vo = newVO();
        readInto(vo);
        return vo;
    }

    public void readInto(VO vo) {
        vo.setId(id == null ? Common.NEW_ID : id);
        vo.setXid(xid);
        vo.setName(name);
    }

    /**
     * Create a new empty VO to fill with values from the model
     * @return
     */
    protected abstract VO newVO();

}
