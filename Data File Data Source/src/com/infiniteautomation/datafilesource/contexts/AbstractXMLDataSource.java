package com.infiniteautomation.datafilesource.contexts;

import java.util.List;

import com.infiniteautomation.datafilesource.dataimage.ImportPoint;

/*
 * @author Phillip Dunlap
 */

public interface AbstractXMLDataSource {
	public abstract List<ImportPoint> getParsedPoints();
}
