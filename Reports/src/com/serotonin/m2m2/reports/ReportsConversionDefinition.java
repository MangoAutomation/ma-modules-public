/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.module.DwrConversionDefinition;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.reports.vo.ReportVO;

public class ReportsConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(ReportVO.class);
        addConversion(ReportPointVO.class);
        addConversion(ReportInstance.class);
    }
}
