/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.script.ResultTypeException;
import com.serotonin.m2m2.rt.script.ScriptExecutor;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Matthew Lohbihler
 */
public class PointLinksDwr extends ModuleDwr {
    @DwrPermission(user = true)
    public Map<String, Object> init() {
        User user = Common.getUser();
        Map<String, Object> data = new HashMap<String, Object>();

        // Get the points that this user can access.
        List<DataPointVO> allPoints = new DataPointDao().getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<IntStringPair> sourcePoints = new ArrayList<IntStringPair>();
        List<IntStringPair> targetPoints = new ArrayList<IntStringPair>();
        for (DataPointVO point : allPoints) {
            if (Permissions.hasDataPointReadPermission(user, point))
                sourcePoints.add(new IntStringPair(point.getId(), point.getExtendedName()));
            if (point.getPointLocator().isSettable() && Permissions.hasDataPointSetPermission(user, point))
                targetPoints.add(new IntStringPair(point.getId(), point.getExtendedName()));
        }

        data.put("sourcePoints", sourcePoints);
        data.put("targetPoints", targetPoints);

        // Get the existing point links.
        List<PointLinkVO> pointLinks = new ArrayList<PointLinkVO>();
        for (PointLinkVO pointLink : new PointLinkDao().getPointLinks()) {
            if (containsPoint(sourcePoints, pointLink.getSourcePointId())
                    && containsPoint(targetPoints, pointLink.getTargetPointId()))
                pointLinks.add(pointLink);
        }

        data.put("pointLinks", pointLinks);

        return data;
    }

    private boolean containsPoint(List<IntStringPair> pointList, int pointId) {
        for (IntStringPair ivp : pointList) {
            if (ivp.getKey() == pointId)
                return true;
        }
        return false;
    }

    @DwrPermission(user = true)
    public PointLinkVO getPointLink(int id) {
        PointLinkVO vo;
        PointLinkDao pointLinkDao = new PointLinkDao();
        if (id == Common.NEW_ID) {
            vo = new PointLinkVO();
            vo.setXid(pointLinkDao.generateUniqueXid());
        }
        else
            vo = pointLinkDao.getPointLink(id);
        return vo;
    }

    @DwrPermission(user = true)
    public ProcessResult savePointLink(int id, String xid, int sourcePointId, int targetPointId, String script,
            int event, boolean writeAnnotation, boolean disabled, ScriptPermissions permissions, int logLevel) {
        // Validate the given information. If there is a problem, return an appropriate error message.
        PointLinkVO vo = new PointLinkVO();
        vo.setId(id);
        vo.setXid(xid);
        vo.setSourcePointId(sourcePointId);
        vo.setTargetPointId(targetPointId);
        vo.setScript(script);
        vo.setEvent(event);
        vo.setWriteAnnotation(writeAnnotation);
        vo.setDisabled(disabled);
        vo.setScriptPermissions(permissions);
        vo.setLogLevel(logLevel);

        ProcessResult response = new ProcessResult();
        PointLinkDao pointLinkDao = new PointLinkDao();

        if (StringUtils.isBlank(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (!pointLinkDao.isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");

        vo.validate(response);

        // Save it
        if (!response.getHasMessages())
            RTMDefinition.instance.savePointLink(vo);

        response.addData("plId", vo.getId());

        return response;
    }

    @DwrPermission(user = true)
    public void deletePointLink(int id) {
        RTMDefinition.instance.deletePointLink(id);
    }

    @DwrPermission(user = true)
    public ProcessResult validateScript(String script, int sourcePointId, int targetPointId, ScriptPermissions permissions, int logLevel) {
        ProcessResult response = new ProcessResult();
        TranslatableMessage message;
        ScriptExecutor scriptExecutor = new ScriptExecutor();

        DataPointRT point = Common.runtimeManager.getDataPoint(sourcePointId);
        if (point == null)
            message = new TranslatableMessage("event.pointLink.sourceUnavailable");
        else {
            Map<String, IDataPointValueSource> context = new HashMap<String, IDataPointValueSource>();
            context.put(PointLinkRT.CONTEXT_VAR_NAME, point);
            DataPointVO target = DataPointDao.instance.getDataPoint(targetPointId);
            if(target == null){
            	message = new TranslatableMessage("pointLinks.validate.targetRequired");
            }else{
	            int targetDataType = target.getPointLocator().getDataTypeId();
	
	            final StringWriter scriptOut = new StringWriter();
	            final PrintWriter scriptWriter = new PrintWriter(scriptOut);
	            ScriptLog scriptLog = new ScriptLog(scriptWriter, logLevel);
	
	            try {
	                PointValueTime pvt = scriptExecutor.execute(script, context, null, System.currentTimeMillis(),
	                        targetDataType, -1, permissions ,scriptWriter, scriptLog);
	                if (pvt.getValue() == null)
	                    message = new TranslatableMessage("event.pointLink.nullResult");
	                else if (pvt.getTime() == -1)
	                    message = new TranslatableMessage("pointLinks.validate.success", pvt.getValue());
	                else
	                    message = new TranslatableMessage("pointLinks.validate.successTs", pvt.getValue(),
	                            Functions.getTime(pvt.getTime()));
	            	//Add the script logging output
	                response.addData("out", scriptOut.toString().replaceAll("\n", "<br/>"));
	            }
	            catch (ScriptException e) {
	                message = new TranslatableMessage("common.default", e.getMessage());
	            }
	            catch (ResultTypeException e) {
	                message = e.getTranslatableMessage();
	            }
            }
        }

        response.addMessage("script", message);
        return response;
    }
    
    @DwrPermission(user = true)
    public String getLogPath(int pointId) {
    	return PointLinkRT.getLogFile(pointId).getAbsolutePath();
    }
    
}
