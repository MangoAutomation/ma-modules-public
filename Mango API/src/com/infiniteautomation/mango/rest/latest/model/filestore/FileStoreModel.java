/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.filestore;

import com.fasterxml.jackson.annotation.JsonView;
import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.latest.model.RoleViews;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.FileStore;

public class FileStoreModel extends AbstractVoModel<FileStore> {

    private boolean builtIn;
    @JsonView(RoleViews.ShowRoles.class)
    private MangoPermissionModel readPermission;
    @JsonView(RoleViews.ShowRoles.class)
    private MangoPermissionModel writePermission;

    public FileStoreModel(FileStore vo) {
        fromVO(vo);
    }

    public FileStoreModel() { }

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

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    @Override
    public void fromVO(FileStore vo) {
        super.fromVO(vo);
        this.readPermission = new MangoPermissionModel(vo.getReadPermission());
        this.writePermission = new MangoPermissionModel(vo.getWritePermission());
        this.builtIn = vo.isBuiltIn();
    }

    @Override
    public FileStore toVO() throws ValidationException {
        FileStore store = super.toVO();
        store.setReadPermission(readPermission != null ? readPermission.getPermission() : null);
        store.setWritePermission(writePermission != null ? writePermission.getPermission() : null);
        return store;
    }

    @Override
    protected FileStore newVO() {
        return new FileStore();
    }

}
