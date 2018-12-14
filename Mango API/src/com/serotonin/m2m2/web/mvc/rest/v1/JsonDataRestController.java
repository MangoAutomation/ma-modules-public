/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.db.dao.JsonDataDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.definitions.permissions.JsonDataCreatePermissionDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.json.JsonDataVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.jsondata.JsonDataModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
public class JsonDataRestController extends MangoVoRestController<JsonDataVO, JsonDataModel, JsonDataDao>{

    private static Log LOG = LogFactory.getLog(JsonDataRestController.class);
    enum MapOperation {
        APPEND, REPLACE, DELETE
    }

    /**
     * @param dao
     */
    public JsonDataRestController() {
        super(JsonDataDao.getInstance());
    }

    @ApiOperation(
            value = "List all available xids",
            notes = "Shows any xids that you have read permissions for",
            response = List.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<String>> list(
            HttpServletRequest request
            ){

        RestProcessResult<List<String>> result = new RestProcessResult<List<String>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            List<JsonDataVO> all = this.dao.getAll();
            List<String> available = new ArrayList<String>();
            for(JsonDataVO vo : all){
                //Check existing permissions
                if(Permissions.hasPermission(user, vo.getReadPermission()) || Permissions.hasPermission(user, vo.getEditPermission())){
                    available.add(vo.getXid());
                }
            }
            return result.createResponseEntity(available);
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get Public JSON Data",
            notes = "Returns only the data"
            )
    @RequestMapping(method = RequestMethod.GET, value="/public/{xid}")
    public ResponseEntity<JsonDataModel> getPublicData(
            HttpServletRequest request,
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid
            ){
        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.OK);
        JsonDataVO vo = JsonDataDao.getInstance().getByXid(xid);

        if(vo == null){
            result.addRestMessage(getDoesNotExistMessage());
            return result.createResponseEntity();
        }else{
            //Check existing permissions
            if(!vo.isPublicData()){
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
            else{
                return result.createResponseEntity(new JsonDataModel(vo));
            }
        }
    }

    @ApiOperation(
            value = "Get JSON Data",
            notes = "Returns only the data"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<JsonDataModel> getData(
            HttpServletRequest request,

            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid
            ) throws UnsupportedEncodingException{
        return getDataWithPath(request, xid, null);
    }

    @ApiOperation(
            value = "Get JSON Data using a path",
            notes = "To get a sub component of the data use a path of member.submember"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/{path:.*}")
    public ResponseEntity<JsonDataModel> getDataWithPath(
            HttpServletRequest request,

            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path
            ) throws UnsupportedEncodingException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){

            JsonDataVO vo = JsonDataDao.getInstance().getByXid(xid);
            if(vo == null){
                result.addRestMessage(getDoesNotExistMessage());
            } else {
                //Check existing permissions
                if(!(Permissions.hasPermission(user, vo.getReadPermission()) || Permissions.hasPermission(user, vo.getEditPermission()))) {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }

                String[] pathParts = splitAndDecodePath(path);
                if (pathParts.length == 0) {
                    return result.createResponseEntity(new JsonDataModel(vo));
                } else {
                    JsonNode data = (JsonNode) vo.getJsonData();
                    JsonNode subNode = getNode(data, pathParts);

                    vo.setJsonData(subNode);
                    return result.createResponseEntity(new JsonDataModel(vo));
                }
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Append JSON Data to existing",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Created", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 409, message = "Data Already Exists")
    })
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

            @ApiParam( value = "Data to save", required = true )
            @RequestBody(required=false)
            JsonNode data,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.OK);
        return modifyJsonData(MapOperation.APPEND, result, xid, new String[] {}, readPermission, editPermission, name, publicData, data, builder, request);
    }

    @ApiOperation(
            value = "Append JSON Data to existing",
            notes = "{path} is the path to data with dots data.member.submember",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Created", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 403, message = "Data Doesn't Exists")
    })
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

            @ApiParam( value = "Data to save", required = true )
            @RequestBody(required=true)
            JsonNode data,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException, UnsupportedEncodingException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.OK);
        String[] pathParts = splitAndDecodePath(path);
        return modifyJsonData(MapOperation.APPEND, result, xid, pathParts, readPermission, editPermission, name, publicData, data, builder, request);
    }


    @ApiOperation(
            value = "Create/replace JSON Data",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Created", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 409, message = "Data Already Exists")
    })
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

            @ApiParam( value = "Data to save", required = true )
            @RequestBody(required=true)
            JsonNode data,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.CREATED);
        return modifyJsonData(MapOperation.REPLACE, result, xid, new String[] {}, readPermission, editPermission, name, publicData, data, builder, request);
    }

    @ApiOperation(
            value = "Replace JSON Data",
            notes = "{path} is the path to data with dots data.member.submember",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Created", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 409, message = "Data Already Exists")
    })
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

            @ApiParam( value = "Data to save", required = true )
            @RequestBody(required=true)
            JsonNode data,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException, UnsupportedEncodingException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.CREATED);
        String[] pathParts = splitAndDecodePath(path);
        return modifyJsonData(MapOperation.REPLACE, result, xid, pathParts, readPermission, editPermission, name, publicData, data, builder, request);
    }

    @ApiOperation(
            value = "Fully Delete JSON Data",
            notes = "",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Deleted", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 403, message = "Data Doesn't Exists")
    })
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<JsonDataModel> deleteJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {
        return deletePartialJsonData(xid, null, builder, request);
    }

    @ApiOperation(
            value = "Partially Delete JSON Data",
            notes = "{path} is the path to data with dots data.member.submember",
            response=JsonDataModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data Deleted", response=JsonDataModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 403, message = "Data Doesn't Exists")
    })
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}/{path:.*}")
    public ResponseEntity<JsonDataModel> deletePartialJsonData(
            @ApiParam(value = "XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @ApiParam(value = "Data path using dots as separator", required = true, allowMultiple = false)
            @PathVariable String path,

            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<JsonDataModel> result = new RestProcessResult<JsonDataModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){

            JsonDataVO vo = this.dao.getByXid(xid);
            if(vo != null){
                //Going to delete data

                //Check existing permissions
                if(!Permissions.hasPermission(user, vo.getEditPermission())){
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
                JsonDataModel model = new JsonDataModel(vo);

                String[] pathParts;
                if (path == null || (pathParts = path.split("\\.")).length == 0) {
                    //Delete the whole thing
                    this.dao.delete(vo.getId());
                } else {
                    //Delete something from the map
                    JsonNode existingData = (JsonNode) vo.getJsonData();
                    boolean deleted = deleteNode(existingData, pathParts);
                    if (!deleted) {
                        result.addRestMessage(getDoesNotExistMessage());
                        return result.createResponseEntity();
                    }

                    if (!model.validate()) {
                        result.addRestMessage(this.getValidationFailedError());
                        return result.createResponseEntity(model);
                    }
                    try {
                        String initiatorId = request.getHeader("initiatorId");
                        this.dao.save(vo, initiatorId);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(),e);
                        result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                    }
                }
                return result.createResponseEntity(model);
            }else{
                result.addRestMessage(getDoesNotExistMessage());
            }
        }

        return result.createResponseEntity();
    }

    private String[] splitAndDecodePath(String path) throws UnsupportedEncodingException {
        String[] pathParts = path != null ? path.split("\\.") : new String[] {};
        for (int i = 0; i < pathParts.length; i++) {
            pathParts[i] = URLDecoder.decode(pathParts[i], "UTF-8");
        }
        return pathParts;
    }

    /**
     * Helper to modify data
     * @param result
     * @param xid
     * @param path
     * @param readPermissions
     * @param editPermissions
     * @param name
     * @param data
     * @param builder
     * @param request
     * @return
     */
    private ResponseEntity<JsonDataModel> modifyJsonData(MapOperation operation, RestProcessResult<JsonDataModel> result,
            String xid, String[] pathParts, Set<String> readPermissions, Set<String> editPermissions, String name, boolean publicData,
            JsonNode data, UriComponentsBuilder builder, HttpServletRequest request) {

        // check we are using this method only for replace and append
        if (operation != MapOperation.REPLACE && operation != MapOperation.APPEND) throw new IllegalArgumentException();

        User user = this.checkUser(request, result);
        if (!result.isOk()) {
            return result.createResponseEntity();
        }

        JsonNode dataToReturn = data;

        JsonDataVO vo = this.dao.getByXid(xid);
        if (vo != null) {
            //Going to replace/append/merge data

            //Check existing permissions
            if(!Permissions.hasPermission(user, vo.getEditPermission())){
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            //Replace the data
            vo.setName(name);
            vo.setPublicData(publicData);
            vo.setReadPermission(Permissions.implodePermissionGroups(readPermissions));
            vo.setEditPermission(Permissions.implodePermissionGroups(editPermissions));

            JsonNode existingData = (JsonNode) vo.getJsonData();

            if (operation == MapOperation.REPLACE) {
                JsonNode newData = replaceNode(existingData, pathParts, data);
                vo.setJsonData(newData);
            } else if (operation == MapOperation.APPEND) {
                dataToReturn = mergeNode(existingData, pathParts, data);
            }
        } else {
            if(!Permissions.hasPermission(user, SystemSettingsDao.instance.getValue(JsonDataCreatePermissionDefinition.TYPE_NAME))) {
                throw new PermissionException(new TranslatableMessage("jsonData.createPermissionDenied", user.getUsername()), user);
            }

            // can't append/merge to a non-existing object or replace data at a path of a non existing object
            if (operation == MapOperation.APPEND || pathParts.length > 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            //Going to create a new one
            vo = new JsonDataVO();
            vo.setXid(xid);
            vo.setName(name);
            vo.setPublicData(publicData);
            vo.setReadPermission(Permissions.implodePermissionGroups(readPermissions));
            vo.setEditPermission(Permissions.implodePermissionGroups(editPermissions));
            vo.setJsonData(data);
        }

        JsonDataModel model = new JsonDataModel(vo);
        if(!model.validate()){
            result.addRestMessage(this.getValidationFailedError());
            // return only the data that was saved, i.e. the data that we supplied a path to
            vo.setJsonData(data);
            return result.createResponseEntity(model);
        }

        //Ensure we have the correct permissions
        //First we must check to ensure that the User actually has editPermission before they can save it otherwise
        // they won't be able to modify it.
        Set<String> userPermissions = user.getPermissionsSet();

        if(!user.isAdmin() && Collections.disjoint(userPermissions, editPermissions)){
            //Return validation error
            result.addRestMessage(this.getValidationFailedError());
            model.addValidationMessage("jsonData.editPermissionRequired", RestMessageLevel.ERROR, "editPermission");
            vo.setJsonData(data);
            return result.createResponseEntity(model);
        }

        try {
            String initiatorId = request.getHeader("initiatorId");
            this.dao.save(vo, initiatorId);

            // can't modify the vo data here as it will be sent out over websocket asynchronously
            JsonDataVO copied = vo.copy();

            // return only the data that was saved, i.e. the data that we supplied a path to
            copied.setJsonData(dataToReturn);
            model = new JsonDataModel(copied);

            URI location = builder.path("/json-data/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
            result.addRestMessage(this.getResourceCreatedMessage(location));
            return result.createResponseEntity(model);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }

        return result.createResponseEntity();
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

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(java.lang.Object)
     */
    @Override
    public JsonDataModel createModel(JsonDataVO vo) {
        return new JsonDataModel(vo);
    }

}
