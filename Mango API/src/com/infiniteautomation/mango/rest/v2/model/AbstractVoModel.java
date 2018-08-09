/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.serotonin.m2m2.vo.AbstractVO;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractVoModel<VO extends AbstractVO<?>> {
    
    //Used to track if the incoming model specifically chose to set to null
    @JsonIgnore
    protected Set<String> nullSettersCalled = new HashSet<>();
    
    @PatchableField
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer id;
    @PatchableField
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String xid;
    @PatchableField
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String name;
    
    protected AbstractVoModel() {
        fromVO(newVO());
    }
    protected AbstractVoModel(VO vo) {
        fromVO(vo);
    }
    
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
    public VO toVO() {
        VO vo = newVO();
        vo.setId(id);
        vo.setXid(xid);
        vo.setName(name);
        return vo;
    }
    
    /**
     * Used to update this model with any non-null values from the update, using all Fields,
     *   to disable a Field use the @PatchableField(enabled=false) annotation
     *   to allow null values that were set into the model use  @PatchableField(allowNull=true)
     * @param update
     */

    public void patch(AbstractVoModel<VO> update) {
        
        //Search the update for non-null annotations up to and including this class
        Class<?> c = update.getClass();
        while(c != Object.class) {
            for(Field f : c.getDeclaredFields()) {
                PatchableField i = f.getAnnotation(PatchableField.class);
                if(i != null && !i.enabled())
                    continue;
                f.setAccessible(true);
                try {
                    Object value = f.get(update);
                    if(value != null)
                        f.set(this, value);
                    else if(i != null && i.allowNull() && update.wasSetToNull(f.getName()))
                        f.set(this, value);

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ServerErrorException(e);
                }
            }
            c = c.getSuperclass();
        }
    }
    
    public void setNullSettersCalled(Set<String> setters) {
        this.nullSettersCalled = setters;
    }
    
    protected boolean wasSetToNull(String fieldName) {
        if(this.nullSettersCalled.contains(fieldName.toLowerCase()))
            return true;
        else
            return false;
    }

    /**
     * Create a new empty VO to fill with values from the model
     * @return
     */
    protected abstract VO newVO();
}
