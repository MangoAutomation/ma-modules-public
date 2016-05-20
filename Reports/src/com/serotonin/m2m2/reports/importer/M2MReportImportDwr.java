/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import com.serotonin.json.JsonWriter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Terry Packer
 *
 */
public class M2MReportImportDwr extends ModuleDwr {
	
	
    @DwrPermission(admin = true)
    public ProcessResult migrate(String driverClassname, String connectionUrl, String username, String password){
    	ProcessResult result = new ProcessResult();
    	
    	//Validate the connection
    	try{
    		DriverManager.registerDriver((Driver) Class.forName(driverClassname).newInstance());
        	Connection connection = DriverManager.getConnection(connectionUrl, username, password);
        	connection.setAutoCommit(false);
        	// Test the connection.
        	DatabaseMetaData md = connection.getMetaData();
        	
        	String productName = md.getDatabaseProductName();
        	DatabaseType type = DatabaseType.DERBY;
        	if(productName.equalsIgnoreCase("mysql")){
        		type = DatabaseType.MYSQL;
        	}else if(productName.equalsIgnoreCase("Apache Derby")){
        		type = DatabaseType.DERBY;
        	}else if(productName.contains("Microsoft")){
        		type = DatabaseType.MSSQL;
        	}else if(productName.equalsIgnoreCase("h2")){
        		type = DatabaseType.H2;
        	}else if(productName.equalsIgnoreCase("postgressql")){
        		type = DatabaseType.MYSQL;
        	}
        	
        	//Get the reports
        	M2MReportDao dao = new M2MReportDao(connection, type);
        	List<M2MReportVO> legacyReports = dao.getReports();
        	
        	//Convert the reports to our VOs
        	List<ReportVO> reports = new ArrayList<ReportVO>();
        	for(M2MReportVO legacyReport: legacyReports){
        		try{
        			ReportVO report = legacyReport.convert(dao);
        			report.validate(result);
        			reports.add(report);
        			
        		}catch(Exception e){
            		result.addGenericMessage("common.default", e.getMessage());
        		}
        	}
        	
        	if(!result.getHasMessages()){
        		ReportDao reportDao = ReportDao.instance;
        		for(ReportVO vo : reports){
        			vo.validate(result);
        			reportDao.saveReport(vo);
        		}
        	}
        	
        	result.addData("reports", reports);
        	
    	}catch(Exception e){
    		result.addContextualMessage("connectionUrl", "common.default", e.getMessage());
    		return result;
    	}
    	
    	
    	return result;
    }

    @DwrPermission(admin = true)
    public ProcessResult generateJson(String driverClassname, String connectionUrl, String username, String password){
    	ProcessResult result = new ProcessResult();
    	
    	//Validate the connection
    	try{
    		DriverManager.registerDriver((Driver) Class.forName(driverClassname).newInstance());
        	Connection connection = DriverManager.getConnection(connectionUrl, username, password);
        	connection.setAutoCommit(false);
        	// Test the connection.
        	DatabaseMetaData md = connection.getMetaData();
        	
        	String productName = md.getDatabaseProductName();
        	DatabaseType type = DatabaseType.DERBY;
        	if(productName.equalsIgnoreCase("mysql")){
        		type = DatabaseType.MYSQL;
        	}else if(productName.equalsIgnoreCase("Apache Derby")){
        		type = DatabaseType.DERBY;
        	}else if(productName.contains("Microsoft")){
        		type = DatabaseType.MSSQL;
        	}else if(productName.equalsIgnoreCase("h2")){
        		type = DatabaseType.H2;
        	}else if(productName.equalsIgnoreCase("postgressql")){
        		type = DatabaseType.MYSQL;
        	}
        	
        	//Get the reports
        	M2MReportDao dao = new M2MReportDao(connection, type);
        	List<M2MReportVO> legacyReports = dao.getReports();
        	
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(Common.JSON_CONTEXT, stringWriter);
            jsonWriter.setPrettyIndent(3);
            jsonWriter.setPrettyOutput(true);
    		
    		int cnt = 0;
    		jsonWriter.append("{");
    		jsonWriter.indent();
    		jsonWriter.quote("reports");
    		jsonWriter.append(": [");
        	//Convert the reports to our VOs
        	for(M2MReportVO legacyReport: legacyReports){
    			legacyReport.jsonWrite(jsonWriter, dao);
    			cnt++;
    			if(cnt < legacyReports.size())
    				jsonWriter.append(",");
        		
        	}
        	jsonWriter.append(']');
        	jsonWriter.append("}");
        	jsonWriter.flush();
        	result.addData("reports", stringWriter.toString());
        	
    	}catch(Exception e){
    		result.addContextualMessage("connectionUrl", "common.default", e.getMessage());
    		return result;
    	}
    	
    	
    	return result;
    }
    
}
