/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.serotonin.m2m2.vo.dataSource.PollingDataSourceVO;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=AbstractDataSourceModel.MODEL_TYPE)
public abstract class AbstractPollingDataSourceModel<T extends PollingDataSourceVO<T>> extends AbstractDataSourceModel<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected TimePeriod pollPeriod;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Boolean quantize;
    
    public AbstractPollingDataSourceModel() {
        super();
    }
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.pollPeriod = new TimePeriod(vo.getUpdatePeriods(), 
                TimePeriodType.convertTo(vo.getUpdatePeriodType()));
        vo.setQuantize(quantize);
    }
    
    @Override
    public T toVO() {
        T vo = super.toVO();
        if(pollPeriod != null) {
            vo.setUpdatePeriods(pollPeriod.getPeriods());
            vo.setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
        }
        quantize = vo.isQuantize();
        return vo;
    }
    
    /**
     * @return the pollPeriod
     */
    public TimePeriod getPollPeriod() {
        return pollPeriod;
    }
    
    /**
     * @param pollPeriod the pollPeriod to set
     */
    public void setPollPeriod(TimePeriod pollPeriod) {
        this.pollPeriod = pollPeriod;
    }

    /**
     * @return the quantize
     */
    public Boolean getQuantize() {
        return quantize;
    }

    /**
     * @param quantize the quantize to set
     */
    public void setQuantize(Boolean quantize) {
        this.quantize = quantize;
    }
}
