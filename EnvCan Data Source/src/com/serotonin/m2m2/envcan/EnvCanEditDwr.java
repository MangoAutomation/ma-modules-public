/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.envcan;

import java.util.Date;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class EnvCanEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveEnvCanDataSource(BasicDataSourceVO basic, int stationId, Date dataStartTime) {
        EnvCanDataSourceVO ds = (EnvCanDataSourceVO) Common.getHttpUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setStationId(stationId);
        ds.setDataStartTime(dataStartTime.getTime());

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public ProcessResult saveEnvCanPointLocator(int id, String xid, String name, EnvCanPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
