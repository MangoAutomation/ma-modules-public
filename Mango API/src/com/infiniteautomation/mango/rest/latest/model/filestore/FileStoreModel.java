/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.filestore;

import com.fasterxml.jackson.annotation.JsonView;
import com.infiniteautomation.mango.rest.latest.model.RoleViews;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.serotonin.m2m2.vo.FileStore;

public class FileStoreModel {

    private int id;
    private String storeName;
    @JsonView(RoleViews.ShowRoles.class)
    private MangoPermissionModel readPermission;
    @JsonView(RoleViews.ShowRoles.class)
    private MangoPermissionModel writePermission;

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

    public MangoPermissionModel getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(MangoPermissionModel readPermission) {
        this.readPermission = readPermission;
    }

    public MangoPermissionModel getWritePermission() {
        return writePermission;
    }

    public void setWritePermission(MangoPermissionModel writePermission) {
        this.writePermission = writePermission;
    }

    public void fromVO(FileStore vo) {
        this.id = vo.getId();
        this.storeName = vo.getStoreName();
        this.readPermission = new MangoPermissionModel(vo.getReadPermission());
        this.writePermission = new MangoPermissionModel(vo.getWritePermission());
    }

    public FileStore toVO() {
        FileStore store = new FileStore();
        store.setId(id);
        store.setStoreName(storeName);

        store.setReadPermission(readPermission != null ? readPermission.getPermission() : null);
        store.setWritePermission(writePermission != null ? writePermission.getPermission() : null);
        return store;
    }

}
