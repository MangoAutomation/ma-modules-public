/**
 * Copyright (C) 20202 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.v2.model.DefaultListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.spring.service.JsonDataService;

/**
 * @author Jared Wiltshire
 */
@RestController
@RequestMapping("/json")
public class JsonRestController {

    private final JsonDataService jsonDataService;
    private final RequestUtils requestUtils;

    @Autowired
    public JsonRestController(JsonDataService jsonDataService, RequestUtils requestUtils) {
        this.jsonDataService = jsonDataService;
        this.requestUtils = requestUtils;
    }

    @RequestMapping(method = RequestMethod.GET, value="/data/{xid}")
    public JsonNode getData(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        return this.jsonDataService.getDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.GET, value="/data/{xid}/**")
    public JsonNode getDataAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        return this.jsonDataService.getDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.POST, value="/data/{xid}")
    public void setData(@PathVariable String xid, HttpServletRequest request, @RequestBody JsonNode data) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        this.jsonDataService.setDataAtPointer(xid, pointer, data);
    }

    @RequestMapping(method = RequestMethod.POST, value="/data/{xid}/**")
    public void setDataAtPointer(@PathVariable String xid, HttpServletRequest request, @RequestBody JsonNode data) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        this.jsonDataService.setDataAtPointer(xid, pointer, data);
    }

    @RequestMapping(method = RequestMethod.DELETE, value="/data/{xid}")
    public void deleteDataAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        this.jsonDataService.deleteDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.DELETE, value="/data/{xid}/**")
    public void setDataAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        this.jsonDataService.deleteDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}")
    public ListWithTotal<JsonNode> query(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        List<JsonNode> items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        return new DefaultListWithTotal<>(items);
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}/**")
    public ListWithTotal<JsonNode> queryAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        List<JsonNode> items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        return new DefaultListWithTotal<>(items);
    }
}
