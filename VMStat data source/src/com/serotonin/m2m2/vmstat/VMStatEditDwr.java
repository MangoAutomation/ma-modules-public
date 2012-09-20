/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class VMStatEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveVMStatDataSource(BasicDataSourceVO basic, int pollSeconds, int outputScale) {
        VMStatDataSourceVO ds = (VMStatDataSourceVO) Common.getUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setPollSeconds(pollSeconds);
        ds.setOutputScale(outputScale);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public ProcessResult saveVMStatPointLocator(int id, String xid, String name, VMStatPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}
