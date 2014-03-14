/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.component;

import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class JspViewChartPoint {
    private final DataPointVO dataPointVO;
    private final String color;

    public JspViewChartPoint(DataPointVO dataPointVO, String color) {
        this.dataPointVO = dataPointVO;
        this.color = color;
    }

    public DataPointVO getDataPointVO() {
        return dataPointVO;
    }

    public String getColor() {
        return color;
    }
}
