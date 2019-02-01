/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.chartRenderer;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

/**
 * @author Jared Wiltshire
 */
public class NoneChartRendererModelDefinition extends ModelDefinition {

    public static final String TYPE_NAME = "chartRendererNone";

    @Override
    public String getModelKey() {
        return null;
    }

    @Override
    public String getModelTypeName() {
        return TYPE_NAME;
    }

    @Override
    public AbstractRestModel<?> createModel() {
        return new NoneChartRendererModel();
    }

    @Override
    public boolean supportsClass(Class<?> clazz) {
        return NoneChartRendererModel.class.equals(clazz);
    }

    @Override
    public Class<? extends AbstractRestModel<?>> getModelClass() {
        return NoneChartRendererModel.class;
    }
}
