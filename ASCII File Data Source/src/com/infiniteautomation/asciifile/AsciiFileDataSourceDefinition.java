package com.infiniteautomation.asciifile;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.web.AsciiFileEditDwr;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileDataSourceDefinition extends DataSourceDefinition{

	@Override
	public String getDataSourceTypeName() {
		return "ASCII FILE";
	}

	@Override
	public String getDescriptionKey() {
		return "dsEdit.file.desc";
	}

	@Override
	protected DataSourceVO<?> createDataSourceVO() {
		return new AsciiFileDataSourceVO();
	}

	@Override
	public String getEditPagePath() {
		return "web/editFile.jsp";
	}

	@Override
	public Class<?> getDwrClass() {
		return AsciiFileEditDwr.class;
	}

}
