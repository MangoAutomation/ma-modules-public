package com.infiniteautomation.datafilesource.dataimage;

public abstract class ImportPoint {
	private final String identifier;
	private final long time;
	
	public enum DataType {ALPHANUMERIC, NUMERIC, MULTISTATE, BINARY};
	
	public ImportPoint(String identifier, long time) {
		this.identifier = identifier;
		this.time = time;
	}

	public String getIdentifier() {
		return this.identifier;
	}
	
	public long getTime() {
		return this.time;
	}
	
	public abstract DataType getDataType();
}
