package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.internal.threads.ThreadInfoBean;
import com.serotonin.m2m2.module.DwrConversionDefinition;
import com.serotonin.m2m2.util.timeout.RejectedTaskStats;
import com.serotonin.timer.OrderedTaskInfo;

public class DwrConversions extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(StackTraceElement.class);
        addConversion(ThreadInfoBean.class);
        addConversion(OrderedTaskInfo.class);
        addConversion(RejectedTaskStats.class);
    }
}
