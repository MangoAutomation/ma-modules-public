/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.rest.v2.model;

import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.watchlist.WatchListParameter;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class WatchListSummaryModel extends AbstractVoModel<WatchListVO>  {

    private String username;
    private String readPermission;
    private String editPermission;
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
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getReadPermission() {
        return readPermission;
    }
    public void setReadPermission(String readPermission) {
        this.readPermission = readPermission;
    }
    public String getEditPermission() {
        return editPermission;
    }
    public void setEditPermission(String editPermission) {
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
