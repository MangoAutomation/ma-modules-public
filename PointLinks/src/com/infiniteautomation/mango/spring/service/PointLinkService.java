/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.pointLinks.PointLinkDao;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.rt.script.ScriptUtils;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Service
public class PointLinkService {

    @Autowired
    private PointLinkDao dao;
    
    /**
     * Get the vo checking permissions and existence
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     */
    public PointLinkVO get(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        PointLinkVO vo = dao.getByXid(xid);
        if(vo == null)
            throw new NotFoundException();
        ensureReadPermission(vo, user);
        return vo;
    }
    
    /**
     * 
     * @param vo
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public PointLinkVO insert(PointLinkVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        //Ensure they can create an event
        Permissions.ensureDataSourcePermission(user);
        
        //Generate an Xid if necessary
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(dao.generateUniqueXid());
        
        vo.ensureValid();
        dao.save(vo);
        ScriptUtils.clearGlobalFunctions();
        return vo;
    }
    
    /**
     * 
     * @param existingXid
     * @param vo
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public PointLinkVO update(String existingXid, PointLinkVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        return update(get(existingXid, user), vo, user);
    }
    
    /**
     * 
     * @param existing
     * @param vo
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public PointLinkVO update(PointLinkVO existing, PointLinkVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        ensureEditPermission(existing, user);
        //Don't change ID ever
        vo.setId(existing.getId());
        vo.ensureValid();
        dao.save(vo);
        ScriptUtils.clearGlobalFunctions();
        return vo;
    }
    
    /**
     * Delete 
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     */
    public PointLinkVO delete(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        PointLinkVO vo = get(xid, user);
        ensureEditPermission(vo, user);
        dao.delete(vo.getId());
        ScriptUtils.clearGlobalFunctions();
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
        //TODO Review permissions access
        if (user.hasAdminPermission()) {
            return new StreamedVOQueryWithTotal<>(dao, rql, transformVO);
        } else {
            return new StreamedVOQueryWithTotal<>(dao, rql, item -> false,  transformVO);
        }
    }
    
    /**
     * For future use if we add permissions to scripts, for now only admin's have permissions
     * @param vo
     * @param user
     */
    public void ensureReadPermission(PointLinkVO vo, PermissionHolder user) throws PermissionException {
        Permissions.ensureHasAdminPermission(user);
    }
    
    /**
     * For future use if we add permissions to scripts, for now only admin's have permissions
     * @param vo
     * @param user
     */
    public void ensureEditPermission(PointLinkVO vo, PermissionHolder user) throws PermissionException {
        Permissions.ensureHasAdminPermission(user);
    }
    
}
