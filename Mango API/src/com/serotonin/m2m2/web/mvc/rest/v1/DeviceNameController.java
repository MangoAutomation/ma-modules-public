/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Jared Wiltshire
 */
@Api(value="Device Names", description="Device Names")
@RestController
@RequestMapping("/device-names")
public class DeviceNameController extends MangoRestController {

    @SuppressWarnings("unused")
    private final Log logger;

    public DeviceNameController() {
        logger = LogFactory.getLog(getClass());
    }

    @ApiOperation(
            value = "List device names",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Set<String>> deviceNames(
            @RequestParam(value="contains", required=false) String contains,
            HttpServletRequest request) {
        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
        final User user = this.checkUser(request, result);
        if (result.isOk()) {
            DeviceAndPermissionsCallback callback = new DeviceAndPermissionsCallback(user);
            if (contains != null) {
                DataPointDao.getInstance().query(SELECT_DEVICENAME + " WHERE pt.deviceName LIKE ?",
                        new Object[] {"%" + contains + "%"}, DEVICE_AND_PERMISSION_MAPPER, callback);
            } else {
                DataPointDao.getInstance().query(SELECT_DEVICENAME,
                        new Object[] {}, DEVICE_AND_PERMISSION_MAPPER, callback);
            }
            return result.createResponseEntity(callback.results);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "List device names by data source ID",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET, value = "/by-data-source-id/{id}")
    public ResponseEntity<Set<String>> deviceNamesByDataSourceId(
            @PathVariable int id,
            @RequestParam(value="contains", required=false) String contains,
            HttpServletRequest request) {
        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
        final User user = this.checkUser(request, result);
        if (result.isOk()) {
            DeviceAndPermissionsCallback callback = new DeviceAndPermissionsCallback(user);
            if (contains != null) {
                DataPointDao.getInstance().query(SELECT_DEVICENAME + " WHERE pt.dataSourceId=? AND pt.deviceName LIKE ?",
                        new Object[] {id, "%" + contains + "%"}, DEVICE_AND_PERMISSION_MAPPER, callback);
            } else {
                DataPointDao.getInstance().query(SELECT_DEVICENAME + " WHERE pt.dataSourceId=?",
                        new Object[] {id}, DEVICE_AND_PERMISSION_MAPPER, callback);
            }
            return result.createResponseEntity(callback.results);
        }
        return result.createResponseEntity();
    }
    
    @ApiOperation(
            value = "List device names by data source XID",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET, value = "/by-data-source-xid/{xid}")
    public ResponseEntity<Set<String>> deviceNamesByDataSourceXid(
            @PathVariable String xid,
            @RequestParam(value="contains", required=false) String contains,
            HttpServletRequest request) {
        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
        final User user = this.checkUser(request, result);
        if (result.isOk()) {
            DeviceAndPermissionsCallback callback = new DeviceAndPermissionsCallback(user);
            if (contains != null) {
                DataPointDao.getInstance().query(SELECT_DEVICENAME + " WHERE ds.xid=? AND pt.deviceName LIKE ?",
                        new Object[] {xid, "%" + contains + "%"}, DEVICE_AND_PERMISSION_MAPPER, callback);
            } else {
                DataPointDao.getInstance().query(SELECT_DEVICENAME + " WHERE ds.xid=?",
                        new Object[] {xid}, DEVICE_AND_PERMISSION_MAPPER, callback);
            }
            return result.createResponseEntity(callback.results);
        }
        return result.createResponseEntity();
    }
    
    /*
     * TODO Remove the below classes and methods and use the functions in DataPointDao for 2.8.x
     */
    
    private static final String SELECT_DEVICENAME = "SELECT pt.deviceName, pt.readPermission, pt.setPermission, ds.editPermission FROM dataPoints AS pt LEFT JOIN dataSources AS ds ON pt.dataSourceId = ds.id";
    
    private static class DeviceAndPermissions {
        String deviceName;
        String readPermission;
        String setPermission;
        String dataSourcePermissions;
    }
    
    private static class DeviceAndPermissionsCallback implements MappedRowCallback<DeviceAndPermissions> {
        User user;
        Set<String> results;

        DeviceAndPermissionsCallback(User user) {
            this.user = user;
            this.results = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        }
        
        @Override
        public void row(DeviceAndPermissions item, int index) {
            if (Permissions.hasPermission(user, item.readPermission) || Permissions.hasPermission(user, item.setPermission) || Permissions.hasPermission(user, item.dataSourcePermissions)) 
                results.add(item.deviceName);
        }
    }
    
    private final static RowMapper<DeviceAndPermissions> DEVICE_AND_PERMISSION_MAPPER = new RowMapper<DeviceAndPermissions>() {
        @Override
        public DeviceAndPermissions mapRow(ResultSet rs, int rowNum) throws SQLException {
            DeviceAndPermissions dp = new DeviceAndPermissions();
            dp.deviceName = rs.getString(1);
            dp.readPermission = rs.getString(2);
            dp.setPermission = rs.getString(3);
            dp.dataSourcePermissions = rs.getString(4);
            return dp;
        }
    };
}
