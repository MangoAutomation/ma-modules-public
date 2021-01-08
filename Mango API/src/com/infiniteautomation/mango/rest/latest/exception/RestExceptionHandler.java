/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.io.EofException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.infiniteautomation.mango.db.query.RQLToCondition.RQLVisitException;
import com.infiniteautomation.mango.io.messaging.MessageSendException;
import com.infiniteautomation.mango.io.messaging.email.EmailFailedException;
import com.infiniteautomation.mango.rest.latest.advice.MangoRequestBodyAdvice;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.views.AdminView;
import com.infiniteautomation.mango.spring.components.EmailAddressVerificationService.EmailAddressInUseException;
import com.infiniteautomation.mango.spring.script.MangoScriptException;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.FeatureDisabledException;
import com.infiniteautomation.mango.util.exception.InvalidRQLException;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.TranslatableExceptionI;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.infiniteautomation.mango.util.exception.TranslatableRuntimeException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.module.DefaultPagesDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.mvc.spring.security.authentication.MangoPasswordAuthenticationProvider.AuthenticationRateException;

/**
 * @author Terry Packer
 */
@ControllerAdvice(basePackages = {"com.infiniteautomation.mango.rest.latest"})
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final Log log = LogFactory.getLog(RestExceptionHandler.class);

    final RequestMatcher browserHtmlRequestMatcher;
    final RestModelMapper mapper;
    final PermissionService service;
    final Environment env;
    final AuthenticationTrustResolver authenticationTrustResolver;

    @Autowired
    public RestExceptionHandler(
            @Qualifier("browserHtmlRequestMatcher")
                    RequestMatcher browserHtmlRequestMatcher,
            RestModelMapper mapper,
            PermissionService service, Environment env, AuthenticationTrustResolver authenticationTrustResolver) {
        this.browserHtmlRequestMatcher = browserHtmlRequestMatcher;
        this.mapper = mapper;
        this.service = service;
        this.env = env;
        this.authenticationTrustResolver = authenticationTrustResolver;
    }

    /**
     * Handle End of file exceptions specifically those from the client's connection being closed
     */
    @ExceptionHandler(EofException.class)
    public Object exceptionHandler(HttpServletRequest request, HttpServletResponse response, IOException ex, WebRequest req) {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        //There is nothing we can send back
        return null;
    }

    //Anything that extends our Base Exception
    @ExceptionHandler({AbstractRestException.class})
    public ResponseEntity<Object> handleMangoError(HttpServletRequest request, HttpServletResponse response, AbstractRestException ex, WebRequest req) {
        return handleExceptionInternal(ex, ex, new HttpHeaders(), ex.getStatus(), req);
    }

    @ExceptionHandler({
            org.springframework.security.access.AccessDeniedException.class,
            PermissionException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<Object> handleAccessDenied(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        Object model;
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            model = new AccessDeniedException(ex);
        } else if (ex instanceof PermissionException) {
            PermissionException permissionException = (PermissionException) ex;
            model = new AccessDeniedException(permissionException.getTranslatableMessage(), ex);
        } else {
            model = ex;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpStatus status = authenticationTrustResolver.isAnonymous(auth) ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        return handleExceptionInternal(ex, model, new HttpHeaders(), status, req);
    }

    @ExceptionHandler({
            ValidationException.class
    })
    public ResponseEntity<Object> handleValidationException(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        ValidationException validationException = (ValidationException) ex;

        ProcessResult result = validationException.getValidationResult();

        //Do any potential mapping of VO property names to model property names
        Class<?> modelClass = (Class<?>) req.getAttribute(MangoRequestBodyAdvice.MODEL_CLASS, RequestAttributes.SCOPE_REQUEST);
        if (validationException.getValidatedClass() != null && modelClass != null) {
            result = mapper.mapValidationErrors(modelClass, validationException.getValidatedClass(), result);
        }

        return handleExceptionInternal(ex, new ValidationFailedRestException(result), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, req);
    }

    @ExceptionHandler({
            MessageSendException.class
    })
    public ResponseEntity<Object> handleEmailFailedException(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        if (ex instanceof EmailFailedException) {
            EmailFailedException e = (EmailFailedException) ex;
            return handleExceptionInternal(ex, new SendEmailFailedRestException(e, e.getSession()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
        } else {
            return handleExceptionInternal(ex, new SendMessageFailedRestException((MessageSendException) ex), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
        }
    }

    @ExceptionHandler({
            NotFoundException.class,
            ResourceNotFoundException.class
    })
    public ResponseEntity<Object> handleNotFoundException(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        return handleExceptionInternal(ex, new NotFoundRestException(ex), new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler({
            InvalidRQLException.class
    })
    public ResponseEntity<Object> handleInvalidRQLException(HttpServletRequest request, HttpServletResponse response, InvalidRQLException ex, WebRequest req) {
        return handleExceptionInternal(ex, new InvalidRQLRestException(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler({
            RQLVisitException.class
    })
    public ResponseEntity<Object> handleRQLVisitException(HttpServletRequest request, HttpServletResponse response, RQLVisitException ex, WebRequest req) {
        return handleExceptionInternal(ex, new RQLVisitRestException(ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler({
            TranslatableIllegalStateException.class
    })
    public ResponseEntity<Object> handleTranslatableIllegalStateException(HttpServletRequest request, HttpServletResponse response, TranslatableIllegalStateException ex, WebRequest req) {
        return handleExceptionInternal(ex, new IllegalStateRestException(ex.getTranslatableMessage(), ex), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler({
            EmailAddressInUseException.class,
            FeatureDisabledException.class
    })
    public ResponseEntity<Object> handleConflictExceptions(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        ConfictRestException body;
        if (ex instanceof FeatureDisabledException) {
            body = new ConfictRestException(MangoRestErrorCode.FEATURE_DISABLED, ((FeatureDisabledException) ex).getTranslatableMessage(), ex);
        } else if (ex instanceof TranslatableExceptionI) {
            body = new ConfictRestException(((TranslatableExceptionI) ex).getTranslatableMessage(), ex);
        } else {
            body = new ConfictRestException(ex);
        }
        return handleExceptionInternal(ex, body, new HttpHeaders(), body.getStatus(), req);
    }

    @ExceptionHandler({
            TranslatableRuntimeException.class
    })
    public ResponseEntity<Object> handleTranslatableRuntimeException(HttpServletRequest request, HttpServletResponse response, TranslatableRuntimeException ex, WebRequest req) {

        GenericRestException body = new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getTranslatableMessage(), ex.getCause());
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @ExceptionHandler({
            TranslatableException.class
    })
    public ResponseEntity<Object> handleTranslatableException(HttpServletRequest request, HttpServletResponse response, TranslatableException ex, WebRequest req) {

        GenericRestException body = new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getTranslatableMessage(), ex.getCause());
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @ExceptionHandler(AuthenticationRateException.class)
    public ResponseEntity<Object> handleIpAddressAuthenticationRateException(HttpServletRequest request, HttpServletResponse response, AuthenticationRateException ex, WebRequest req) {
        RateLimitedRestException body = RateLimitedRestException.restExceptionFor(ex);
        return handleExceptionInternal(ex, body, new HttpHeaders(), body.getStatus(), req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleUsernameAuthenticationRateException(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex, WebRequest req) {
        AuthenticationFailedRestException body = AuthenticationFailedRestException.restExceptionFor(ex);
        return handleExceptionInternal(ex, body, new HttpHeaders(), body.getStatus(), req);
    }

    @ExceptionHandler(MangoScriptException.class)
    public ResponseEntity<Object> handleMangoScriptException(HttpServletRequest request, HttpServletResponse response, MangoScriptException ex, WebRequest req) {
        ScriptRestException body = new ScriptRestException(ex);
        return handleExceptionInternal(ex, body, new HttpHeaders(), body.getStatus(), req);
    }

    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<Object> handleAllOtherErrors(HttpServletRequest request, HttpServletResponse response, Exception ex, WebRequest req) {
        return handleExceptionInternal(ex, new ServerErrorException(ex), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Exception handled, returning status %s for request %s", status, request.getDescription(true)), ex);
        }

        PermissionHolder user;
        try {
            user = Common.getUser();
        } catch (PermissionException e) {
            user = null;
        }

        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        HttpServletResponse servletResponse = ((ServletWebRequest) request).getResponse();

        // redirect user to not found or error page
        if (!env.getProperty("rest.disableErrorRedirects", Boolean.class, false) && this.browserHtmlRequestMatcher.matches(servletRequest)) {
            String uri;
            if (status == HttpStatus.NOT_FOUND) {
                uri = DefaultPagesDefinition.getNotFoundUri(servletRequest, servletResponse);
            } else if (status == HttpStatus.UNAUTHORIZED) {
                uri = DefaultPagesDefinition.getLoginUri(servletRequest, servletResponse);
            } else if (status == HttpStatus.FORBIDDEN) {
                uri = DefaultPagesDefinition.getUnauthorizedUri(servletRequest, servletResponse, user instanceof User ? (User) user : null);
            } else {
                uri = DefaultPagesDefinition.getErrorUri(servletRequest, servletResponse);
            }

            if (uri != null) {
                try {
                    servletResponse.sendRedirect(uri);
                    return null;
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        // To strip off the double messages generated by this...
        if (ex instanceof NestedRuntimeException)
            ex = (Exception) ((NestedRuntimeException) ex).getMostSpecificCause();

        // If no body provided we will create one
        if (body == null)
            body = new GenericRestException(status, ex);

        //Add admin view if necessary
        MappingJacksonValue value = new MappingJacksonValue(body);
        value.setSerializationView(service.hasAdminRole(user) ? AdminView.class : Object.class);
        return new ResponseEntity<>(value, headers, status);
    }
}
