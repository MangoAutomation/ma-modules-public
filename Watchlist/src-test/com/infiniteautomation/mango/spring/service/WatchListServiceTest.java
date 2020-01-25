/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.spring.dao.WatchListDao;
import com.infiniteautomation.mango.spring.dao.WatchListTableDefinition;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.watchlist.WatchListCreatePermission;
import com.serotonin.m2m2.watchlist.WatchListSchemaDefinition;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class WatchListServiceTest extends AbstractVOServiceWithPermissionsTest<WatchListVO, WatchListTableDefinition, WatchListDao, WatchListService> {

    @BeforeClass
    public static void setup() {
        List<ModuleElementDefinition> definitions = new ArrayList<>();
        definitions.add(new WatchListSchemaDefinition());
        definitions.add(new WatchListCreatePermission());
        addModule("watchList", definitions);
    }

    @Override
    String getCreatePermissionType() {
        return WatchListCreatePermission.PERMISSION;
    }

    @Override
    void setReadRoles(Set<Role> roles, WatchListVO vo) {
        vo.setReadRoles(roles);
    }

    @Override
    void setEditRoles(Set<Role> roles, WatchListVO vo) {
        vo.setEditRoles(roles);
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
        assertEquals(expected.getUserId(), actual.getUserId());
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
        vo.setUserId(owner.getId());
        for(IDataPoint point : createMockDataPoints(5, false, owner.getRoles(), owner.getRoles())) {
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
        vo.getReadRoles().add(role);
    }

    @Override
    void addEditRoleToFail(Role role, WatchListVO vo) {
        vo.getEditRoles().add(role);
    }

    @Test(expected = PermissionException.class)
    @Override
    public void testCreatePrivilegeFails() {
        WatchListVO vo = newVO(editUser);
        addRoleToCreatePermission(PermissionHolder.SUPERADMIN_ROLE.get());
        getService().permissionService.runAs(editUser, () -> {
            service.insert(vo);
        });
    }
}
