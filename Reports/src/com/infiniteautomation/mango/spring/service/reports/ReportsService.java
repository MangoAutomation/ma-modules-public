/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service.reports;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.ReportPermissionDefinition;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportJob;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 *
 * TODO Add read and edit permissions to the VO for use in this class
 * TODO Mango 3.6 the name field is 100 chars long in the database, it should be 255 to be compatible with base class validation
 *
 * @author Terry Packer
 *
 */
@Service
public class ReportsService extends AbstractVOService<ReportVO, ReportDao> {

    public ReportsService(@Autowired ReportDao dao) {
        super(dao);
    }

    protected void maybeSchedule(ReportVO vo) {
        // Conditionally schedule the report.
        String host = "";
        WebContext webContext = WebContextFactory.get();
        int port;
        if (webContext != null) {
            HttpServletRequest req = webContext.getHttpServletRequest();
            host = req.getServerName();
            port = req.getLocalPort();
        }else{
            port = Common.envProps.getInt("web.port", 8080);
        }

        ReportJob.scheduleReportJob(host, port, vo);
    }

    @Override
    protected ReportVO insert(ReportVO vo, PermissionHolder user, boolean full)
            throws PermissionException, ValidationException {
        ReportVO saved = super.insert(vo, user, full);
        maybeSchedule(saved);
        return saved;
    }

    @Override
    protected ReportVO update(ReportVO existing, ReportVO vo, PermissionHolder user, boolean full)
            throws PermissionException, ValidationException {
        ReportVO updated = super.update(existing, vo, user, full);
        maybeSchedule(updated);
        return updated;
    }

    @Override
    public ReportVO delete(String xid, PermissionHolder user)
            throws PermissionException, NotFoundException {
        ReportVO vo = get(xid, user);
        ensureEditPermission(user, vo);
        ReportJob.unscheduleReportJob(vo);
        dao.delete(vo.getId());
        return vo;
    }

    @Override
    public boolean hasCreatePermission(PermissionHolder user, ReportVO vo) {
        if(user.hasAdminPermission())
            return true;
        else if(Permissions.hasAnyPermission(user, getReportCreatePermissions()))
            return true;
        else
            return false;
    }

    protected Set<String> getReportCreatePermissions() {
        String reportCreatePermissions = SystemSettingsDao.instance.getValue(ReportPermissionDefinition.PERMISSION);
        if(reportCreatePermissions == null)
            return new HashSet<>();
        else
            return Permissions.explodePermissionGroups(reportCreatePermissions);
    }


    @Override
    public boolean hasEditPermission(PermissionHolder user, ReportVO vo) {
        if (user instanceof User && ((User) user).getId() == vo.getId())
            return true;
        return user.hasAdminPermission();
    }

    @Override
    public boolean hasReadPermission(PermissionHolder user, ReportVO vo) {
        if (user instanceof User && ((User) user).getId() == vo.getId())
            return true;
        return user.hasAdminPermission();
    }

}
