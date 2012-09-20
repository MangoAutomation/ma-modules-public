/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
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
