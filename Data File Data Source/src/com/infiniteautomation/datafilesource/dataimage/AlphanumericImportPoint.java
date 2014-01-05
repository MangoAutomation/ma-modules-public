package com.infiniteautomation.datafilesource.dataimage;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

public class AlphanumericImportPoint extends ImportPoint {
	
	public final String value;

	public AlphanumericImportPoint(String value, String identifier, long time) {
		super(identifier, time);
		this.value = value;
	}

	@Override
	public DataType getDataType() {
		return DataType.ALPHANUMERIC;
	}

}
