/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.InvalidArgumentException;
import com.serotonin.util.ColorUtils;

/**
 * @author Matthew Lohbihler
 */
public class ChartPointTag extends TagSupport {
    private static final long serialVersionUID = -1;

    private String xid;
    private String color;

    public void setXid(String xid) {
        this.xid = xid;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public int doStartTag() throws JspException {
        ChartTag chartTag = (ChartTag) findAncestorWithClass(this, ChartTag.class);
        if (chartTag == null)
            throw new JspException("chartPoint tags must be used within a chart tag");

        // Validate the colour.
        try {
            if (!StringUtils.isBlank(color))
                ColorUtils.toColor(color);
        }
        catch (InvalidArgumentException e) {
            throw new JspException("Invalid color '" + color + "'");
        }

        chartTag.addChartPoint(xid, color);

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public void release() {
        super.release();
        xid = null;
        color = null;
    }
}
