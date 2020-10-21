/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.service;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.dao.WatchListDao;
import com.infiniteautomation.mango.spring.dao.WatchListTableDefinition;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.watchlist.WatchListCreatePermission;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class WatchListServiceTest extends AbstractVOServiceWithPermissionsTest<WatchListVO, WatchListTableDefinition, WatchListDao, WatchListService> {

    @BeforeClass
    public static void setup() {
        loadModules();
    }

    @Override
    String getCreatePermissionType() {
        return WatchListCreatePermission.PERMISSION;
    }

    @Override
    void setReadPermission(MangoPermission permission, WatchListVO vo) {
        vo.setReadPermission(permission);
    }

    @Override
    void setEditPermission(MangoPermission permission, WatchListVO vo) {
        vo.setEditPermission(permission);
    }

    @Override
    WatchListService getService() {
        return Common.getBean(WatchListService.class);
    }

    @Override
    WatchListDao getDao() {
        return WatchListDao.getInstance();
    }

    @Override
    void assertVoEqual(WatchListVO expected, WatchListVO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getXid(), actual.getXid());
        assertEquals(expected.getName(), actual.getName());

        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getData().size(), actual.getData().size());
        expected.getData().keySet().forEach(key -> {
            assertEquals(expected.getData().get(key), (actual.getData().get(key)));
        });

    }

    @Override
    WatchListVO newVO(User owner) {
        WatchListVO vo = new WatchListVO();
        vo.setName(UUID.randomUUID().toString());
        vo.setType(WatchListVO.STATIC_TYPE);
        for(IDataPoint point : createMockDataPoints(5, false, MangoPermission.requireAnyRole(owner.getRoles()), MangoPermission.requireAnyRole(owner.getRoles()))) {
            vo.getPointList().add(point);
        }
        Map<String, Object> randomData = new HashMap<>();
        randomData.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        vo.setData(randomData);

        return vo;
    }

    @Override
    WatchListVO updateVO(WatchListVO existing) {
        WatchListVO copy = (WatchListVO) existing.copy();
        copy.setName(UUID.randomUUID().toString());
        Map<String, Object> randomData = new HashMap<>();
        randomData.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        copy.setData(randomData);
        return copy;
    }

    @Override
    void addReadRoleToFail(Role role, WatchListVO vo) {
        vo.getReadPermission().getRoles().add(Collections.singleton(role));
    }

    @Override
    void addEditRoleToFail(Role role, WatchListVO vo) {
        vo.getEditPermission().getRoles().add(Collections.singleton(role));
    }

    @Test(expected = PermissionException.class)
    @Override
    public void testCreatePrivilegeFails() {
        WatchListVO vo = newVO(editUser);
        addRoleToCreatePermission(PermissionHolder.SUPERADMIN_ROLE);
        removeRoleFromCreatePermission(PermissionHolder.USER_ROLE);
        getService().permissionService.runAs(editUser, () -> {
            service.insert(vo);
        });
    }

    @Override
    String getReadRolesContextKey() {
        return "readPermission";
    }

    @Override
    String getEditRolesContextKey() {
        return "editPermission";
    }
}
