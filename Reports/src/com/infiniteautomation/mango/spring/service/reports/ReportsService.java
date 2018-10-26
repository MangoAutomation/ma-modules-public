/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service.reports;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.ReportPermissionDefinition;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

import net.jazdw.rql.parser.ASTNode;

/**
 * TODO This class is prep for Mango 3.6 that 
 * has a base AbstractVOMangoService class
 * 
 * TODO Add read and edit permissions to the VO for use in this class
 * 
 * @author Terry Packer
 *
 */
@Service
public class ReportsService {
    
    private ReportDao dao;
    
    public ReportsService(@Autowired ReportDao dao) {
        this.dao = dao;
    }
    
    /**
     * 
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO get(String xid, User user) throws NotFoundException, PermissionException, ValidationException {
        return get(xid, user, false);
    }
    
    /**
     * Get relational data too
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO getFull(String xid, User user) throws NotFoundException, PermissionException, ValidationException {
        return get(xid, user, true);
    }
    
    /**
     * 
     * @param xid
     * @param user
     * @param full
     * @return
     */
    protected ReportVO get(String xid, User user, boolean full) {
        ReportVO vo;
        if(full)
            vo = dao.getFullByXid(xid);
        else
            vo = dao.getByXid(xid);
           
        if(vo == null)
            throw new NotFoundException();
        ensureReadPermission(user, vo);
        return vo;
    }
    
    
    /**
     * 
     * @param id
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO get(int id, User user) throws NotFoundException, PermissionException, ValidationException {
        return get(id, user, false);
    }
    
    /**
     * Get relational data too
     * @param id
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO getFull(int id, User user) throws NotFoundException, PermissionException, ValidationException {
        return get(id, user, true);
    }
    
    /**
     * 
     * @param id
     * @param user
     * @param full
     * @return
     */
    protected ReportVO get(int id, User user, boolean full) {
        ReportVO vo;
        if(full)
            vo = dao.getFull(id);
        else
            vo = dao.get(id);
           
        if(vo == null)
            throw new NotFoundException();
        ensureReadPermission(user, vo);
        return vo;
    }
    
    /**
     * Insert a vo with its relational data
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO insertFull(ReportVO vo, PermissionHolder user) throws PermissionException, ValidationException {
        return insert(vo, user, true);
    }
    
    /**
     * Insert a vo without its relational data
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO insert(ReportVO vo, PermissionHolder user) throws PermissionException, ValidationException {
        return insert(vo, user, false);
    }

    
    
    /**
     * 
     * @param vo
     * @param user
     * @param full
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    protected ReportVO insert(ReportVO vo, PermissionHolder user, boolean full) throws PermissionException, ValidationException {
        //Ensure they can create a list
        ensureCreatePermission(user);
        
        //Generate an Xid if necessary
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(dao.generateUniqueXid());
        
        ensureValid(vo, user);
        if(full)
            dao.saveFull(vo);
        else
            dao.save(vo);
        return vo;
    }

    /**
     * Update a vo without its relational data
     * @param existingXid
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO update(String existingXid, ReportVO vo, User user) throws PermissionException, ValidationException {
        return update(get(existingXid, user), vo, user);
    }


    /**
     * Update a vo without its relational data
     * @param existing
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO update(ReportVO existing, ReportVO vo, User user) throws PermissionException, ValidationException {
       return update(existing, vo, user, false);
    }
    
    /**
     * Update a vo and its relational data
     * @param existingXid
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO updateFull(String existingXid, ReportVO vo, User user) throws PermissionException, ValidationException {
        return updateFull(get(existingXid, user), vo, user);
    }


    /**
     * Update a vo and its relational data
     * @param existing
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public ReportVO updateFull(ReportVO existing, ReportVO vo, User user) throws PermissionException, ValidationException {
        return update(existing, vo, user, true);
    }
    
    protected ReportVO update(ReportVO existing, ReportVO vo, User user, boolean full) throws PermissionException, ValidationException {
        ensureEditPermission(user, existing);
        vo.setId(existing.getId());
        ensureValid(vo, user);
        if(full)
            dao.saveFull(vo);
        else
            dao.save(vo);
        return vo;
    }

    
    /**
     * 
     * @param xid
     * @param user
     * @return
     * @throws PermissionException
     */
    public ReportVO delete(String xid, User user) throws PermissionException {
        ReportVO vo = get(xid, user);
        ensureEditPermission(user, vo);
        dao.delete(vo.getId());
        return vo;
    }
    
    /**
     * Query for VOs without returning the relational info
     * @param conditions
     * @param callback
     */
    public void customizedQuery(ConditionSortLimit conditions, MappedRowCallback<ReportVO> callback) {
        dao.customizedQuery(conditions, callback);
    }
    
    /**
     * Query for VOs and load the relational info
     * @param conditions
     * @param callback
     */
    public void customizedQuery(ASTNode conditions, MappedRowCallback<ReportVO> callback) {
        dao.customizedQuery(dao.rqlToCondition(conditions), callback);
    }
    
    /**
     * Query for VOs and load the relational info
     * @param conditions
     * @param callback
     */
    public void customizedQueryFull(ConditionSortLimit conditions, MappedRowCallback<ReportVO> callback) {
        dao.customizedQuery(conditions, (item, index) ->{
            dao.loadRelationalData(item);
            callback.row(item, index);
        });
    }
    
    /**
     * Query for VOs and collect the relational info
     * @param conditions
     * @param callback
     */
    public void customizedQueryFull(ASTNode conditions, MappedRowCallback<ReportVO> callback) {
        dao.customizedQuery(dao.rqlToCondition(conditions), (item, index) ->{
            dao.loadRelationalData(item);
            callback.row(item, index);
        });
    }
    
    /**
     * Count VOs
     * @param conditions
     * @return
     */
    public int customizedCount(ConditionSortLimit conditions) {
        return dao.customizedCount(conditions);
    }
    
    /**
     * Count VOs
     * @param conditions - RQL AST Node
     * @return
     */
    public int customizedCount(ASTNode conditions) {
        return dao.customizedCount(dao.rqlToCondition(conditions));
    }

    /**
     * Can this user create any VOs
     * 
     * @param user
     * @return
     */
    public boolean hasCreatePermission(PermissionHolder user) {
        if(user.hasAdminPermission())
            return true;
        else if(Permissions.hasAnyPermission(user, getReportCreatePermissions()))
            return true;
        else
            return false;
    }
    
    /**
     * Can this user edit this VO
     * 
     * @param user
     * @param vo
     * @return
     */
    public boolean hasEditPermission(User user, ReportVO vo) {
        if(user.hasAdminPermission())
            return true;
        else if(vo.getId() == user.getId())
            return true;
        else
            return false;
    }
    
    /**
     * Can this user read this VO
     * 
     * @param user
     * @param vo
     * @return
     */
    public boolean hasReadPermission(User user, ReportVO vo) {
        if(user.hasAdminPermission())
            return true;
        else if(vo.getId() == user.getId())
            return true;
        else
            return false;
    }

    /**
     * Ensure this user can create a vo
     * 
     * @param user
     * @throws PermissionException
     */
    public void ensureCreatePermission(PermissionHolder user) throws PermissionException {
        user.ensureHasAnyPermission(getReportCreatePermissions());
    }
    
    /**
     * Ensure this user can edit this vo
     * 
     * @param user
     * @param vo
     */
    public void ensureEditPermission(User user, ReportVO vo) throws PermissionException {
        if(user.getId() == vo.getId())
            return;
        user.ensureHasAdminPermission();
    }
    
    /**
     * Ensure this user can read this vo
     * 
     * @param user
     * @param vo
     * @throws PermissionException
     */
    public void ensureReadPermission(User user, ReportVO vo) throws PermissionException {
        if(user.getId() == vo.getId())
            return;
        user.ensureHasAdminPermission();
    }
    
 
    /**
     * Validate the VO
     * @param vo
     * @param user
     */
    protected void ensureValid(ReportVO vo, PermissionHolder user) {
        //TODO Implement me fully by checking permissions etc
        //TODO Copy logic from VO to here when we add edit/read permissions
        //TODO Mango 3.6 the name field is 100 chars long in the database, it should be 255 to be compatible with base class validation
        ProcessResult result = new ProcessResult();
        vo.validate(result);
        if(result.getHasMessages())
            throw new ValidationException(result);
    }
    
    protected Set<String> getReportCreatePermissions() {
        String reportCreatePermissions = SystemSettingsDao.instance.getValue(ReportPermissionDefinition.PERMISSION);
        if(reportCreatePermissions == null)
            return new HashSet<>();
        else
            return Permissions.explodePermissionGroups(reportCreatePermissions);
    }
    
}
