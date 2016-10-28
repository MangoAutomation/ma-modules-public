package com.infiniteautomation.asciifile.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.infiniteautomation.asciifile.rt.AsciiFileDataSourceRT;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.infiniteautomation.mango.regex.MatchCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileEditDwr extends DataSourceEditDwr {

	@DwrPermission(user = true)
	public ProcessResult saveFileDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType,
			String filePath) {
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
		if (verify.exists())
			return verify.canRead();
		return false;
	}
	
    @DwrPermission(user = true)
    public ProcessResult testString(int dsId, String raw) {
    	final ProcessResult pr = new ProcessResult();
    	
    	//Message we will work with
    	String msg;

    	if(dsId == -1) {
    		pr.addContextualMessage("testString", "dsEdit.file.test.needsSave");
    		return pr;
    	}
    	
    	
    	msg = StringEscapeUtils.unescapeJava(raw);
    	
		//Map to store the values vs the points they are for
    	final List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		pr.addData("results", results);
    	
    	DataPointDao dpd = new DataPointDao();
    	List<DataPointVO> points = dpd.getDataPoints(dsId, null);

    	
		for(final DataPointVO vo : points){
			final Map<String, Object> result = new HashMap<String,Object>();
			MatchCallback callback = new MatchCallback(){
	
				@Override
				public void onMatch(String pointIdentifier, PointValueTime value) {
					result.put("success", "true");
					result.put("name", vo.getName());
					result.put("identifier", pointIdentifier);
					result.put("value", value != null ? value.toString() : "null");
				}
	
				@Override
				public void pointPatternMismatch(String message, String pointValueRegex) {
					result.put("success", "false");
					result.put("name", vo.getName());
					result.put("error", new TranslatableMessage("dsEdit.file.test.noPointRegexMatch").translate(Common.getTranslations()));
				}
	
				@Override
				public void messagePatternMismatch(String message, String messageRegex) { }
	
				@Override
				public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
					result.put("success", "false");
					result.put("name", vo.getName());
					result.put("error", new TranslatableMessage("dsEdit.file.test.noIdentifierFound").translate(Common.getTranslations()));

				}
	
				@Override
				public void matchGeneralFailure(Exception e) {
					result.put("success", "false");
					result.put("name", vo.getName());
					result.put("error", new TranslatableMessage("common.default", e.getMessage()).translate(Common.getTranslations()));
				}
			};
			AsciiFilePointLocatorVO locator = vo.getPointLocator();
			AsciiFileDataSourceRT.matchPointValueTime(msg, 
					Pattern.compile(locator.getValueRegex()), 
					locator.getPointIdentifier(),
					locator.getPointIdentifierIndex(),
					locator.getDataTypeId(),
					locator.getValueIndex(),
					locator.getHasTimestamp(),
					locator.getTimestampIndex(), 
					locator.getTimestampFormat(), callback);
			if(result.size() > 0){
				results.add(result);
			}
		
		}
    	return pr;
    }
}
