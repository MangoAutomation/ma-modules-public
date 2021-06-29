/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.watchlist.WatchListVO.WatchListType;

/**
 *
 * @author Terry Packer
 */
@Component
public class WatchListModelMapping implements RestModelMapping<WatchListVO, WatchListModel> {

    private final WatchListService watchListService;
    private final DataPointService dataPointService;

    @Autowired
    public WatchListModelMapping(WatchListService watchListService,
            DataPointService dataPointService) {
        this.watchListService = watchListService;
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
        model.setReadPermission(new MangoPermissionModel(vo.getReadPermission()));
        model.setEditPermission(new MangoPermissionModel(vo.getEditPermission()));

        if (vo.getType() == WatchListType.STATIC) {
            //Set the point summaries
            model.setPoints(vo.getPointList().stream()
                    .map(WatchListDataPointModel::new)
                    .collect(Collectors.toList()));
        }

        return model;
    }

    @Override
    public WatchListVO unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        WatchListModel model = (WatchListModel)from;
        WatchListVO vo = model.toVO();

        vo.setReadPermission(model.getReadPermission() != null ? model.getReadPermission().getPermission() : new MangoPermission());
        vo.setEditPermission(model.getEditPermission() != null ? model.getEditPermission().getPermission() : new MangoPermission());

        if (vo.getType() == WatchListType.STATIC && model.getPoints() != null) {
            vo.setPointList(model.getPoints().stream().map(m -> {
                DataPointSummary summary = new DataPointSummary();
                summary.setXid(m.getXid());
                return summary;
            }).collect(Collectors.toList()));
        }

        return vo;
    }
}