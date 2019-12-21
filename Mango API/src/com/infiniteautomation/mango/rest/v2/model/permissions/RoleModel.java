/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.permissions;

import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.vo.RoleVO;

/**
 * Used to edit the Roles themselves.  To represent a role inside a permission on 
 *   a VO use a String and map it to the RoleVO
 * 
 * @author Terry Packer
 *
 */
public class RoleModel extends AbstractVoModel<RoleVO> {

    @Override
    protected RoleVO newVO() {
        return new RoleVO();
    }
}
