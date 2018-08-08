/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.service.maintenanceEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.spring.dao.DataSourceDao;
import com.infiniteautomation.mango.spring.dao.MaintenanceEventDao;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventRT;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.RTMDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.exception.NotFoundException;
import com.serotonin.m2m2.vo.exception.ValidationException;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;

import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Service
public class MaintenanceEventsService {
    
    /**
     * Get the full VO if exists and has read permission else throw exception
     * @param xid
     * @return
     * @throws NotFoundException
     */
    public MaintenanceEventVO getFullByXid(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        MaintenanceEventVO vo = MaintenanceEventDao.instance.getFullByXid(xid);
        if(vo == null)
            throw new NotFoundException();
        ensureReadPermission(vo, user);
        return vo;
    }
    
    /**
     * Insert an event, check permissions and validate.  Xid generated if not supplied.
     * @param vo
     * @param user
     * @return
     * @throws PermissionException
     * @throws ValidationException
     */
    public MaintenanceEventVO insert(MaintenanceEventVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        //Ensure they can create an event
        Permissions.ensureDataSourcePermission(user);
        
        //Generate an Xid if necessary
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(MaintenanceEventDao.instance.generateUniqueXid());
        
        vo.ensureValid();
        RTMDefinition.instance.saveMaintenanceEvent(vo);
        return vo;
    }
    
    public MaintenanceEventVO update(String existingXid, MaintenanceEventVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        return update(getFullByXid(existingXid, user), vo, user);
    }
    
    public MaintenanceEventVO update(MaintenanceEventVO existing, MaintenanceEventVO vo, PermissionHolder user) throws NotFoundException, PermissionException, ValidationException {
        ensureEditPermission(existing, user);
        //Don't change ID ever
        vo.setId(existing.getId());
        vo.ensureValid();
        RTMDefinition.instance.saveMaintenanceEvent(vo);
        return vo;
    }
    
    /**
     * Delete an event
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException
     * @throws PermissionException
     */
    public MaintenanceEventVO delete(String xid, PermissionHolder user) throws NotFoundException, PermissionException {
        MaintenanceEventVO vo = getFullByXid(xid, user);
        ensureEditPermission(vo, user);
        RTMDefinition.instance.deleteMaintenanceEvent(vo.getId());
        return vo;
    }
    
    /**
     * Toggle a running maintenance event
     * @param xid
     * @param user
     * @return
     * @throws NotFoundException - if DNE
     * @throws PermissionException - if no toggle permission
     * @throws TranslatableIllegalStateException - if disabled
     */
    public boolean toggle(String xid, PermissionHolder user) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        MaintenanceEventVO existing = MaintenanceEventDao.instance.getByXid(xid);
        if(existing == null)
            throw new NotFoundException();
        ensureTogglePermission(existing, user);
        MaintenanceEventRT rt = RTMDefinition.instance.getRunningMaintenanceEvent(existing.getId());
        boolean activated = false;
        if (rt == null)
            throw new TranslatableIllegalStateException(new TranslatableMessage("maintenanceEvents.toggle.disabled"));
        else
            activated = rt.toggle();
        return activated;
    }
    
    public StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user, Function<MaintenanceEventVO, Object> transformVisit) {
        
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission() || Permissions.hasDataSourcePermission(user)) {
            return new StreamedVOQueryWithTotal<>(MaintenanceEventDao.instance, rql, transformVisit);
        } else {
            return new StreamedVOQueryWithTotal<>(MaintenanceEventDao.instance, rql, item -> {
                if(item.getDataPoints().size() > 0) {
                    DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                    MaintenanceEventDao.instance.getPoints(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                if(item.getDataSources().size() > 0) {
                    DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                    MaintenanceEventDao.instance.getDataSources(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                return true;
            }, transformVisit);
        }
    }
    
    /**
     * Ensure the user can edit this VO
     * @param user
     * @param vo
     */
    public void ensureEditPermission(MaintenanceEventVO vo, PermissionHolder user) {
        if(user.hasAdminPermission())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, false);
                MaintenanceEventDao.instance.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToReadPoints"), user);
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                MaintenanceEventDao.instance.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToEditSources"), user);
            }
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
     * Ensure the permission holder can read the event
     * @param user
     * @param vo
     */
    public void ensureReadPermission(MaintenanceEventVO vo, PermissionHolder user) {
        if(user.hasAdminPermission())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                MaintenanceEventDao.instance.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToReadPoints"), user);
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                MaintenanceEventDao.instance.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToEditSources"), user);
            }
        }
    }
    
    /**
     * Check the permission on the data point and if the user does not have it
     * then cache and check the permission on the data source
     *
     * @author Terry Packer
     */
    static class DataPointPermissionsCheckCallback implements MappedRowCallback<DataPointVO> {

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
                            DataSourceVO<?> newDs = DataSourceDao.instance.get(k);
                            return newDs;
                        });
                        if(!Permissions.hasDataSourcePermission(user, source))
                            hasPermission.setFalse();
                    }
                }else {
                    DataSourceVO<?> source = sources.computeIfAbsent(point.getDataSourceId(), k -> {
                        DataSourceVO<?> newDs = DataSourceDao.instance.get(k);
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
}
