/**
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.serotonin.m2m2.i18n.Translations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.infiniteautomation.mango.db.query.pojo.RQLFilterJsonNode;
import com.infiniteautomation.mango.rest.latest.model.ArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.jsondata.JsonDataModel;
import com.infiniteautomation.mango.rest.latest.resolver.RemainingPath;
import com.infiniteautomation.mango.spring.service.JsonDataService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.json.JsonDataVO;

import net.jazdw.rql.parser.ASTNode;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Jared Wiltshire
 */
@RestController
@RequestMapping("/json")
public class JsonRestController {

    private final JsonDataService jsonDataService;

    @Autowired
    public JsonRestController(JsonDataService jsonDataService) {
        this.jsonDataService = jsonDataService;
    }

    @RequestMapping(method = RequestMethod.GET, value="/data/{xid}")
    public JsonNode getData(@PathVariable String xid, HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        return this.jsonDataService.getDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.GET, value="/data/{xid}/**")
    public JsonNode getDataAtPointer(@PathVariable String xid, @ApiIgnore @RemainingPath String path) throws UnsupportedEncodingException {
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
    public JsonNode setDataAtPointer(@PathVariable String xid, @ApiIgnore @RemainingPath String path, @RequestBody JsonNode data) throws UnsupportedEncodingException {
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
    public JsonNode setDataAtPointer(@PathVariable String xid, @ApiIgnore @RemainingPath String path) throws UnsupportedEncodingException {
        String pointer = "/" + path;
        return this.jsonDataService.deleteDataAtPointer(xid, pointer);
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}")
    public ArrayWithTotal<Stream<JsonNode>> query(@PathVariable String xid, HttpServletRequest request, ASTNode rql) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pointer = path.endsWith("/") ? "/" : "";
        ArrayNode items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        return new FilteredStreamWithTotal<>(items, new RQLFilterJsonNode(rql));
    }

    @RequestMapping(method = RequestMethod.GET, value="/query/{xid}/**")
    public ArrayWithTotal<Stream<JsonNode>> queryAtPointer(@PathVariable String xid, @ApiIgnore @RemainingPath String path, ASTNode rql) throws UnsupportedEncodingException {
        String pointer = "/" + path;
        ArrayNode items = this.jsonDataService.valuesForDataAtPointer(xid, pointer);
        return new FilteredStreamWithTotal<>(items, new RQLFilterJsonNode(rql));
    }

    @RequestMapping(method = RequestMethod.GET, value="/store/{xid}")
    public JsonDataModel getStore(@PathVariable String xid, @RequestParam(required = false, defaultValue = "false") boolean withData) {
        JsonDataVO vo = this.jsonDataService.get(xid);
        if (!withData) {
            vo.setJsonData(null);
        }
        return new JsonDataModel(vo);
    }

    @RequestMapping(method = RequestMethod.POST, value="/store")
    public JsonDataModel createStore(@RequestBody JsonDataModel body, @RequestParam(required = false, defaultValue = "false") boolean withData) {
        JsonDataVO create = body.toVO();
        if (!withData) {
            create.setJsonData(null);
        }
        JsonDataVO vo = this.jsonDataService.insert(create);
        if (!withData) {
            vo.setJsonData(null);
        }
        return new JsonDataModel(vo);
    }

    @RequestMapping(method = RequestMethod.PUT, value="/store/{xid}")
    public JsonDataModel updateStore(@PathVariable String xid, @RequestBody JsonDataModel body, @RequestParam(required = false, defaultValue = "false") boolean withData) {
        JsonDataVO update = body.toVO();
        JsonDataVO vo;
        if (withData) {
            vo = this.jsonDataService.update(xid, update);
        } else {
            vo = this.jsonDataService.updateStore(xid, update);
        }
        if (!withData) {
            vo.setJsonData(null);
        }
        return new JsonDataModel(vo);
    }

    @RequestMapping(method = RequestMethod.DELETE, value="/store/{xid}")
    public JsonDataModel deleteStore(@PathVariable String xid, @RequestParam(required = false, defaultValue = "false") boolean withData) {
        JsonDataVO vo = this.jsonDataService.delete(xid);
        if (!withData) {
            vo.setJsonData(null);
        }
        return new JsonDataModel(vo);
    }
}
