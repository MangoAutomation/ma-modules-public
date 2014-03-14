/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.util.List;

import com.serotonin.m2m2.module.FiledataDefinition;

public class ReportsFiledataDefinition extends FiledataDefinition {
    @Override
    public List<Long> getFiledataImageIds() {
        return new ReportDao().getFiledataIds();
    }
}
