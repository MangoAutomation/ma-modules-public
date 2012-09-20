/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.jviews.JspViewsCommon;
import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Matthew Lohbihler
 */
abstract public class ViewTagSupport extends TagSupport {
    private static final long serialVersionUID = -1;

    protected JspView getJspView() throws JspException {
        JspView view = JspViewsCommon.getJspView((HttpServletRequest) pageContext.getRequest());
        if (view == null)
            throw new JspException("No JSP view in session. Use the init tag before defining points");
        return view;
    }

    protected DataPointVO getDataPointVO(JspView view, String xid) throws JspException {
        // Find the point.
        DataPointVO dataPointVO = new DataPointDao().getDataPoint(xid);
        if (dataPointVO == null)
            throw new JspException("Point with XID '" + xid + "' not found");

        // Check that the authorizing user has access to the point.
        Permissions.ensureDataPointReadPermission(view.getAuthorityUser(), dataPointVO);

        return dataPointVO;
    }
}
