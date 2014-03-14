/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews;

import org.directwebremoting.WebContextFactory;

import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Matthew Lohbihler
 */
public class JspViewDwr extends ModuleDwr {
    @DwrPermission(anonymous = true)
    public void setJspViewPoint(int pollSessionId, String xid, String valueStr) {
        JspView view = JspViewsCommon.getJspView(WebContextFactory.get().getHttpServletRequest());
        setPointImpl(view.getPoint(xid), valueStr, view.getAuthorityUser());
        notifyLongPollImpl(getLongPollData(pollSessionId, false).getRequest());
    }
}
