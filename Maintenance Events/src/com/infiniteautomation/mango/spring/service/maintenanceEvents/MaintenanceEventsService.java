/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service.maintenanceEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventRT;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.RTMDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Service
public class MaintenanceEventsService extends AbstractVOService<MaintenanceEventVO, MaintenanceEventDao>{

    private final DataSourceDao<DataSourceVO<?>> dataSourceDao;
    
    @Autowired
    public MaintenanceEventsService(MaintenanceEventDao dao, DataSourceDao<DataSourceVO<?>> dataSourceDao) {
        super(dao);
        this.dataSourceDao = dataSourceDao;
    }

    @Override
    public MaintenanceEventVO insert(MaintenanceEventVO vo, PermissionHolder user, boolean full)
            throws PermissionException, ValidationException {
        //Ensure they can create
        ensureCreatePermission(user);
        
        //Generate an Xid if necessary
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(dao.generateUniqueXid());
        
        ensureValid(vo, user);
        RTMDefinition.instance.saveMaintenanceEvent(vo);
        return vo;
    }
    
    @Override
    protected MaintenanceEventVO update(MaintenanceEventVO existing, MaintenanceEventVO vo,
            PermissionHolder user, boolean full) throws PermissionException, ValidationException {
        ensureEditPermission(user, existing);
        vo.setId(existing.getId());
        ensureValid(vo, user);
        RTMDefinition.instance.saveMaintenanceEvent(vo);
        return vo;
    }
    
    public MaintenanceEventVO update(String existingXid, MaintenanceEventVO vo, PermissionHolder user)
            throws PermissionException, ValidationException {
        return update(getFull(existingXid, user), vo, user);
    } 
    
    /**
     * Delete an event
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     */
    @Override
    public MaintenanceEventVO delete(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        MaintenanceEventVO vo = getFull(xid, user);
        ensureEditPermission(user, vo);
        RTMDefinition.instance.deleteMaintenanceEvent(vo.getId());
        return vo;
    }
    
    /**
     * Toggle a running maintenance event
     * @param xid
     * @param user
     * @return - state of event after toggle
     * @throws NotFoundException - if DNE
     * @throws PermissionException - if no toggle permission
     * @throws TranslatableIllegalStateException - if disabled
     */
    public boolean toggle(String xid, PermissionHolder user) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        MaintenanceEventRT rt = getEventRT(xid, user);
        return rt.toggle();
    }
    
    /**
     * Check if a maintenance event is active
     * @param xid
     * @param user
     * @return - state of event
     * @throws NotFoundException - if DNE
     * @throws PermissionException - if no toggle permission
     * @throws TranslatableIllegalStateException - if disabled
     */
    public boolean isEventActive(String xid, PermissionHolder user) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        MaintenanceEventRT rt = getEventRT(xid, user);
        return rt.isEventActive();
    }
    
    /**
     * Set the state of a Maintenance event, if state does not change do nothing.
     * @param xid
     * @param user
     * @param active
     * @return - state of event, should match active unless event is disabled
     * @throws NotFoundException - if DNE
     * @throws PermissionException - if no toggle permission
     * @throws TranslatableIllegalStateException - if disabled
     */
    public boolean setState(String xid, PermissionHolder user, boolean active) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        MaintenanceEventRT rt = getEventRT(xid, user);
        if(active) {
            //Ensure active
            if(!rt.isEventActive())
                rt.toggle();
            return true;
        }else {
            if(rt.isEventActive())
                rt.toggle();
            return false;
        }
    }
    
    public MaintenanceEventRT getEventRT(String xid, PermissionHolder user) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        MaintenanceEventVO existing = dao.getByXid(xid);
        if(existing == null)
            throw new NotFoundException();
        ensureTogglePermission(existing, user);
        MaintenanceEventRT rt = RTMDefinition.instance.getRunningMaintenanceEvent(existing.getId());
        if (rt == null)
            throw new TranslatableIllegalStateException(new TranslatableMessage("maintenanceEvents.toggle.disabled"));
        return rt;
    }
    
    public StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user, Function<MaintenanceEventVO, Object> transformVO) {
        
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission() || Permissions.hasDataSourcePermission(user)) {
            return new StreamedVOQueryWithTotal<>(dao, rql, transformVO);
        } else {
            return new StreamedVOQueryWithTotal<>(dao, rql, item -> {
                if(item.getDataPoints().size() > 0) {
                    DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                    dao.getPoints(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                if(item.getDataSources().size() > 0) {
                    DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                    dao.getDataSources(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                return true;
            },  transformVO);
        }
    }

    /**
     * Ensure the user has permission to toggle this event
     * @param user
     * @param vo
     */
    public void ensureTogglePermission(MaintenanceEventVO vo, PermissionHolder user) {
        if(user.hasAdminPermission())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else if(!Permissions.hasPermission(user, vo.getTogglePermission()));
            throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToToggleEvent"), user);
    }
    
    
    /**
     * Check the permission on the data point and if the user does not have it
     * then cache and check the permission on the data source
     *
     * @author Terry Packer
     */
    class DataPointPermissionsCheckCallback implements MappedRowCallback<DataPointVO> {

        Map<Integer, DataSourceVO<?>> sources = new HashMap<>();
        MutableBoolean hasPermission = new MutableBoolean(true);
        boolean read;
        PermissionHolder user;
        
        /**
         * 
         * @param read = true to check read permission, false = check edit permission
         */
        public DataPointPermissionsCheckCallback(PermissionHolder user, boolean read) {
            this.user = user;
            this.read = read;
        }
        
        /* (non-Javadoc)
         * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
         */
        @Override
        public void row(DataPointVO point, int index) {
            
            if(!hasPermission.getValue()) {
                //short circuit the logic if we already failed
                return;
            }else {
                if(read) {
                    if(!Permissions.hasDataPointReadPermission(user, point)) {
                        DataSourceVO<?> source = sources.computeIfAbsent(point.getDataSourceId(), k -> {
                            DataSourceVO<?> newDs = dataSourceDao.get(k);
                            return newDs;
                        });
                        if(!Permissions.hasDataSourcePermission(user, source))
                            hasPermission.setFalse();
                    }
                }else {
                    DataSourceVO<?> source = sources.computeIfAbsent(point.getDataSourceId(), k -> {
                        DataSourceVO<?> newDs = dataSourceDao.get(k);
                        return newDs;
                    });
                    if(!Permissions.hasDataSourcePermission(user, source))
                        hasPermission.setFalse();
                }
            }
        }
    }
    
    /**
     * Does the user have edit permission for all data sources
     *
     * @author Terry Packer
     */
    static class DataSourcePermissionsCheckCallback implements MappedRowCallback<DataSourceVO<?>> {

        MutableBoolean hasPermission = new MutableBoolean(true);
        PermissionHolder user;
        
        /**
         * 
         * @param read = true to check read permission, false = check edit permission
         */
        public DataSourcePermissionsCheckCallback(PermissionHolder user) {
            this.user = user;
        }
        
        /* (non-Javadoc)
         * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
         */
        @Override
        public void row(DataSourceVO<?> source, int index) {
            
            if(!hasPermission.getValue()) {
                //short circuit the logic if we already failed
                return;
            }else {
                if(!Permissions.hasDataSourcePermission(user, source))
                    hasPermission.setFalse();
            }
        }
    }

    @Override
    public boolean hasCreatePermission(PermissionHolder user) {
        return Permissions.hasDataSourcePermission(user);
    }

    @Override
    public boolean hasEditPermission(PermissionHolder user, MaintenanceEventVO vo) {
        if(user.hasAdminPermission())
            return true;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return true;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, false);
                dao.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    return false;
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                dao.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasReadPermission(PermissionHolder user, MaintenanceEventVO vo) {
        if(user.hasAdminPermission())
            return true;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return true;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                dao.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    return false;
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                dao.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    return false;
            }
        }
        return true;
    }
}
