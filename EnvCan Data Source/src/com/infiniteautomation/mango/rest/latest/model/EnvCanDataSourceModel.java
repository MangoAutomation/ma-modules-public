/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.Date;

import com.infiniteautomation.mango.rest.v2.model.datasource.AbstractPollingDataSourceModel;
import com.serotonin.m2m2.envcan.EnvCanDataSourceDefinition;
import com.serotonin.m2m2.envcan.EnvCanDataSourceVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=EnvCanDataSourceDefinition.DATA_SOURCE_TYPE, parent=EnvCanDataSourceModel.class)
public class EnvCanDataSourceModel extends AbstractPollingDataSourceModel<EnvCanDataSourceVO>{
    
    private int stationId;
    private Date dataStartTime;
    
    public EnvCanDataSourceModel() {

    }
    
    public EnvCanDataSourceModel(EnvCanDataSourceVO data) {
        fromVO(data);
    }

    @Override
    public String getModelType() {
        return EnvCanDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public EnvCanDataSourceVO toVO() {
        EnvCanDataSourceVO vo = super.toVO();
        vo.setStationId(stationId);
        if(dataStartTime != null)
            vo.setDataStartTime(dataStartTime.getTime());
        return vo;
    }
    
    @Override
    public void fromVO(EnvCanDataSourceVO vo) {
        super.fromVO(vo);
        this.stationId = vo.getStationId();
        this.dataStartTime = new Date(vo.getDataStartTime());
    }
    
    /**
     * @return the stationId
     */
    public int getStationId() {
        return stationId;
    }

    /**
     * @param stationId the stationId to set
     */
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    /**
     * @return the dataStartTime
     */
    public Date getDataStartTime() {
        return dataStartTime;
    }

    /**
     * @param dataStartTime the dataStartTime to set
     */
    public void setDataStartTime(Date dataStartTime) {
        this.dataStartTime = dataStartTime;
    }
    
}
