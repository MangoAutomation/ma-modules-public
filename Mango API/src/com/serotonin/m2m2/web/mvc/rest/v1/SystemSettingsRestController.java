/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.systemSettings.SystemSettingTypeEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Access to System Settings
 *
 * @author Terry Packer
 */
@Api(value="System Settings", description="Configure/Read System Settings")
@RestController
@RequestMapping("/system-settings")
public class SystemSettingsRestController extends MangoRestController{

    @Autowired
    @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
    ObjectMapper mapper;

    private SystemSettingsDao dao = SystemSettingsDao.instance;

    @ApiOperation(
            value = "Get System Setting By key",
            notes = "Admin Permission Required, if no type supplied assume to be string"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{key}")
    public ResponseEntity<Object> get(
            @ApiParam(value = "Valid System Setting ID", required = true, allowMultiple = false)
            @PathVariable String key,
            @ApiParam(value = "Return Type", required = false, defaultValue="STRING", allowMultiple = false)
            @RequestParam(required=false, defaultValue="STRING") SystemSettingTypeEnum type,
            HttpServletRequest request) throws IOException {
        RestProcessResult<Object> result = new RestProcessResult<Object>(HttpStatus.OK);

        this.checkUser(request, result);
        if(result.isOk()){
            Object value = null;
            switch(type){
                case BOOLEAN:
                    value = SystemSettingsDao.instance.getBooleanValue(key);
                    break;
                case INTEGER:
                    value = SystemSettingsDao.instance.getIntValue(key);
                    break;
                case JSON:
                    String json = SystemSettingsDao.instance.getValue(key);
                    if(json != null) {
                        value = this.mapper.reader().readTree(json);
                    }
                    break;
                case STRING:
                default:
                    //First get the value as a String
                    value = SystemSettingsDao.instance.getValue(key);
                    try{
                        //Can it potentially be converted to an export code?
                        Integer i = Integer.parseInt((String) value);
                        value = SystemSettingsDao.instance.convertToCodeFromValue(key, i);
                        //Was it able to be converted?
                        if(value == null)
                            value = i.toString();
                    }catch(NumberFormatException e){ }

                    break;
            }
            if (value == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }else{
                return result.createResponseEntity(value);
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get All System Settings",
            notes = "Admin Permission Required, All settings returned as string types"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getAll(HttpServletRequest request) {
        RestProcessResult<Map<String, Object>> result = new RestProcessResult<Map<String, Object>>(HttpStatus.OK);

        this.checkUser(request, result);
        if(result.isOk()){
            Map<String,Object> settings = dao.getAllSystemSettingsAsCodes();
            return result.createResponseEntity(settings);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Update an existing System Setting",
            notes = "If no type is provided, String is assumed"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/{key}")
    public ResponseEntity<Object> update(
            @PathVariable String key,
            @ApiParam(value = "Updated model", required = true)
            @RequestBody(required=true) Object model,
            @ApiParam(value = "Setting Type", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="STRING") SystemSettingTypeEnum type,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {

        RestProcessResult<Object> result = new RestProcessResult<Object>(HttpStatus.OK);

        this.checkUser(request, result);
        if (result.isOk()) {
            Map<String, Object> settings = new HashMap<String, Object>();
            settings.put(key, model);
            ProcessResult response = new ProcessResult();
            this.dao.validate(settings, response, user);
            if (response.getHasMessages()) {
                throw new ValidationException(response);
            }
            switch (type) {
                case BOOLEAN:
                    dao.setBooleanValue(key, (Boolean) model);
                    break;
                case INTEGER:
                    dao.setIntValue(key, (Integer) model);
                    break;
                case JSON:
                    try {
                        dao.setJsonObjectValue(key, model);
                    } catch (Exception e) {
                        result.addRestMessage(
                                this.getInternalServerErrorMessage(e.getMessage()));
                        return result.createResponseEntity();
                    }
                    break;
                case STRING:
                default:
                    // Potentially convert value from its code
                    Integer code = this.dao.convertToValueFromCode(key, (String) model);
                    if (code != null)
                        dao.setIntValue(key, code);
                    else
                        dao.setValue(key, (String) model);
                    break;
            }

            // J.W. WTF is this for?
            // Put a link to the updated data in the header
            URI location =
                    builder.path("/v1/system-settings/{key}").buildAndExpand(key).toUri();
            result.addRestMessage(getResourceUpdatedMessage(location));

            return result.createResponseEntity(model);
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Update Many System Settings",
            notes = "Admin Privs Required"
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> updateMany(
            @ApiParam(value = "Updated settings", required = true)
            @RequestBody(required=true) Map<String,Object> settings,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {

        RestProcessResult<Map<String,Object>> result = new RestProcessResult<Map<String,Object>>(HttpStatus.OK);

        this.checkUser(request, result);
        if (result.isOk()) {
            ProcessResult response = new ProcessResult();
            // Convert incoming ExportCodes to int values
            settings = this.dao.convertCodesToValues(settings);
            this.dao.validate(settings, response, user);
            if (response.getHasMessages()) {
                throw new ValidationException(response);
            }
            this.dao.updateSettings(settings);

            // J.W. WTF is this for?
            // Put a link to the updated data in the header
            URI location = builder.path("/v1/system-settings").buildAndExpand().toUri();
            result.addRestMessage(getResourceUpdatedMessage(location));

            return result.createResponseEntity(settings);
        }

        return result.createResponseEntity();
    }

    /**
     * Override to add check for Data Source Permissions since that is required
     */
    @Override
    protected User checkUser(HttpServletRequest request, @SuppressWarnings("rawtypes") RestProcessResult result) {
        User user = super.checkUser(request, result);
        if(user != null){
            if(!user.isAdmin()){
                result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("common.default", "No Admin Permission"));
            }
        }
        return user;
    }

}
