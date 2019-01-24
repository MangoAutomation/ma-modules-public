/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.infiniteautomation.mango.rest.v2.model.datasource.AbstractDataSourceModel;
import com.serotonin.m2m2.vmstat.VMStatDataSourceDefinition;
import com.serotonin.m2m2.vmstat.VMStatDataSourceVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=VMStatDataSourceDefinition.DATA_SOURCE_TYPE, parent=AbstractDataSourceModel.class)
public class VMStatDataSourceModel extends AbstractDataSourceModel<VMStatDataSourceVO>{

    private int pollSeconds;
    private String outputScale;
    
    public VMStatDataSourceModel() {
        super();
    }
    
    public VMStatDataSourceModel(VMStatDataSourceVO data) {
        super(data);
    }

    @Override
    public String getModelType() {
        return VMStatDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public VMStatDataSourceVO toVO() {
        VMStatDataSourceVO vo = super.toVO();
        vo.setPollSeconds(pollSeconds);
        vo.setOutputScale(VMStatDataSourceVO.OUTPUT_SCALE_CODES.getId(outputScale));
        return vo;
    }
    
    @Override
    public void fromVO(VMStatDataSourceVO vo) {
        super.fromVO(vo);
        this.pollSeconds = vo.getPollSeconds();
        this.outputScale = VMStatDataSourceVO.OUTPUT_SCALE_CODES.getCode(vo.getOutputScale());
    }

    /**
     * @return the pollSeconds
     */
    public int getPollSeconds() {
        return pollSeconds;
    }

    /**
     * @param pollSeconds the pollSeconds to set
     */
    public void setPollSeconds(int pollSeconds) {
        this.pollSeconds = pollSeconds;
    }

    /**
     * @return the outputScale
     */
    public String getOutputScale() {
        return outputScale;
    }

    /**
     * @param outputScale the outputScale to set
     */
    public void setOutputScale(String outputScale) {
        this.outputScale = outputScale;
    }
}
