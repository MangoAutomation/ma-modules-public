/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class VMStatEditDwr extends DataSourceEditDwr {
    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public ProcessResult saveVMStatDataSource(BasicDataSourceVO basic, int pollSeconds, int outputScale) {
        VMStatDataSourceVO ds = (VMStatDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setPollSeconds(pollSeconds);
        ds.setOutputScale(outputScale);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public ProcessResult saveVMStatPointLocator(int id, String xid, String name, VMStatPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
