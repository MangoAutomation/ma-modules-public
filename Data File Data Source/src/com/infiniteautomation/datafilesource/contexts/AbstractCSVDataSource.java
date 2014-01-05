package com.infiniteautomation.datafilesource.contexts;

import java.util.List;
import java.util.ArrayList;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

public abstract class AbstractCSVDataSource {
	protected String[] identifiers;
	protected List<ImportPoint> parsedPoints = new ArrayList<ImportPoint>();
	
	public void setIdentifiers(String[] identifiers) {
		this.identifiers = identifiers;
	}
	
	public List<ImportPoint> getParsedPoints() {
		return this.parsedPoints;
	}
	
	public boolean takesPointIdentifiers() {
		return false;
	}
	
	public abstract void importRow(String[] data, int rowNum);
}
