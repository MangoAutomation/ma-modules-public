/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.jsondata.JsonDataModel;
import com.infiniteautomation.mango.spring.service.JsonDataService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.json.JsonDataVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 *
 * NoSQL Endpoint for storage of String data and JSON Objects
 *
 * nosql/{data-id}/{data}
 *
 * @author Terry Packer
 *
 */
@Api(value="JSON Store")
@RestController
@RequestMapping("/json-data")
public class JsonDataRestController {

    enum MapOperation {
        APPEND, REPLACE, DELETE
    }

    private final BiFunction<JsonDataVO, User, JsonDataModel> map = (vo, user) -> {return new JsonDataModel(vo);};
    private final JsonDataService service;
    private final PermissionService permissionService;

    @Autowired
    public JsonDataRestController(JsonDataService service, PermissionService permissionService) {
        this.service = service;
        this.permissionService = permissionService;
    }

    @ApiOperation(
            value = "List all available xids",
            notes = "Shows any xids that you have read permissions for"
            )
    @RequestMapping(method = RequestMethod.GET)
    public List<String> list(){
        List<JsonDataVO> all = service.getAll();
        List<String> xids = new ArrayList<>(all.size());
        for(JsonDataVO vo : all) {
            xids.add(vo.getXid());
        }
        return xids;
    }

    @ApiOperation(value = "Get Public JSON Data")
    @RequestMapping(method = RequestMethod.GET, value="/public/{xid}")
    public JsonDataModel getPublicData(
            HttpServletRequest request,
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user){
        return this.map.apply(service.get(xid), user);
    }

    @ApiOperation(value = "Get JSON Data")
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public JsonDataModel getData(
            HttpServletRequest request,
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) throws UnsupportedEncodingException{
        return getDataWithPath(request, xid, null, user);
    }

    @ApiOperation(
            value = "Get JSON Data using a path",
            notes = "To get a sub component of the data use a path of member.submember"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/{path:.*}")
    public JsonDataModel getDataWithPath(
            HttpServletRequest request,

            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path,
            @AuthenticationPrincipal User user) throws UnsupportedEncodingException {

        JsonDataVO vo = service.get(xid);
        String[] pathParts = splitAndDecodePath(path);
        if (pathParts.length != 0) {
            JsonNode data = vo.getJsonData();
            JsonNode subNode = getNode(data, pathParts);
            vo.setJsonData(subNode);
        }
        return this.map.apply(vo, user);
    }

    @ApiOperation(
            value = "Append JSON Data to existing"
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<JsonDataModel> updateJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Read Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> readPermission,

            @ApiParam(value = "Edit Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> editPermission,

            @ApiParam(value = "Name", required = true, allowMultiple = false, defaultValue="")
            @RequestParam(required=false, defaultValue="") String name,

            @ApiParam(value = "Is public?", required = true, allowMultiple = false, defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean publicData,

            @ApiParam( value = "Data to save")
            @RequestBody(required=true)
            JsonNode data,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        return modifyJsonData(
                MapOperation.APPEND,
                xid,
                new String[] {},
                readPermission,
                editPermission,
                name,
                publicData,
                data,
                user,
                builder,
                request, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Append JSON Data to existing",
            notes = "{path} is the path to data with dots data.member.submember"
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}/{path:.*}")
    public ResponseEntity<JsonDataModel> updateJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path,

            @ApiParam(value = "Read Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> readPermission,

            @ApiParam(value = "Edit Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> editPermission,

            @ApiParam(value = "Name", required = true, allowMultiple = false, defaultValue="")
            @RequestParam(required=false, defaultValue="") String name,

            @ApiParam(value = "Is public?", required = true, allowMultiple = false, defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean publicData,

            @ApiParam( value = "Data to save")
            @RequestBody(required=true)
            JsonNode data,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws UnsupportedEncodingException {
        String[] pathParts = splitAndDecodePath(path);
        return modifyJsonData(
                MapOperation.APPEND,
                xid,
                pathParts,
                readPermission,
                editPermission,
                name,
                publicData,
                data,
                user,
                builder, request, HttpStatus.OK);
    }


    @ApiOperation(
            value = "Create/replace JSON Data"
            )
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value="/{xid}")
    public ResponseEntity<JsonDataModel> createJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Read Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> readPermission,

            @ApiParam(value = "Edit Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> editPermission,

            @ApiParam(value = "Name", required = true, allowMultiple = false, defaultValue="")
            @RequestParam(required=false, defaultValue="") String name,

            @ApiParam(value = "Is public?", required = true, allowMultiple = false, defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean publicData,

            @ApiParam( value = "Data to save")
            @RequestBody(required=true)
            JsonNode data,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        return modifyJsonData(MapOperation.REPLACE,
                xid,
                new String[] {},
                readPermission,
                editPermission,
                name,
                publicData, data,
                user, builder, request, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Replace JSON Data",
            notes = "{path} is the path to data with dots data.member.submember"
            )
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value="/{xid}/{path:.*}")
    public ResponseEntity<JsonDataModel> replaceJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path,

            @ApiParam(value = "Read Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> readPermission,

            @ApiParam(value = "Edit Permissions", required = false, defaultValue="", allowMultiple = true)
            @RequestParam(required=false, defaultValue="") Set<String> editPermission,

            @ApiParam(value = "Name", required = true, allowMultiple = false, defaultValue="")
            @RequestParam(required=false, defaultValue="") String name,

            @ApiParam(value = "Is public?", required = true, allowMultiple = false, defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean publicData,

            @ApiParam( value = "Data to save")
            @RequestBody(required=true)
            JsonNode data,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws UnsupportedEncodingException {

        String[] pathParts = splitAndDecodePath(path);
        return modifyJsonData(MapOperation.REPLACE,
                xid,
                pathParts,
                readPermission,
                editPermission,
                name,
                publicData,
                data, user,
                builder, request, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Fully Delete JSON Data")
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public JsonDataModel deleteJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return deletePartialJsonData(xid, null, user);
    }

    @ApiOperation(value = "Partially Delete JSON Data",
            notes = "{path} is the path to data with dots data.member.submember")
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}/{path:.*}")
    public JsonDataModel deletePartialJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path,
            @AuthenticationPrincipal User user) {

        String[] pathParts;
        if (path == null || (pathParts = path.split("\\.")).length == 0) {
            //Delete the whole thing
            return map.apply(this.service.delete(xid), user);
        } else {
            JsonDataVO vo = service.get(xid);
            //Delete something from the map
            JsonNode existingData = vo.getJsonData();
            boolean deleted = deleteNode(existingData, pathParts);
            if (!deleted) {
                throw new NotFoundException();
            }
            return map.apply(service.update(xid, vo), user);
        }
    }

    private String[] splitAndDecodePath(String path) throws UnsupportedEncodingException {
        String[] pathParts = path != null ? path.split("\\.") : new String[] {};
        for (int i = 0; i < pathParts.length; i++) {
            pathParts[i] = URLDecoder.decode(pathParts[i], "UTF-8");
        }
        return pathParts;
    }

    /**
     * Helper method
     * @param operation
     * @param xid
     * @param pathParts
     * @param readPermissions
     * @param editPermissions
     * @param name
     * @param publicData
     * @param data
     * @param builder
     * @param request
     * @param user
     * @return
     */
    private ResponseEntity<JsonDataModel> modifyJsonData(MapOperation operation,
            String xid, String[] pathParts, Set<String> readPermissions, Set<String> editPermissions, String name, boolean publicData,
            JsonNode data, User user, UriComponentsBuilder builder, HttpServletRequest request, HttpStatus successStatus) {

        // check we are using this method only for replace and append
        if (operation != MapOperation.REPLACE && operation != MapOperation.APPEND) throw new IllegalArgumentException();

        JsonNode dataToReturn = data;
        JsonDataVO vo;
        try{
            vo = service.get(xid);
            //Replace the data
            vo.setName(name);
            vo.setPublicData(publicData);
            vo.setReadRoles(permissionService.explodeLegacyPermissionGroupsToRoles(readPermissions));
            vo.setEditRoles(permissionService.explodeLegacyPermissionGroupsToRoles(editPermissions));

            JsonNode existingData = vo.getJsonData();

            if (operation == MapOperation.REPLACE) {
                JsonNode newData = replaceNode(existingData, pathParts, data);
                vo.setJsonData(newData);
            } else if (operation == MapOperation.APPEND) {
                dataToReturn = mergeNode(existingData, pathParts, data);
            }
            this.service.update(xid, vo);
        }catch(NotFoundException e) {
            // can't append/merge to a non-existing object or replace data at a path of a non existing object
            if (operation == MapOperation.APPEND || pathParts.length > 0) {
                throw e;
            }
            //Going to create a new one
            vo = new JsonDataVO();
            vo.setXid(xid);
            vo.setName(name);
            vo.setPublicData(publicData);
            vo.setReadRoles(permissionService.explodeLegacyPermissionGroupsToRoles(readPermissions));
            vo.setEditRoles(permissionService.explodeLegacyPermissionGroupsToRoles(editPermissions));
            vo.setJsonData(data);
            this.service.insert(vo);
        }
        // can't modify the vo data here as it will be sent out over websocket asynchronously
        JsonDataVO copied = (JsonDataVO) vo.copy();
        // return only the data that was saved, i.e. the data that we supplied a path to
        copied.setJsonData(dataToReturn);
        URI location = builder.path("/json-data/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(this.map.apply(copied, user), headers, successStatus);
    }

    int toArrayIndex(String fieldName) {
        try {
            return Integer.valueOf(fieldName);
        } catch (NumberFormatException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidArrayField", fieldName));
        }
    }

    JsonNode getNode(final JsonNode existingData, final String[] dataPath) {
        JsonNode node = existingData;
        for (int i = 0; i < dataPath.length; i++) {
            String fieldName = dataPath[i];

            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                node = objectNode.get(fieldName);
            } else if (node.isArray()) {
                ArrayNode arrayNode = (ArrayNode) node;
                int index = toArrayIndex(fieldName);
                node = arrayNode.get(index);
            } else {
                throw new BadRequestException(new TranslatableMessage("rest.error.cantGetFieldOfNodeType", node.getNodeType()));
            }

            if (node == null) {
                throw new NotFoundRestException();
            }
        }
        return node;
    }

    boolean deleteNode(final JsonNode existingData, final String[] dataPath) {
        if (dataPath.length == 0) throw new IllegalArgumentException();

        String[] parentPath = Arrays.copyOfRange(dataPath, 0, dataPath.length - 1);
        String fieldName = dataPath[dataPath.length - 1];

        JsonNode parent = getNode(existingData, parentPath);
        JsonNode deletedValue = null;

        if (parent.isObject()) {
            ObjectNode parentObject = (ObjectNode) parent;
            deletedValue = parentObject.remove(fieldName);
        } else if (parent.isArray()) {
            ArrayNode parentArray = (ArrayNode) parent;
            int index = toArrayIndex(fieldName);
            deletedValue = parentArray.remove(index);
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantDeleteFieldOfNodeType", parent.getNodeType()));
        }

        return deletedValue != null;
    }

    JsonNode replaceNode(final JsonNode existingData, final String[] dataPath, final JsonNode newData) {
        if (dataPath.length == 0) {
            return newData;
        }

        String[] parentPath = Arrays.copyOfRange(dataPath, 0, dataPath.length - 1);
        String fieldName = dataPath[dataPath.length - 1];

        JsonNode parent = getNode(existingData, parentPath);

        if (parent.isObject()) {
            ObjectNode parentObject = (ObjectNode) parent;
            parentObject.set(fieldName, newData);
        } else if (parent.isArray()) {
            ArrayNode parentArray = (ArrayNode) parent;
            int index = toArrayIndex(fieldName);
            parentArray.set(index, newData);
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantSetFieldOfNodeType", parent.getNodeType()));
        }

        return existingData;
    }

    JsonNode mergeNode(final JsonNode existingData, final String[] dataPath, final JsonNode newData) {
        JsonNode destination = getNode(existingData, dataPath);
        JsonNode mergedNode;

        if (destination.isObject()) {
            // object merge
            if (!newData.isObject()) {
                throw new BadRequestException(new TranslatableMessage("rest.error.cantMergeNodeTypeIntoObject", newData.getNodeType()));
            }

            ObjectNode destinationObject = (ObjectNode) destination;

            Iterator<Entry<String, JsonNode>> it = newData.fields();
            while (it.hasNext()) {
                Entry<String, JsonNode> entry = it.next();
                destinationObject.set(entry.getKey(), entry.getValue());
            }

            mergedNode = destinationObject;
        } else if (destination.isArray()) {
            // append operation
            ArrayNode destinationArray = (ArrayNode) destination;
            destinationArray.add(newData);

            mergedNode = destinationArray;
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantMergeIntoX", destination.getNodeType()));
        }

        return mergedNode;
    }

}
