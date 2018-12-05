/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.model.event.RaiseEventModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.LicenseViolatedException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author Terry Packer
 */
@Api(value="Example Controller", description="Test for new controller type")
@RestController
@RequestMapping("/example")
public class ExampleV2RestController extends AbstractMangoRestV2Controller{

    private static final Log LOG = LogFactory.getLog(ExampleV2RestController.class);

    @Autowired
    MangoSessionRegistry sessionRegistry;

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Example User Credentials test", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/admin-get/{resourceId}"} )
    public ResponseEntity<Object> exampleGet(@AuthenticationPrincipal User user,
            @ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId) {
        return new ResponseEntity<Object>(HttpStatus.OK);
    }


    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example User Credentials test", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/user-get/{resourceId}"} )
    public ResponseEntity<Object> userGet(@AuthenticationPrincipal User user,
            @ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId) {
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example Permission Exception Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/permissions-exception"} )
    public ResponseEntity<Object> alwaysFails(@AuthenticationPrincipal User user) {
        throw new PermissionException(new TranslatableMessage("common.default", "I always fail."), user);
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example Access Denied Exception Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/access-denied-exception"} )
    public ResponseEntity<Object> accessDenied(@AuthenticationPrincipal User user) {
        throw new AccessDeniedException("I don't have access.");
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example Generic Rest Exception Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/generic-exception"} )
    public ResponseEntity<Object> genericFailure(@AuthenticationPrincipal User user) {
        throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example Runtime Exception Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/runtime-exception"} )
    public ResponseEntity<Object> runtimeFailure(@AuthenticationPrincipal User user) {
        throw new RuntimeException("I'm a runtime Exception");
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example IOException Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/io-exception"} )
    public ResponseEntity<Object> ioFailure(@AuthenticationPrincipal User user) throws IOException{
        throw new IOException("I'm an Exception");
    }

    @PreAuthorize("hasAllPermissions('user')")
    @ApiOperation(value = "Example LicenseViolationException Response", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/license-violation"} )
    public ResponseEntity<Object> licenseViolation(@AuthenticationPrincipal User user) throws IOException{
        throw new LicenseViolatedException(new TranslatableMessage("common.default", "Test Violiation"));
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Expire the session of the current user", notes = "must be admin")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/expire-session"} )
    public ResponseEntity<Object> expireSessions(@AuthenticationPrincipal User user){
        List<SessionInformation> infos = sessionRegistry.getAllSessions(user, false);
        for(SessionInformation info : infos)
            info.expireNow();
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Example Path matching", notes = "")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.GET}, value = {"/{resourceId}/**"} )
    public ResponseEntity<String> matchPath(@AuthenticationPrincipal User user,
            @ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId,
            HttpServletRequest request) {

        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        AntPathMatcher apm = new AntPathMatcher();
        String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

        return new ResponseEntity<String>(finalPath, HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Raise an event", notes = "must be admin")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
    })
    @RequestMapping( method = {RequestMethod.POST}, value = {"/raise-event"} )
    public ResponseEntity<Object> raiseExampleEvent(@AuthenticationPrincipal User user,
            @RequestBody(required=true) RaiseEventModel model){
        if(model == null)
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR);
        Common.eventManager.raiseEvent(model.getEvent().toEventType(), Common.timer.currentTimeMillis(), true, model.getLevel(), new TranslatableMessage("common.default", model.getMessage()), model.getContext());
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Identity function", notes = "Returns whatever is sent in the request body. Useful for testing message converters. Must be admin")
    @RequestMapping(method = RequestMethod.POST, value = {"/identity"})
    public Object identityFunction(
            @RequestBody Object node) {

        return node;
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Log ERROR Level Message", notes = "Must be admin")
    @RequestMapping(method = RequestMethod.POST, value = {"/log-error-message"})
    public void logErorMessage(
            @RequestBody String message) {
        LOG.error(message);
    }
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Log WARN Level Message", notes = "Must be admin")
    @RequestMapping(method = RequestMethod.POST, value = {"/log-warn-message"})
    public void logWarnMessage(
            @RequestBody String message) {
        LOG.warn(message);
    }
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Log INFO Level Message", notes = "Must be admin")
    @RequestMapping(method = RequestMethod.POST, value = {"/log-info-message"})
    public void logInfoMessage(
            @RequestBody String message) {
        LOG.info(message);
    }
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Log DEBUG Level Message", notes = "Must be admin")
    @RequestMapping(method = RequestMethod.POST, value = {"/log-debug-message"})
    public void logDebugMessage(
            @RequestBody String message) {
        LOG.debug(message);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get upload limit")
    @RequestMapping(method = RequestMethod.GET, value = {"/upload-limit"})
    public long getUploadLimit() {
        return Common.envProps.getLong("web.fileUpload.maxSize", 50000000);
    }

    @PreAuthorize("hasRole('ROLE_TEST SPACE')")
    @RolesAllowed("ROLE_TEST SPACE")
    @Secured("ROLE_TEST SPACE")
    @ApiOperation(value = "User must have a permission named 'TEST SPACE'")
    @RequestMapping(method = RequestMethod.GET, value = {"/role-with-space"})
    public String canGetWithSpaceInPermission() {
        return "OK";
    }
}
