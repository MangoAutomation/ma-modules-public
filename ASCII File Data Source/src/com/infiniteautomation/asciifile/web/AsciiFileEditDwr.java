package com.infiniteautomation.asciifile.web;

import java.io.File;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileEditDwr extends DataSourceEditDwr{

	   @DwrPermission(user = true)
	    public ProcessResult saveFileDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType, String filePath) {
	        AsciiFileDataSourceVO ds = (AsciiFileDataSourceVO) Common.getUser().getEditDataSource();

	        setBasicProps(ds, basic);
	        ds.setUpdatePeriods(updatePeriods);
	        ds.setUpdatePeriodType(updatePeriodType);
	        ds.setFilePath(filePath);
	        
	        return tryDataSourceSave(ds);
	    }	
	
	    @DwrPermission(user = true)
	    public ProcessResult savePointLocator(int id, String xid, String name, AsciiFilePointLocatorVO locator) {
	        return validatePoint(id, xid, name, locator, null);
	    }
	    
	    @DwrPermission(user = true)
	    public boolean checkIsFileReadable(String path) {
	    	File verify = new File(path);
	    	if(verify.exists())
	    		return verify.canRead();
	    	return false;
	    }
}
