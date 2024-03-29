/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.user.UserModel;
import com.infiniteautomation.mango.spring.components.pageresolver.LoginUriInfo;
import com.infiniteautomation.mango.spring.components.pageresolver.PageResolver;
import com.serotonin.m2m2.db.dao.InstalledModulesDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.security.oauth2.OAuth2Information;
import com.serotonin.m2m2.web.mvc.spring.security.permissions.AnonymousAccess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Login/Switch User Actions
 *
 * Ensure that the URLs in MangoSecurityConfiguration are changed if you change the @RequestMapping value
 *
 * @author Terry Packer
 */
@Api(value = "Login")
@RestController
@RequestMapping("/login")
public class LoginRestController {

    public static final String LOGIN_DEFAULT_URI_HEADER = "X-Mango-Default-URI";
    public static final String LOGIN_DEFAULT_URI_REQUIRED_HEADER = "X-Mango-Default-URI-Required";
    public static final String LOGIN_LAST_UPGRADE_HEADER = "X-Mango-Last-Upgrade";
    private final PageResolver pageResolver;
    private final OAuth2Information oAuth2Information;
    private final InstalledModulesDao installedModulesDao;

    @Autowired
    public LoginRestController(PageResolver pageResolver, OAuth2Information oAuth2Information, InstalledModulesDao installedModulesDao) {
        this.pageResolver = pageResolver;
        this.oAuth2Information = oAuth2Information;
        this.installedModulesDao = installedModulesDao;
    }

    /**
     * <p>The actual authentication for the login occurs in the core, by the time this
     * end point is actually reached the user is either already authenticated or not.
     * The Spring Security authentication success handler forwards the request here.</p>
     *
     * <p>Authentication exceptions are re-thrown and mapped to rest bodies in {@link com.infiniteautomation.mango.rest.latest.exception.RestExceptionHandler MangoSpringExceptionHandler}</p>
     *
     * <p>Ensure that the URLs in MangoSecurityConfiguration are changed if you change the @RequestMapping value</p>
     */
    @ApiOperation(value = "Login", notes = "Used to login using POST and JSON credentials")
    @RequestMapping(method = RequestMethod.POST)
    @AnonymousAccess
    public ResponseEntity<UserModel> loginPost(
            @AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) {

        AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (ex != null) {
            throw ex;
        }

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LoginUriInfo info = pageResolver.getDefaultUriInfo(request, response, user);
            response.setHeader(LOGIN_DEFAULT_URI_HEADER, info.getUri());
            response.setHeader(LOGIN_LAST_UPGRADE_HEADER, Long.toString(installedModulesDao.lastUpgradeTime().toEpochMilli() / 1000));
            if(info.isRequired())
                response.setHeader(LOGIN_DEFAULT_URI_REQUIRED_HEADER, Boolean.TRUE.toString());
            return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
        }
    }

    /**
     * The actual authentication for the switch user occurs in the core by the SwitchUserFilter,
     *  by the time this end point is actually reached the user is either already authenticated or not
     * The Spring Security authentication success handler forwards the request here
     *
     * Ensure that the URLs in MangoSecurityConfiguration are changed if you change the @RequestMapping value
     *
     */
    @ApiOperation(value = "Switch User", notes = "Used to switch User using GET")
    @RequestMapping(method = RequestMethod.POST,  value="/su")
    public ResponseEntity<UserModel> switchUser(
            @ApiParam(value = "Username to switch to", required = true, allowMultiple = false)
            @RequestParam(required=true) String username,
            @AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (ex != null) {
            // TODO
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
            return null;
        }

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LoginUriInfo info = pageResolver.getDefaultUriInfo(request, response, user);
            response.setHeader(LOGIN_DEFAULT_URI_HEADER, info.getUri());
            response.setHeader(LOGIN_LAST_UPGRADE_HEADER, Long.toString(installedModulesDao.lastUpgradeTime().toEpochMilli() / 1000));
            if(info.isRequired())
                response.setHeader(LOGIN_DEFAULT_URI_REQUIRED_HEADER, Boolean.TRUE.toString());
            return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
        }
    }

    /**
     * The actual authentication for the exit user occurs in the core by the SwitchUserFilter,
     *  by the time this end point is actually reached the user is either already authenticated or not
     * The Spring Security authentication success handler forwards the request here
     *
     * Ensure that the URLs in MangoSecurityConfiguration are changed if you change the @RequestMapping value
     *
     */
    @ApiOperation(value = "Exit Switch User", notes = "Used to switch User using POST")
    @RequestMapping(method = RequestMethod.POST,  value="/exit-su")
    public ResponseEntity<UserModel> exitSwitchUser(
            @AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (ex != null) {
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
            return null;
        }

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LoginUriInfo info = pageResolver.getDefaultUriInfo(request, response, user);
            response.setHeader(LOGIN_DEFAULT_URI_HEADER, info.getUri());
            response.setHeader(LOGIN_LAST_UPGRADE_HEADER, Long.toString(installedModulesDao.lastUpgradeTime().toEpochMilli() / 1000));
            if(info.isRequired())
                response.setHeader(LOGIN_DEFAULT_URI_REQUIRED_HEADER, Boolean.TRUE.toString());
            return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Get list of enabled OAuth2 / OpenID connect clients")
    @RequestMapping(method = RequestMethod.GET,  value="/oauth2-clients")
    @AnonymousAccess
    public List<OAuth2Information.OAuth2ClientInfo> oauth2Clients() {
        return oAuth2Information.enabledClients();
    }
}
