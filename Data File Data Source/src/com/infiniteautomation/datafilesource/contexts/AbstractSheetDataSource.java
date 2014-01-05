package com.infiniteautomation.datafilesource.contexts;

import java.util.List;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;
import com.serotonin.m2m2.vo.emport.AbstractSheetEmporter;

/*
 * @author Phillip Dunlap
 */

//Contains declarations and base methods that enable the data file data source to perform the point setting
// internal to the data source as opposed to through the sheet emporter.
public abstract class AbstractSheetDataSource extends AbstractSheetEmporter {
	//An easily read data structure class to standardize the action post-import.
	protected List<ImportPoint> parsedPoints;
	protected String[] headers;
	
	@Override
	protected String[] getHeaders() {
		return headers;
	}
	
	public void setHeaders(String[] headers) {
		this.headers = headers;
	}
	
	public List<ImportPoint> getParsedPoints() {
		return parsedPoints;
	}
	
	//Determines whether the data source loops through its points and makes a header array or not
	// IF AN EMPORTER ANSWERS TRUE IT MUST ALSO IMPLEMENT THE SET HEADERS METHOD
	public boolean takesPointHeaders() {
		return false;
	}
}
