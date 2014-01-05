package com.infiniteautomation.datafilesource;

import com.infiniteautomation.datafilesource.vo.DataFileDataSourceVO;
import com.infiniteautomation.datafilesource.web.DataFileDataSourceEditDwr;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class DataFileDataSourceDefinition extends DataSourceDefinition{

	@Override
	public String getDataSourceTypeName() {
		return "Data File";
	}

	@Override
	public String getDescriptionKey() {
		return "dsEdit.datafile.desc";
	}

	@Override
	protected DataSourceVO<?> createDataSourceVO() {
		return new DataFileDataSourceVO();
	}

	@Override
	public String getEditPagePath() {
		return "web/datafile.jsp";
	}

	@Override
	public Class<?> getDwrClass() {
		return DataFileDataSourceEditDwr.class;
	}

}