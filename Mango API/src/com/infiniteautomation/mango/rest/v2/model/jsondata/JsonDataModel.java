/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.jsondata;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.json.JsonDataVO;

/**
 *
 * @author Terry Packer
 */
public class JsonDataModel extends AbstractVoModel<JsonDataVO> {

    private String readPermission;
    private String editPermission;
    private boolean publicData;
    private Object jsonData;

    public JsonDataModel(JsonDataVO data) {
        fromVO(data);
    }

    public JsonDataModel() { }

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

    public boolean isPublicData() {
        return publicData;
    }

    public void setPublicData(boolean publicData) {
        this.publicData = publicData;
    }

    public Object getJsonData() {
        return jsonData;
    }

    public void setJsonData(Object jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public void fromVO(JsonDataVO vo) {
        super.fromVO(vo);
        this.readPermission = PermissionService.implodeRoles(vo.getReadRoles());
        this.editPermission = PermissionService.implodeRoles(vo.getEditRoles());
        this.publicData = vo.isPublicData();
        this.jsonData = vo.getJsonData();
    }

    @Override
    public JsonDataVO toVO() throws ValidationException {
        JsonDataVO vo = super.toVO();
        PermissionService service = Common.getBean(PermissionService.class);
        vo.setReadRoles(service.explodeLegacyPermissionGroupsToRoles(readPermission));
        vo.setEditRoles(service.explodeLegacyPermissionGroupsToRoles(editPermission));
        vo.setPublicData(publicData);
        vo.setJsonData(jsonData);
        return vo;
    }

    @Override
    protected JsonDataVO newVO() {
        return new JsonDataVO();
    }

}
