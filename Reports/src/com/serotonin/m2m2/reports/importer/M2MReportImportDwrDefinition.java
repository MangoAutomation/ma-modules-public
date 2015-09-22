/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import com.serotonin.m2m2.module.DwrDefinition;
import com.serotonin.m2m2.web.dwr.ModuleDwr;

/**
 * @author Terry Packer
 *
 */
public class M2MReportImportDwrDefinition extends DwrDefinition {
    @Override
    public Class<? extends ModuleDwr> getDwrClass() {
        return M2MReportImportDwr.class;
    }
}
