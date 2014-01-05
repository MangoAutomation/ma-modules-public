package com.infiniteautomation.datafilesource.dataimage;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

public class BinaryImportPoint extends ImportPoint {
	public final boolean value;
	
	public BinaryImportPoint(String identifier, boolean value, long time) {
		super(identifier, time);
		this.value = value;
	}

	@Override
	public DataType getDataType() {
		return DataType.BINARY;
	}
	
	
}
