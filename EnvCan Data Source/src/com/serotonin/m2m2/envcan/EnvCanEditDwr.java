/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class EnvCanEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveEnvCanDataSource(BasicDataSourceVO basic, int stationId) {
        EnvCanDataSourceVO ds = (EnvCanDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setStationId(stationId);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public ProcessResult saveEnvCanPointLocator(int id, String xid, String name, EnvCanPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
