/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.db.dao.UserDao;
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

    private final WatchListService watchListService;
    private final UserDao userDao;
    private final DataPointService dataPointService;

    @Autowired
    public WatchListModelMapping(WatchListService watchListService,
            UserDao userDao,
            DataPointService dataPointService) {
        this.watchListService = watchListService;
        this.userDao = userDao;
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
        model.setUsername(userDao.getXidById(vo.getUserId()));
        model.setReadPermission(new MangoPermissionModel(vo.getReadPermission()));
        model.setEditPermission(new MangoPermissionModel(vo.getEditPermission()));

        //Set the point summaries
        List<WatchListDataPointModel> points = new ArrayList<>();
        watchListService.getWatchListPoints(vo.getId(), (point) -> {
            points.add(new WatchListDataPointModel(point));
        });
        model.setPoints(points);

        return model;
    }

    @Override
    public WatchListVO unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        WatchListModel model = (WatchListModel)from;
        WatchListVO vo = model.toVO();

        Integer userId = userDao.getIdByXid(model.getUsername());
        if(userId != null) {
            vo.setUserId(userId);
        }

        vo.setReadPermission(model.getReadPermission() != null ? model.getReadPermission().getPermission() : new MangoPermission());
        vo.setEditPermission(model.getEditPermission() != null ? model.getEditPermission().getPermission() : new MangoPermission());

        ProcessResult result = new ProcessResult();
        if(model.getPoints() != null) {
            for(WatchListDataPointModel summary : model.getPoints()) {
                try {
                    vo.getPointList().add(dataPointService.getSummary(summary.getXid()));
                }catch(NotFoundException e) {
                    result.addContextualMessage("points", "watchList.validate.pointNotFound", summary.getXid());
                }catch(PermissionException e) {
                    result.addContextualMessage("points", "watchlist.vaildate.pointNoReadPermission", summary.getXid());
                }
            }
        }
        if(result.getHasMessages()) {
            throw new ValidationException(result);
        }

        return vo;
    }
}