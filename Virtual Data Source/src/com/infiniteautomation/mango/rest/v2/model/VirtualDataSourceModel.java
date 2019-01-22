/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.infiniteautomation.mango.rest.v2.model.datasource.AbstractPollingDataSourceModel;
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=VirtualDataSourceDefinition.TYPE_NAME, parent=AbstractPollingDataSourceModel.class)
public class VirtualDataSourceModel extends AbstractPollingDataSourceModel<VirtualDataSourceVO>{

    private boolean polling;
    
    public VirtualDataSourceModel() {
        super();
    }
    
    public VirtualDataSourceModel(VirtualDataSourceVO data) {
        super(data);
    }

    @Override
    public String getModelType() {
        return VirtualDataSourceDefinition.TYPE_NAME;
    }
    
    @Override
    public VirtualDataSourceVO toVO() {
        VirtualDataSourceVO vo = super.toVO();
        vo.setPolling(polling);
        return vo;
    }
    
    @Override
    public void fromVO(VirtualDataSourceVO vo) {
        super.fromVO(vo);
        this.polling = vo.isPolling();
    }

    /**
     * @return the polling
     */
    public boolean isPolling() {
        return polling;
    }
    /**
     * @param polling the polling to set
     */
    public void setPolling(boolean polling) {
        this.polling = polling;
    }
}
