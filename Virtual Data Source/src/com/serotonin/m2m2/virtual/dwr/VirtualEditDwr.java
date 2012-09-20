/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.dwr;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.util.IntMessagePair;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class VirtualEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveVirtualDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType) {
        VirtualDataSourceVO ds = (VirtualDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public IntMessagePair[] getChangeTypes(int dataTypeId) {
        return ChangeTypeVO.getChangeTypes(dataTypeId);
    }

    @DwrPermission(user = true)
    public ProcessResult saveVirtualPointLocator(int id, String xid, String name, VirtualPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
