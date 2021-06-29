/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.role;

import java.util.Set;

import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.role.RoleVO;

/**
 * @author Terry Packer
 *
 */
public class RoleModel extends AbstractVoModel<RoleVO> {

    private Set<String> inherited;

    public RoleModel(RoleVO vo) {
        fromVO(vo);
    }

    public RoleModel() {

    }

    @Override
    public void fromVO(RoleVO vo) {
        super.fromVO(vo);
    }

    @Override
    public RoleVO toVO() throws ValidationException {
        return new RoleVO(id == null ? Common.NEW_ID : id, xid, name);
    }

    @Override
    protected RoleVO newVO() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Set<String> getInherited() {
        return inherited;
    }

    public void setInherited(Set<String> inherited) {
        this.inherited = inherited;
    }

}
