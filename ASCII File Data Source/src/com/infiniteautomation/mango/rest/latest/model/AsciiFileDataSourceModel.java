/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractPollingDataSourceModel;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE, parent=AsciiFileDataSourceModel.class)
public class AsciiFileDataSourceModel extends AbstractPollingDataSourceModel<AsciiFileDataSourceVO>{
    
    private String filePath;
    
    public AsciiFileDataSourceModel() {

    }
    
    public AsciiFileDataSourceModel(AsciiFileDataSourceVO data) {
        fromVO(data);
    }

    @Override
    public String getModelType() {
        return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public AsciiFileDataSourceVO toVO() {
        AsciiFileDataSourceVO vo = super.toVO();
        vo.setFilePath(filePath);
        return vo;
    }
    
    @Override
    public void fromVO(AsciiFileDataSourceVO vo) {
        super.fromVO(vo);
        this.filePath = vo.getFilePath();
    }
    
    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }
    
}
