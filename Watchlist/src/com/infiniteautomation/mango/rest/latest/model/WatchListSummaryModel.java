/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.watchlist.WatchListParameter;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class WatchListSummaryModel extends AbstractVoModel<WatchListVO>  {

    private MangoPermissionModel readPermission;
    private MangoPermissionModel editPermission;
    private String type;
    private String query;
    private List<WatchListParameter> params;
    private Map<String, Object> data;

    public WatchListSummaryModel() { }
    public WatchListSummaryModel(WatchListVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(WatchListVO vo) {
        super.fromVO(vo);
        type = vo.getType();
        query = vo.getQuery();
        params = vo.getParams();
        data = vo.getData();
    }

    @Override
    public WatchListVO toVO() throws ValidationException {
        WatchListVO vo = super.toVO();
        vo.setType(type);
        vo.setQuery(query);
        vo.setParams(params);
        vo.setData(data);
        return vo;
    }

    @Override
    protected WatchListVO newVO() {
        return new WatchListVO();
    }
    public MangoPermissionModel getReadPermission() {
        return readPermission;
    }
    public void setReadPermission(MangoPermissionModel readPermission) {
        this.readPermission = readPermission;
    }
    public MangoPermissionModel getEditPermission() {
        return editPermission;
    }
    public void setEditPermission(MangoPermissionModel editPermission) {
        this.editPermission = editPermission;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
    public List<WatchListParameter> getParams() {
        return params;
    }
    public void setParams(List<WatchListParameter> params) {
        this.params = params;
    }
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
