/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
import com.infiniteautomation.mango.util.script.MangoJavaScript;
import com.infiniteautomation.mango.util.script.MangoJavaScriptAction;
import com.infiniteautomation.mango.util.script.MangoJavaScriptError;
import com.infiniteautomation.mango.util.script.MangoJavaScriptResult;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.script.ScriptContextVariable;
import com.serotonin.m2m2.util.log.LogLevel;
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
    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public Map<String, Object> init() {
        User user = Common.getHttpUser();
        Map<String, Object> data = new HashMap<String, Object>();

        // Get the points that this user can access.
        List<DataPointVO> allPoints = DataPointDao.getInstance().getDataPoints(DataPointExtendedNameComparator.instance, false);
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
        for (PointLinkVO pointLink : PointLinkDao.getInstance().getAll()) {
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

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public PointLinkVO getPointLink(int id) {
        PointLinkVO vo;
        PointLinkDao pointLinkDao = PointLinkDao.getInstance();
        if (id == Common.NEW_ID) {
            vo = new PointLinkVO();
            vo.setXid(pointLinkDao.generateUniqueXid());
        }
        else
            vo = pointLinkDao.get(id);
        return vo;
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public ProcessResult savePointLink(int id, String xid, String name, int sourcePointId, int targetPointId, String script,
            int event, boolean writeAnnotation, boolean disabled, String permissions, LogLevel logLevel, float logSize, int logCount) {
        // Validate the given information. If there is a problem, return an appropriate error message.
        User user = Common.getHttpUser();
        PointLinkVO vo = new PointLinkVO();
        vo.setId(id);
        vo.setXid(xid);
        vo.setName(name);
        vo.setSourcePointId(sourcePointId);
        vo.setTargetPointId(targetPointId);
        vo.setScript(script);
        vo.setEvent(event);
        vo.setWriteAnnotation(writeAnnotation);
        vo.setDisabled(disabled);
        vo.setScriptPermissions(new ScriptPermissions(Permissions.explodePermissionGroups(permissions), user.getPermissionHolderName()));
        vo.setLogLevel(logLevel);
        vo.setLogSize(logSize);
        vo.setLogCount(logCount);

        ProcessResult response = new ProcessResult();
        PointLinkDao pointLinkDao = PointLinkDao.getInstance();

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

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public void deletePointLink(int id) {
        RTMDefinition.instance.deletePointLink(id);
    }

    @DwrPermission(custom = PointLinkPermissionDefinition.PERMISSION)
    public ProcessResult validateScript(String script, int sourcePointId, int targetPointId, String permissions, LogLevel logLevel) {
        User user = Common.getHttpUser();
        ProcessResult response = new ProcessResult();
        TranslatableMessage message;
        
        MangoJavaScriptService service = Common.getBean(MangoJavaScriptService.class);
        MangoJavaScript vo = new MangoJavaScript();
        vo.setWrapInFunction(true);
        vo.setLogLevel(logLevel);
        vo.setPermissions(new ScriptPermissions(Permissions.explodePermissionGroups(permissions), user.getPermissionHolderName()));
        vo.setScript(script);
        List<ScriptContextVariable> context = new ArrayList<>();
        vo.setContext(context);
        ScriptContextVariable source = new ScriptContextVariable();
        source.setVariableName(PointLinkRT.CONTEXT_SOURCE_VAR_NAME);
        source.setDataPointId(sourcePointId);
        context.add(source);
        ScriptContextVariable target = new ScriptContextVariable();
        target.setVariableName(PointLinkRT.CONTEXT_TARGET_VAR_NAME);
        target.setDataPointId(targetPointId);
        context.add(target);
        
        DataPointVO sourceVo = DataPointDao.getInstance().getDataPoint(sourcePointId, false);
        if(sourceVo == null) {
            message = new TranslatableMessage("pointLinks.validate.sourceRequired");
            response.addMessage("script", message);
            return response;
        }
        DataPointVO targetVo = DataPointDao.getInstance().getDataPoint(targetPointId, false);
        if(targetVo == null) {
            message = new TranslatableMessage("pointLinks.validate.targetRequired");
            response.addMessage("script", message);
            return response;
        }
        vo.setResultDataTypeId(targetVo.getPointLocator().getDataTypeId());
        
        MangoJavaScriptResult result = service.testScript(vo, user);
        PointValueTime pvt = (PointValueTime)result.getResult();
        if (pvt == null || pvt.getValue() == null)
            message = new TranslatableMessage("event.pointLink.nullResult");
        else if(pvt.getValue() == MangoJavaScriptService.UNCHANGED)
            message = new TranslatableMessage("pointLinks.validate.successNoValue");
        else if (pvt.getTime() == -1)
            message = new TranslatableMessage("pointLinks.validate.success", pvt.getValue());
        else
            message = new TranslatableMessage("pointLinks.validate.successTs", pvt.getValue(),
                    Functions.getTime(pvt.getTime()));
        response.addMessage("script", message);
        
        if(result.hasErrors()) {
            for(MangoJavaScriptError e : result.getErrors()) {
                if(e.getLineNumber() == null) {
                    response.addContextualMessage("script", "literal", e.getMessage());
                } else {
                    if (e.getColumnNumber() == null)
                        response.addContextualMessage("script", "scripting.rhinoException", e.getMessage(), e.getLineNumber());
                    else
                        response.addContextualMessage("script", "scripting.rhinoExceptionCol", e.getMessage(), e.getLineNumber(), e.getColumnNumber());
                }
            }
        }
    
        //Convert the actions into the script out
        String output = result.getScriptOutput();
        Locale locale = null;
        if(user != null)
            locale = Locale.forLanguageTag(user.getLocale());
        if(locale == null)
            locale = Common.getLocale();
        if(result.getActions() != null) {
            for(MangoJavaScriptAction action : result.getActions())
                output += action.getMessage().translate(Translations.getTranslations(locale)) + "\n";
        }
        response.addData("out", com.serotonin.web.taglib.Functions.lfToBr(com.serotonin.web.taglib.Functions.crlfToBr(output)));
        return response;
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public String getLogPath(int pointId) {
        return Common.getLogsDir().getAbsolutePath() + File.separator + PointLinkRT.LOG_FILE_PREFIX + pointId + ".log";
    }

}
