package com.infiniteautomation.datafilesource.dataimage;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

public class MultistateImportPoint extends ImportPoint {
	public final int value;
	
	public MultistateImportPoint(String identifier, int value, long time) {
		super(identifier, time);
		this.value = value;
	}

	@Override
	public DataType getDataType() {
		return DataType.MULTISTATE;
	}
}
