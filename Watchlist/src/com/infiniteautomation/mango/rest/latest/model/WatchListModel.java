/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import java.util.List;

import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class WatchListModel extends WatchListSummaryModel {

    private List<WatchListDataPointModel> points;

    public WatchListModel() { }
    public WatchListModel(WatchListVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(WatchListVO vo) {
        super.fromVO(vo);
    }

    @Override
    public WatchListVO toVO() throws ValidationException {
        WatchListVO vo = super.toVO();
        return vo;
    }
    public List<WatchListDataPointModel> getPoints() {
        return points;
    }
    public void setPoints(List<WatchListDataPointModel> points) {
        this.points = points;
    }
}
