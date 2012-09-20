/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
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
