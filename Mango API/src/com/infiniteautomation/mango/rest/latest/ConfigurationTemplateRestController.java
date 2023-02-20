/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.spring.service.ConfigurationTemplateService;
import com.serotonin.json.JsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Class to provide server information
 *
 * @author Rodrigo Medina
 */
@Api(value = "Template Configuration Service")
@RestController
@RequestMapping("/template")
public class ConfigurationTemplateRestController {

    private final Logger log = LoggerFactory.getLogger(ConfigurationTemplateRestController.class);
    private final ConfigurationTemplateService configurationTemplateService;

    @Autowired
    public ConfigurationTemplateRestController(ConfigurationTemplateService configurationTemplateService) {
        this.configurationTemplateService = configurationTemplateService;
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Import template and data file for configuration", notes = "Returns a json String with the mapped template.")
    @RequestMapping(method = RequestMethod.GET, value = "/import")
    public ResponseEntity<String> template(
            @AuthenticationPrincipal PermissionHolder user,
            @RequestParam() String filePath,
            @RequestParam() String templateName) throws IOException,
            JsonException {
        MustacheFactory mf = new DefaultMustacheFactory();
        //"default", "script-examples/mustache/data.csv"
        String fileStore = "default";
        String result = configurationTemplateService.generateTemplate(fileStore, filePath, templateName);
        if (Objects.isNull(result) || result.isBlank()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.noFileProvided"));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
