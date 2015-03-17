package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.internal.threads.ThreadInfoBean;
import com.serotonin.m2m2.module.DwrConversionDefinition;

public class DwrConversions extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(StackTraceElement.class);
        addConversion(ThreadInfoBean.class);
    }
}
