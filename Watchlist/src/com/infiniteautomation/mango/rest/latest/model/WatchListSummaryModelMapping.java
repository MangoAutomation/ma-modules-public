/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
@Component
public class WatchListSummaryModelMapping implements RestModelMapping<WatchListVO, WatchListSummaryModel> {

    private final UserDao userDao;

    @Autowired
    public WatchListSummaryModelMapping(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Class<? extends WatchListVO> fromClass() {
        return WatchListVO.class;
    }

    @Override
    public Class<? extends WatchListSummaryModel> toClass() {
        return WatchListSummaryModel.class;
    }

    @Override
    public WatchListSummaryModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        WatchListVO vo = (WatchListVO)from;
        WatchListSummaryModel model = new WatchListSummaryModel(vo);
        model.setUsername(userDao.getXidById(vo.getUserId()));
        model.setReadPermission(new MangoPermissionModel(vo.getReadPermission()));
        model.setEditPermission(new MangoPermissionModel(vo.getEditPermission()));
        return model;
    }

    @Override
    public WatchListVO unmap(Object from, PermissionHolder user, RestModelMapper mapper)
            throws ValidationException {
        WatchListSummaryModel model = (WatchListSummaryModel)from;
        WatchListVO vo = model.toVO();

        Integer userId = userDao.getIdByXid(model.getUsername());
        if(userId != null) {
            vo.setUserId(userId);
        }

        vo.setReadPermission(model.getReadPermission() != null ? model.getReadPermission().getPermission() : new MangoPermission());
        vo.setEditPermission(model.getEditPermission() != null ? model.getEditPermission().getPermission() : new MangoPermission());

        return vo;
    }
}