/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.rest.v2.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
@Component
public class WatchListModelMapping implements RestModelMapping<WatchListVO, WatchListModel> {

    private final UsersService userService;
    private final PermissionService permissionService;
    private final DataPointService dataPointService;

    @Autowired
    public WatchListModelMapping(PermissionService permissionService,
            UsersService userService,
            DataPointService dataPointService) {
        this.permissionService = permissionService;
        this.userService = userService;
        this.dataPointService = dataPointService;
    }

    @Override
    public Class<? extends WatchListVO> fromClass() {
        return WatchListVO.class;
    }

    @Override
    public Class<? extends WatchListModel> toClass() {
        return WatchListModel.class;
    }

    @Override
    public WatchListModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        WatchListVO vo = (WatchListVO)from;
        WatchListModel model = new WatchListModel(vo);
        model.setUsername(userService.getDao().getXidById(vo.getUserId()));
        model.setReadPermission(PermissionService.implodeRoles(vo.getReadRoles()));
        model.setEditPermission(PermissionService.implodeRoles(vo.getEditRoles()));

        return model;
    }

    @Override
    public WatchListVO unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        WatchListModel model = (WatchListModel)from;
        WatchListVO vo = model.toVO();

        Integer userId = userService.getDao().getIdByXid(model.getUsername());
        if(userId != null) {
            vo.setUserId(userId);
        }

        vo.setReadRoles(permissionService.explodeLegacyPermissionGroupsToRoles(model.getReadPermission()));
        vo.setEditRoles(permissionService.explodeLegacyPermissionGroupsToRoles(model.getEditPermission()));

        ProcessResult result = new ProcessResult();
        for(WatchListDataPointModel summary : model.getPoints()) {
            try {
                vo.getPointList().add(dataPointService.getSummary(summary.getXid()));
            }catch(NotFoundException e) {
                result.addContextualMessage("points", "watchList.validate.pointNotFound", summary.getXid());
            }catch(PermissionException e) {
                result.addContextualMessage("points", "watchlist.vaildate.pointNoReadPermission", summary.getXid());
            }
        }
        if(result.getHasMessages()) {
            throw new ValidationException(result);
        }

        return vo;
    }
}