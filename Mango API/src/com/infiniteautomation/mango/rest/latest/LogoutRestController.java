/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.web.mvc.spring.security.permissions.AnonymousAccess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value = "Logout")
@RestController
@RequestMapping("/logout")
public class LogoutRestController {

    /**
     * POST Logout action
     */
    @ApiOperation(value = "Logout", notes = "Perform logout using POST")
    @RequestMapping(method = RequestMethod.POST)
    @AnonymousAccess
    public ResponseEntity<Void> logoutPost() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
