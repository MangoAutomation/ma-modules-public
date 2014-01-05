package com.infiniteautomation.datafilesource.dataimage;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

public class NumericImportPoint extends ImportPoint{
	
	public final double value;

	public NumericImportPoint(String identifier, double value, long time) {
		super(identifier, time);
		this.value = value;
	}

	@Override
	public DataType getDataType() {
		return DataType.NUMERIC;
	}

}
