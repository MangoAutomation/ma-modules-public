/**
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.infiniteautomation.mango.db.query.pojo.RQLFilterJsonNode;
import com.infiniteautomation.mango.rest.v2.model.ArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.spring.service.JsonDataService;
import com.infiniteautomation.mango.util.RQLUtils;

import net.jazdw.rql.parser.ASTNode;

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
    public JsonNode setData(@PathVariable String xid, HttpServletRequest request, @RequestBody JsonNode data) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        this.jsonDataService.setDataAtPointer(xid, pointer, data);
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value="/data/{xid}/**")
    public JsonNode setDataAtPointer(@PathVariable String xid, HttpServletRequest request, @RequestBody JsonNode data) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        this.jsonDataService.setDataAtPointer(xid, pointer, data);
        return data;
    }

    @RequestMapping(method = RequestMethod.DELETE, value="/data/{xid}")
    public JsonNode deleteDataAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        return this.jsonDataService.deleteDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.DELETE, value="/data/{xid}/**")
    public JsonNode setDataAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        return this.jsonDataService.deleteDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}")
    public ArrayWithTotal<Stream<JsonNode>> query(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        ArrayNode items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return new FilteredStreamWithTotal<>(items, new RQLFilterJsonNode(rql));
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}/**")
    public ArrayWithTotal<Stream<JsonNode>> queryAtPointer(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = this.requestUtils.extractRemainingPath(request);
        String pointer = "/" + path;
        ArrayNode items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return new FilteredStreamWithTotal<>(items, new RQLFilterJsonNode(rql));
    }
}
