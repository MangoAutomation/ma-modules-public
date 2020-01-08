/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.filestore;

import com.fasterxml.jackson.annotation.JsonView;
import com.infiniteautomation.mango.rest.v2.model.RoleViews;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.FileStore;

public class FileStoreModel {

    private int id;
    private String storeName;
    @JsonView(RoleViews.ShowRoles.class)
    private String readPermission;
    @JsonView(RoleViews.ShowRoles.class)
    private String writePermission;

    public FileStoreModel(FileStore vo) {
        fromVO(vo);
    }

    public FileStoreModel() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(String readPermission) {
        this.readPermission = readPermission;
    }

    public String getWritePermission() {
        return writePermission;
    }

    public void setWritePermission(String writePermission) {
        this.writePermission = writePermission;
    }

    public void fromVO(FileStore vo) {
        this.id = vo.getId();
        this.storeName = vo.getStoreName();
        this.readPermission = PermissionService.implodeRoles(vo.getReadRoles());
        this.writePermission = PermissionService.implodeRoles(vo.getWriteRoles());
    }

    public FileStore toVO() {
        FileStore store = new FileStore();
        store.setId(id);
        store.setStoreName(storeName);
        PermissionService service = Common.getBean(PermissionService.class);
        store.setReadRoles(service.explodeLegacyPermissionGroupsToRoles(readPermission));
        store.setWriteRoles(service.explodeLegacyPermissionGroupsToRoles(writePermission));
        return store;
    }

}
