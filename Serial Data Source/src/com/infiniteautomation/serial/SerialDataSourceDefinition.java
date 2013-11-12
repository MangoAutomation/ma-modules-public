package com.infiniteautomation.serial;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.web.SerialEditDwr;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class SerialDataSourceDefinition extends DataSourceDefinition{

	@Override
	public String getDataSourceTypeName() {
		return "SERIAL";
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
