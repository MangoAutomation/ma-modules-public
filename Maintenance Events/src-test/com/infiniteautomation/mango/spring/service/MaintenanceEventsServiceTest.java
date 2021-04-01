/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.service;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.db.tables.MaintenanceEvents;
import com.serotonin.m2m2.maintenanceEvents.db.tables.records.MaintenanceEventsRecord;
import com.serotonin.m2m2.module.definitions.permissions.DataSourcePermissionDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventsServiceTest extends AbstractVOServiceWithPermissionsTest<MaintenanceEventVO, MaintenanceEventsRecord, MaintenanceEvents, MaintenanceEventDao, MaintenanceEventsService> {

    @BeforeClass
    public static void setup() {
        loadModules();
    }

    @Override
    String getCreatePermissionType() {
        return DataSourcePermissionDefinition.PERMISSION;
    }

    @Override
    void setReadPermission(MangoPermission permission, MaintenanceEventVO vo) {
        //A user with read permission for all data points (and sources) in this event has read permission
        if(permission != null) {
            //Get the data points and add our roles to the read roles
            for (int dpId : vo.getDataPoints()) {
                DataPointVO dp = DataPointDao.getInstance().get(dpId);
                dp.setReadPermission(permission);
                DataPointDao.getInstance().update(dp.getId(), dp);
            }
        }
        vo.setTogglePermission(permission);
    }

    @Override
    void setEditPermission(MangoPermission permission, MaintenanceEventVO vo) {
        //A user with edit permission for the sources of all points (and all data sources sources) in this event has edit permission
        if(permission != null) {
            //Get the data points and add our roles to the read roles
            for (int dpId : vo.getDataPoints()) {
                DataPointVO dp = DataPointDao.getInstance().get(dpId);
                dp.setEditPermission(permission);
                DataPointDao.getInstance().update(dp.getId(), dp);

                DataSourceVO ds = DataSourceDao.getInstance().get(dp.getDataSourceId());
                ds.setEditPermission(permission);
                DataSourceDao.getInstance().update(ds.getId(), ds);
            }
        }
        vo.setTogglePermission(permission);
    }

    @Override
    MaintenanceEventsService getService() {
        return Common.getBean(MaintenanceEventsService.class);
    }

    @Override
    MaintenanceEventDao getDao() {
        return MaintenanceEventDao.getInstance();
    }

    @Override
    void assertVoEqual(MaintenanceEventVO expected, MaintenanceEventVO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getXid(), actual.getXid());
        assertEquals(expected.getName(), actual.getName());
    }

    @Override
    MaintenanceEventVO newVO(User owner) {
        MaintenanceEventVO vo = new MaintenanceEventVO();
        vo.setName("testing name");
        for(IDataPoint point : createMockDataPoints(5)) {
            vo.getDataPoints().add(point.getId());
        }
        return vo;
    }

    @Override
    MaintenanceEventVO updateVO(MaintenanceEventVO existing) {
        MaintenanceEventVO vo = (MaintenanceEventVO)existing.copy();
        vo.setName("testing");
        return vo;
    }

    @Override
    @Test()
    public void testCannotRemoveEditAccess() {
        //This test does not apply
    }

    @Override
    @Test()
    public void testAddEditRoleUserDoesNotHave() {
        //This test does not apply
    }

    @Override
    @Test
    public void testCountQueryReadPermissionEnforcement() {
        //This test does not apply
    }

    @Override
    @Test
    public void testCountQueryEditPermissionEnforcement() {
        //This test does not apply
    }

    @Override
    @Test
    public void testQueryReadPermissionEnforcement() {
        //This test does not apply
    }

    @Override
    @Test
    public void testQueryEditPermissionEnforcement() {
        //This test does not apply
    }

    //TODO Test Add/Remove/Use Toggle Permission

    @Override
    String getReadPermissionContextKey() {
        return "togglePermission";
    }

    @Override
    String getEditPermissionContextKey() {
        return "togglePermission";
    }

    @Test
    @Override
    public void testAddReadRoleUserDoesNotHave() {
        runTest(() -> {
            MaintenanceEventVO vo = newVO(readUser);
            setReadPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
            setEditPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
            service.insert(vo);
            runAs.runAs(readUser, () -> {
                MaintenanceEventVO fromDb = service.get(vo.getId());
                assertVoEqual(vo, fromDb);
                vo.setTogglePermission(MangoPermission.requireAnyRole(roleService.getSuperadminRole()));
                service.update(fromDb.getId(), fromDb);
            });

        }, getReadPermissionContextKey(), getReadPermissionContextKey());
    }

    @Override
    @Test
    public void testCannotRemoveReadAccess() {
        //NoOp
    }

    @Test
    public void testCannotRemoveToggleAccess() {
        runTest(() -> {
            MaintenanceEventVO vo = newVO(editUser);
            setReadPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
            setEditPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
            service.insert(vo);
            runAs.runAs(readUser, () -> {
                MaintenanceEventVO fromDb = service.get(vo.getId());
                assertVoEqual(vo, fromDb);
                vo.setTogglePermission(MangoPermission.requireAnyRole(Collections.emptySet()));
                service.update(fromDb.getId(), fromDb);
            });
        }, getReadPermissionContextKey());
    }
}
