/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import javax.servlet.jsp.JspException;

import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class SimplePointTag extends ViewTagSupport {
    private static final long serialVersionUID = -1;

    private String xid;
    private boolean raw;
    private String disabledValue;
    private boolean time;

    public void setXid(String xid) {
        this.xid = xid;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public void setDisabledValue(String disabledValue) {
        this.disabledValue = disabledValue;
    }

    public void setTime(boolean time) {
        this.time = time;
    }

    @Override
    public int doStartTag() throws JspException {
        // Find the custom view.
        JspView view = getJspView();

        // Find the point.
        DataPointVO dataPointVO = getDataPointVO(view, xid);

        // Add the point to the view
        int id = view.addPoint(dataPointVO, raw, disabledValue, time);

        // Add the id for the point to the page context.
        pageContext.setAttribute("componentId", id);

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public void release() {
        super.release();
        xid = null;
        raw = false;
        disabledValue = null;
        time = false;
    }
}
