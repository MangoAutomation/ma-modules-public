/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

/**
 * @author Terry Packer
 *
 */
public class ReportEmportDefinition extends EmportDefinition {
    
	public static final String elementId = "reports";

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public String getDescriptionKey() {
        return "header.reports";
    }

    @Override
    public Object getExportData() {
        List<ReportVO> wls = ReportDao.getInstance().getReports();
        return wls;
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext, PermissionHolder importer) throws JsonException {
        JsonObject reportJson = jsonValue.toJsonObject();

        String xid = reportJson.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = ReportDao.getInstance().generateUniqueXid();

        ReportVO report = null; 
        try{
        	report = ReportDao.getInstance().getReport(xid);
        }catch(IncorrectResultSizeDataAccessException e){
        	importContext.getResult().addGenericMessage("reports.emport.duplicateXids", xid);
        	return;
        }
        if (report == null) {
            report = new ReportVO();
            report.setXid(xid);
        }

        try {
            importContext.getReader().readInto(report, reportJson);

            // Now validate it. Use a new response object so we can distinguish errors in this user from other
            // errors.
            ProcessResult reportResponse = new ProcessResult();
            report.validate(reportResponse);
            if (reportResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(reportResponse, "emport.report.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = report.getId() == Common.NEW_ID;
                ReportDao.getInstance().saveReport(report);
                importContext.addSuccessMessage(isnew, "emport.report.prefix", xid);
            }
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.report.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.report.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}

