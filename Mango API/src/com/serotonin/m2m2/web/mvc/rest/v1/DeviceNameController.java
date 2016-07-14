/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Jared Wiltshire
 */
@Api(value="Device Names", description="Device Names")
@RestController
@RequestMapping("/v1/device-names")
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
    @RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<Set<String>> deviceNames(HttpServletRequest request) {
        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
        final User user = this.checkUser(request, result);
        if (result.isOk()) {
            Set<String> deviceNames = DaoRegistry.dataPointDao.getDeviceNames(user);
            return result.createResponseEntity(deviceNames);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "List device names",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/by-data-source-id/{id}")
    public ResponseEntity<Set<String>> deviceNamesByDataSourceId(
            @PathVariable int id,
            HttpServletRequest request) {
        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
        final User user = this.checkUser(request, result);
        if (result.isOk()) {
            Set<String> deviceNames = DaoRegistry.dataPointDao.getDeviceNames(user, id);
            return result.createResponseEntity(deviceNames);
        }
        return result.createResponseEntity();
    }
}
