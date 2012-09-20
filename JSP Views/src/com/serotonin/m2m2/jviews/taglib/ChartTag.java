/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.jviews.component.JspViewChartPoint;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class ChartTag extends ViewTagSupport {
    private static final long serialVersionUID = -1;

    private int duration;
    private String durationType;
    private int width;
    private int height;
    private List<JspViewChartPoint> points;
    private JspView view;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int doStartTag() throws JspException {
        points = new ArrayList<JspViewChartPoint>();

        // Find the JSP view.
        view = getJspView();

        return EVAL_BODY_INCLUDE;
    }

    void addChartPoint(String xid, String color) throws JspException {
        DataPointVO dataPointVO = getDataPointVO(view, xid);
        points.add(new JspViewChartPoint(dataPointVO, color));
    }

    @Override
    public int doEndTag() throws JspException {
        int periodType = Common.TIME_PERIOD_CODES.getId(durationType.toUpperCase());
        if (periodType == -1)
            throw new JspException("Invalid durationType. Must be one of " + Common.TIME_PERIOD_CODES.getCodeList());
        long millis = Common.getMillis(periodType, duration);

        // Add the chart to the view
        int id = view.addChart(millis, width, height, points);

        // Add the id for the point to the page context.
        pageContext.setAttribute("componentId", id);

        return EVAL_PAGE;
    }

    @Override
    public void release() {
        super.release();
        duration = 0;
        durationType = null;
        width = 0;
        height = 0;
        view = null;
        points = null;
    }
}
