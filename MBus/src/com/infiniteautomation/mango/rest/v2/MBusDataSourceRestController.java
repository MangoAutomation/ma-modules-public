/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.service.DataSourceService;

import io.swagger.annotations.Api;

/**
 * @author Terry Packer
 *
 */
@Api(value = "MBus data sources utilities")
@RestController()
@RequestMapping("/mbus-data-sources")
public class MBusDataSourceRestController {

    private final DataSourceService<?> service;
    
    @Autowired
    public MBusDataSourceRestController(DataSourceService<?> service) {
        this.service = service;
    }
    
    //TODO add temporary resource for test utility
}
