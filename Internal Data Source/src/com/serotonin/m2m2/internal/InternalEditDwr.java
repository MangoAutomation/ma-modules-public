/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class InternalEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveInternalDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType) {
        InternalDataSourceVO ds = (InternalDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public ProcessResult saveInternalPointLocator(int id, String xid, String name, InternalPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
