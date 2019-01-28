/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.pointLinks.PointLinkDao;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Service
public class PointLinkService extends AbstractVOService<PointLinkVO, PointLinkDao> {

    private final MangoJavaScriptService service;
    
    @Autowired    
    public PointLinkService(PointLinkDao dao, MangoJavaScriptService service) {
        super(dao);
        this.service = service;
    }
    
    @Override
    protected PointLinkVO insert(PointLinkVO vo, PermissionHolder user, boolean full)
            throws PermissionException, ValidationException {
        PointLinkVO created = super.insert(vo, user, full);
        service.clearGlobalFunctions();
        return created;
    }
    
    @Override
    protected PointLinkVO update(PointLinkVO existing, PointLinkVO vo, PermissionHolder user,
            boolean full) throws PermissionException, ValidationException {
        PointLinkVO updated = super.update(existing, vo, user, full);
        service.clearGlobalFunctions();
        return updated;
    }

    @Override
    public PointLinkVO delete(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        PointLinkVO vo = super.delete(xid, user);
        service.clearGlobalFunctions();
        return vo;
    }
    
    
    
    /**
     * Perform a query, only admin users will get results
     * @param rql
     * @param user
     * @param transformVO
     * @return
     */
    public StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user, Function<PointLinkVO, Object> transformVO) {
        //If we are admin or have overall data source permission we can view all
        return new StreamedVOQueryWithTotal<>(dao, rql, item -> { return hasReadPermission(user, item);},  transformVO);
    }

    @Override
    public boolean hasCreatePermission(PermissionHolder user) {
        //Ensure they can create an event
        return Permissions.hasDataSourcePermission(user);
    }

    @Override
    public boolean hasEditPermission(PermissionHolder user, PointLinkVO vo) {
        return Permissions.hasDataSourcePermission(user);
    }

    @Override
    public boolean hasReadPermission(PermissionHolder user, PointLinkVO vo) {
        return Permissions.hasDataSourcePermission(user);
    }
    
}
