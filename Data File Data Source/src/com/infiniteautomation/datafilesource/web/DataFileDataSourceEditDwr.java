package com.infiniteautomation.datafilesource.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.infiniteautomation.datafilesource.vo.DataFileDataSourceVO;
import com.infiniteautomation.datafilesource.vo.DataFilePointLocatorVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Phillip Dunlap
 */

public class DataFileDataSourceEditDwr extends DataSourceEditDwr {
	@DwrPermission(user = true)
	public ProcessResult getTemplateList() {
		DataFileDataSourceVO ds = (DataFileDataSourceVO) Common.getUser().getEditDataSource();
		File templateDir = new File(Common.MA_HOME + ModuleRegistry.getModule("dataFile").getDirectoryPath() + "/web/templates/" + ds.getTypeName() + "/");
		ProcessResult pr = new ProcessResult();
		
		List<String> templates = new ArrayList<String>();
		for(File f : templateDir.listFiles()) {
			if(f.isFile() && f.getName().endsWith(".class"))
				templates.add(f.getName().replaceAll("\\.class", ""));
		}
		
		pr.addData("templates", templates);
		pr.addData("selected", ds.getTemplate());
		return pr;
	}
	
    @DwrPermission(user = true)
    public ProcessResult saveFileDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType, int fileType, 
    		String filePath, boolean deleteAfterImport, boolean createPoints, String addedPrefix, String template) {
        DataFileDataSourceVO ds = (DataFileDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setFilePath(filePath);
        ds.setDeleteAfterImport(deleteAfterImport);
        ds.setAddedPrefix(addedPrefix);
        ds.setFileType(fileType);
        ds.setTemplate(template);
        ds.setCreatePoints(createPoints);
        
        return tryDataSourceSave(ds);
    }
    
    @DwrPermission(user = true)
    public ProcessResult savePointLocator(int id, String xid, String name, DataFilePointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
    
    @DwrPermission(user = true)
    public boolean checkDoesFileExist(String path) {
    	File verify = new File(path);
    	if(verify.exists())
    		return true;
    	return false;
    }
}
