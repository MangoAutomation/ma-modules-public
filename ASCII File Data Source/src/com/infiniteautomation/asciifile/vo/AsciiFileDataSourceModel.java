/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.asciifile.vo;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractPollingDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class AsciiFileDataSourceModel extends AbstractPollingDataSourceModel<AsciiFileDataSourceVO>{

	public AsciiFileDataSourceModel() {
		super();
	}
	
	/**
	 * @param data
	 */
	public AsciiFileDataSourceModel(AsciiFileDataSourceVO data) {
		super(data);
	}

	@JsonGetter("filePath")
	public String getFilePath() {
	    return this.data.getFilePath();
	}

	@JsonSetter("filePath")
	public void setFilePath(String filePath) {
	    this.data.setFilePath(filePath);
	}

    @Override
    public String getModelType() {
        return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
