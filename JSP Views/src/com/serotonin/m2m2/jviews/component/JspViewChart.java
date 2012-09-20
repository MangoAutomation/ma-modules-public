/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.component;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.jviews.JspComponentState;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;

/**
 * @author Matthew Lohbihler
 */
public class JspViewChart extends JspViewComponent {
    private final long duration;
    private final int width;
    private final int height;
    private final List<JspViewChartPoint> points;

    public JspViewChart(long duration, int id, int width, int height, List<JspViewChartPoint> points) {
        super(id);
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.points = points;
    }

    @Override
    protected void createStateImpl(RuntimeManager rtm, HttpServletRequest request, JspComponentState state) {
        long maxTs = 0;
        for (JspViewChartPoint point : points) {
            DataPointRT dataPointRT = rtm.getDataPoint(point.getDataPointVO().getId());
            if (dataPointRT != null) {
                PointValueTime pvt = dataPointRT.getPointValue();
                if (pvt != null && maxTs < pvt.getTime())
                    maxTs = pvt.getTime();
            }
        }

        StringBuilder htmlData = new StringBuilder();
        htmlData.append("chart/");
        htmlData.append(maxTs);
        htmlData.append('_');
        htmlData.append(duration);

        for (JspViewChartPoint point : points) {
            htmlData.append('_');
            htmlData.append(point.getDataPointVO().getId());
            if (!StringUtils.isBlank(point.getColor()))
                htmlData.append('|').append(point.getColor().replaceAll("#", "0x"));
        }

        htmlData.append(".png");

        htmlData.append("?w=");
        htmlData.append(width);
        htmlData.append("&h=");
        htmlData.append(height);

        state.setValue(htmlData.toString());
    }
}
