/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

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

    protected TimePeriod pollPeriod;

    public AbstractPollingDataSourceModel() {
        super();
    }
    
    public AbstractPollingDataSourceModel(T vo) {
        super(vo);
    }
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.pollPeriod = new TimePeriod(vo.getUpdatePeriods(), 
                TimePeriodType.convertTo(vo.getUpdatePeriodType()));
    }
    
    @Override
    public T toVO() {
        T vo = super.toVO();
        if(pollPeriod != null) {
            vo.setUpdatePeriods(pollPeriod.getPeriods());
            vo.setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
        }
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
}
