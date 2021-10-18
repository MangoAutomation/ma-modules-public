/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.spring.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rules.ExpectValidationException;
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

        List<Integer> actualPoints = actual.getDataPoints();
        List<Integer> expectedPoints = expected.getDataPoints();
        assertEquals(expectedPoints.size(), actualPoints.size());
        for (int i = 0; i < expectedPoints.size(); i++) {
            assertEquals(expectedPoints.get(i), actualPoints.get(i));
        }

        List<String> actualPointXids = new ArrayList<>();
        List<String> expectedPointXids = new ArrayList<>();
        dao.getPointXids(actual.getId(), actualPointXids::add);
        dao.getPointXids(expected.getId(), expectedPointXids::add);
        assertEquals(expectedPointXids.size(), actualPointXids.size());
        for (int i = 0; i < expectedPointXids.size(); i++) {
            assertEquals(expectedPointXids.get(i), actualPointXids.get(i));
        }

        List<Integer> actualDataSources = actual.getDataSources();
        List<Integer> expectedDataSources = expected.getDataSources();
        assertEquals(expectedDataSources.size(), actualDataSources.size());
        for (int i = 0; i < expectedDataSources.size(); i++) {
            assertEquals(expectedDataSources.get(i), actualDataSources.get(i));
        }

        List<String> actualDataSourceXids = new ArrayList<>();
        List<String> expectedDataSourceXids = new ArrayList<>();
        dao.getSourceXids(actual.getId(), actualDataSourceXids::add);
        dao.getSourceXids(expected.getId(), expectedDataSourceXids::add);
        assertEquals(expectedDataSourceXids.size(), actualDataSourceXids.size());
        for (int i = 0; i < expectedDataSourceXids.size(); i++) {
            assertEquals(expectedDataSourceXids.get(i), actualDataSourceXids.get(i));
        }
    }

    @Override
    MaintenanceEventVO newVO(User owner) {
        MaintenanceEventVO vo = new MaintenanceEventVO();
        vo.setName(UUID.randomUUID().toString());

        List<Integer> dataPointIds = new ArrayList<>();
        for(IDataPoint point : createMockDataPoints(5)) {
            dataPointIds.add(point.getId());
        }
        vo.setDataPoints(dataPointIds);

        List<Integer> dataSourceIds = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            Integer dataSourceId =  createMockDataSource().getId();
            dataSourceIds.add(dataSourceId);
        }
        vo.setDataSources(dataSourceIds);
        return vo;
    }

    @Override
    MaintenanceEventVO updateVO(MaintenanceEventVO existing) {
        MaintenanceEventVO copy = (MaintenanceEventVO) existing.copy();
        copy.setName("new name");

        List<Integer> dataPointIds = new ArrayList<>();
        for(IDataPoint point : createMockDataPoints(10)) {
            dataPointIds.add(point.getId());
        }
        copy.setDataPoints(dataPointIds);

        List<Integer> dataSourceIds = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            Integer dataSourceId =  createMockDataSource().getId();
            dataSourceIds.add(dataSourceId);
        }
        copy.setDataSources(dataSourceIds);
        return copy;
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
        validation.expectValidationException(getReadPermissionContextKey());
        MaintenanceEventVO vo = newVO(readUser);
        setReadPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
        setEditPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
        service.insert(vo);
        runAs.runAs(readUser, () -> {
            MaintenanceEventVO fromDb = service.get(vo.getId());
            assertVoEqual(vo, fromDb);
            fromDb.setTogglePermission(MangoPermission.superadminOnly());
            service.update(fromDb.getId(), fromDb);
        });
    }

    @Override
    @Test
    public void testCannotRemoveReadAccess() {
        //NoOp
    }

    @Test
    @ExpectValidationException("togglePermission")
    public void testCannotRemoveToggleAccess() {
        MaintenanceEventVO vo = newVO(editUser);
        setReadPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
        setEditPermission(MangoPermission.requireAnyRole(roleService.getUserRole()), vo);
        service.insert(vo);
        runAs.runAs(readUser, () -> {
            MaintenanceEventVO fromDb = service.get(vo.getId());
            assertVoEqual(vo, fromDb);
            fromDb.setTogglePermission(MangoPermission.superadminOnly());
            service.update(fromDb.getId(), fromDb);
        });
    }
}
