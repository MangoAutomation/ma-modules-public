/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;

import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventsTableDefinition;
import com.serotonin.m2m2.maintenanceEvents.RTMDefinition;
import com.serotonin.m2m2.maintenanceEvents.SchemaDefinition;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.module.definitions.permissions.DataSourcePermissionDefinition;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventsServiceTest extends AbstractVOServiceWithPermissionsTest<MaintenanceEventVO, MaintenanceEventsTableDefinition, MaintenanceEventDao, MaintenanceEventsService> {

    @BeforeClass
    public static void setup() {
        List<ModuleElementDefinition> definitions = new ArrayList<>();
        definitions.add(new SchemaDefinition());
        definitions.add(new RTMDefinition());
        addModule("maintenanceEvents", definitions);
    }

    @Override
    String getCreatePermissionType() {
        return DataSourcePermissionDefinition.PERMISSION;
    }

    @Override
    void setReadRoles(Set<Role> roles, MaintenanceEventVO vo) {
        vo.setToggleRoles(roles);
        if(roles != null) {
            getService().permissionService.runAsSystemAdmin(() -> {
                Set<Role> existing = roleService.getRoles(getCreatePermissionType());
                for(Role r :roles) {
                    if(!existing.contains(r) && !r.equals(roleService.getUserRole())) {
                        roleService.addRoleToPermission(r, getCreatePermissionType(), systemSuperadmin);
                    }
                }
            });
        }
    }

    @Override
    void addReadRoleToFail(Role role, MaintenanceEventVO vo) {
        vo.getToggleRoles().add(role);
    }

    @Override
    void setEditRoles(Set<Role> roles, MaintenanceEventVO vo) {
        vo.setToggleRoles(roles);
        if(roles != null) {
            getService().permissionService.runAsSystemAdmin(() -> {
                Set<Role> existing = roleService.getRoles(getCreatePermissionType());
                for(Role r :roles) {
                    if(!existing.contains(r) && !r.equals(roleService.getUserRole())) {
                        roleService.addRoleToPermission(r, getCreatePermissionType(), systemSuperadmin);
                    }
                }
            });
        }
    }

    @Override
    void addEditRoleToFail(Role role, MaintenanceEventVO vo) {
        vo.getToggleRoles().add(role);
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

        getService().permissionService.runAsSystemAdmin(() -> {
            Set<Role> existing = roleService.getRoles(getCreatePermissionType());
            for(Role r : owner.getRoles()) {
                if(!existing.contains(r) && !r.equals(roleService.getUserRole())) {
                    roleService.addRoleToPermission(r, getCreatePermissionType(), systemSuperadmin);
                }
            }
        });
        return vo;
    }

    @Override
    MaintenanceEventVO updateVO(MaintenanceEventVO existing) {
        MaintenanceEventVO vo = existing.copy();
        vo.setName("testing");
        return vo;
    }

}
