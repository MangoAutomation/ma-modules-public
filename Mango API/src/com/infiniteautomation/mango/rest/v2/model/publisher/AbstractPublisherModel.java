/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.publisher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PublisherDefinition;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractPublisherModel.MODEL_TYPE)
public class AbstractPublisherModel<T extends PublishedPointVO> extends AbstractVoModel<PublisherVO<T>>{
    
    public static final String MODEL_TYPE = "modelType";

    @JsonIgnore
    protected PublisherDefinition definition;
    
    @ApiModelProperty("Read only description of publisher connection")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage connectionDescription;

    @ApiModelProperty("Read only description of publisher type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage description;

    @Override
    protected PublisherVO<T> newVO() {
        // TODO Auto-generated method stub
        return null;
    }

}
