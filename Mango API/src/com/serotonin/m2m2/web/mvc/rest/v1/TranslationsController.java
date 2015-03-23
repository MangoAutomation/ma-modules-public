/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Jared Wiltshire
 */
@Api(value="Translations", description="Retrieves translations of messages")
@RestController
@RequestMapping("/v1/translations")
public class TranslationsController extends MangoRestController {
    
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<Map<String, ?>> translations(
            
            @RequestParam(value = "language", required = false) String language,
            
            HttpServletRequest request) {
        return namespacedTranslations(null, language, request);
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value = "/{namespace}")
    public ResponseEntity<Map<String, ?>> namespacedTranslations(
            
            @ApiParam(value = "Namespace", required = true, allowMultiple = false)
            @PathVariable String namespace,
            
            @RequestParam(value = "language", required = false) String language,
            
            HttpServletRequest request) {
        
        RestProcessResult<Map<String, ?>> result =
                new RestProcessResult<Map<String, ?>>(HttpStatus.OK);
        this.checkUser(request, result);
        
        if (result.isOk()) {
            //User user = Common.getUser();
            // TODO add user locale setting which can be set to "Browser", "Server" or specific value
            
            Locale locale;
            if (language == null || language.isEmpty()) {
                // browser locale
                locale = RequestContextUtils.getLocale(request);
                // server locale
                // locale = Locale.getDefault();
                // user locale
                // locale = user.getLocale();
            }
            else {
                locale = Locale.forLanguageTag(language.replace("_", "-"));
            }
            
            Translations translations = Translations.getTranslations(locale);
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("translations", translations.asMap(namespace));
            resultMap.put("locale", locale.toLanguageTag());
            
            return result.createResponseEntity(resultMap);
        }
        
        return result.createResponseEntity();
    }
}
