package com.infiniteautomation.serial;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.web.SerialEditDwr;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class SerialDataSourceDefinition extends DataSourceDefinition{
	
	public static final String DATA_SOURCE_TYPE = "SERIAL";

	@Override
	public String getDataSourceTypeName() {
		return DATA_SOURCE_TYPE;
	}

	@Override
	public String getDescriptionKey() {
		return "dsEdit.serial.desc";
	}

	@Override
	protected DataSourceVO<?> createDataSourceVO() {
		return new SerialDataSourceVO();
	}

	@Override
	public String getEditPagePath() {
		return "web/editSerial.jsp";
	}

	@Override
	public Class<?> getDwrClass() {
		return SerialEditDwr.class;
	}

}
