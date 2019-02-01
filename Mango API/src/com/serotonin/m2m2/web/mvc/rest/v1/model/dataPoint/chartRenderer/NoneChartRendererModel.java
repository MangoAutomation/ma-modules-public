/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.chartRenderer;

/**
 * @author Jared Wiltshire
 */
public class NoneChartRendererModel extends BaseChartRendererModel<NoneChartRendererModel> {

    @Override
    public String getType() {
        return NoneChartRendererModelDefinition.TYPE_NAME;
    }

}
