/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractPollingDataSourceModel;
import com.serotonin.m2m2.internal.InternalDataSourceDefinition;
import com.serotonin.m2m2.internal.InternalDataSourceVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=InternalDataSourceDefinition.DATA_SOURCE_TYPE, parent=InternalDataSourceModel.class)
public class InternalDataSourceModel extends AbstractPollingDataSourceModel<InternalDataSourceVO>{
    
    private String createPointsPattern;
    
    public InternalDataSourceModel() {

    }
    
    public InternalDataSourceModel(InternalDataSourceVO data) {
        fromVO(data);
    }

    @Override
    public String getModelType() {
        return InternalDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public InternalDataSourceVO toVO() {
        InternalDataSourceVO vo = super.toVO();
        vo.setCreatePointsPattern(createPointsPattern);
        return vo;
    }
    
    @Override
    public void fromVO(InternalDataSourceVO vo) {
        super.fromVO(vo);
        this.createPointsPattern = vo.getCreatePointsPattern();
    }

    /**
     * @return the createPointsPattern
     */
    public String getCreatePointsPattern() {
        return createPointsPattern;
    }

    /**
     * @param createPointsPattern the createPointsPattern to set
     */
    public void setCreatePointsPattern(String createPointsPattern) {
        this.createPointsPattern = createPointsPattern;
    }

    
    
}
