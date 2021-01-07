/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.security.permissions.AnonymousAccess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Jared Wiltshire
 */
@Api(value="i18n Translations access")
@RestController
@RequestMapping("/translations")
public class TranslationsController {

    //Namespaces available for public endpoint
    private final List<String> publicNamespaces;

    public TranslationsController(){
        this.publicNamespaces = new ArrayList<String>();
        this.publicNamespaces.add("public");
        this.publicNamespaces.add("login");
        this.publicNamespaces.add("header");
    }

    public static class TranslationsModel {
        private String locale;
        private Map<String, Map<String,String>> translations;
        private String[] namespaces;

        public String getLocale() {
            return locale;
        }
        public void setLocale(String locale) {
            this.locale = locale;
        }
        public Map<String, Map<String, String>> getTranslations() {
            return translations;
        }
        public void setTranslations(Map<String, Map<String, String>> translations) {
            this.translations = translations;
        }
        public String[] getNamespaces() {
            return namespaces;
        }
        public void setNamespaces(String[] namespaces) {
            this.namespaces = namespaces;
        }
    }

    @ApiOperation(value = "Get all translations", notes = "Kitchen sink of translations")
    @RequestMapping(method = RequestMethod.GET)
    public TranslationsModel translations(
            @ApiParam(value = "Language for translations", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "server", required = false, defaultValue = "false") boolean server,
            @RequestParam(value = "browser", required = false, defaultValue = "false") boolean browser,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        return namespacedTranslations(null, language, server, browser, user, request);
    }

    @ApiOperation(value = "Get translations based on namespaces", notes = "Namespace must be base namespace, ie common not common.messages. Returns sub-namespaces too.  For > 1 use comma common,public")
    @RequestMapping(method = RequestMethod.GET, value = "/{namespaces}")
    public TranslationsModel namespacedTranslations(
            @ApiParam(value = "Message Namespaces, simmilar to java package structure", allowMultiple = true)
            @PathVariable String[] namespaces,
            @ApiParam(value = "Language for translation (must have language pack installed)", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            @ApiParam(value = "Use server language for translation", allowMultiple = false)
            @RequestParam(value = "server", required = false, defaultValue = "false") boolean server,
            @RequestParam(value = "browser", required = false, defaultValue = "false") boolean browser,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        TranslationsModel resultMap = new TranslationsModel();
        Locale locale = this.getLocale(language, server, browser, request, user);

        resultMap.setLocale(locale.toLanguageTag());
        resultMap.setTranslations(getTranslationMap(namespaces, locale));
        resultMap.setNamespaces(namespaces);
        return resultMap;
    }

    @ApiOperation(value = "Get translations for public namespaces", notes = "Namespace must be base , ie public not public.messages. Returns sub-namespaces too. For > 1 use comma common,public")
    @RequestMapping(method = RequestMethod.GET, value = "/public/{namespaces}")
    @AnonymousAccess
    public TranslationsModel publicNamespacedTranslations(
            @ApiParam(value = "Message Namespaces, simmilar to java package structure", allowMultiple = true)
            @PathVariable String[] namespaces,
            @ApiParam(value = "Language for translation (must have language pack installed)", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            @ApiParam(value = "Use server language for translation", allowMultiple = false)
            @RequestParam(value = "server", required = false, defaultValue = "false") boolean server,
            @RequestParam(value = "browser", required = false, defaultValue = "false") boolean browser,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        //Confirm the requested namespace is indeed public
        for(String namespace : namespaces){
            if(!this.publicNamespaces.contains(namespace)){
                throw new BadRequestException();
            }
        }
        TranslationsModel resultMap = new TranslationsModel();
        Locale locale = this.getLocale(language, server, browser, request, user);

        resultMap.setLocale(locale.toLanguageTag());
        resultMap.setTranslations(getTranslationMap(namespaces, locale));
        resultMap.setNamespaces(namespaces);
        return resultMap;
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Clear the translation cache", notes = "Translations will be reloaded from .properties files upon next translation request")
    @RequestMapping(method = RequestMethod.POST, value = "/clear-cache")
    public void clearCache() {
        Translations.clearCache();
    }

    /**
     * Get the locale for the request
     * @param language
     * @param request
     * @return
     */
    private Locale getLocale(String language, boolean server, boolean browser, HttpServletRequest request, User user) {
        if (!StringUtils.isBlank(language)) {
            return Locale.forLanguageTag(language.replace('_', '-'));
        }

        if (browser) {
            return RequestContextUtils.getLocale(request);
        } else if (server) {
            return Common.getLocale();
        }

        String userLocale = null;
        if (user != null) {
            userLocale = user.getLocale();
        }

        if (user == null || StringUtils.isBlank(userLocale)) {
            return Common.getLocale();
        } else {
            return Locale.forLanguageTag(userLocale.replace('_', '-'));
        }
    }

    /**
     * Get a set of translations for many namespaces
     * @param namespaces
     * @param locale
     * @return
     */
    public static Map<String, Map<String,String>> getTranslationMap(String[] namespaces, Locale locale) {
        Translations translations = Translations.getTranslations(locale);
        Map<String, Map<String,String>> resultMap = new HashMap<String, Map<String,String>>();
        if(namespaces == null) {
            resultMap.putAll(translations.asMap());
        }else {
            for(String namespace : namespaces){
                Map<String, Map<String,String>> tranMap = translations.asMap(namespace);
                Iterator<String> it = tranMap.keySet().iterator();
                while(it.hasNext()){
                    String key = it.next();
                    Map<String,String> submap = resultMap.get(key);
                    if(submap == null){
                        submap = new HashMap<String,String>();
                        resultMap.put(key, submap);
                    }
                    submap.putAll(tranMap.get(key));
                }
            }
        }
        return resultMap;
    }

    public static TranslationsModel getTranslations(String[] namespaces, Locale locale) {
        TranslationsModel resultMap = new TranslationsModel();
        resultMap.setLocale(locale.toLanguageTag());
        resultMap.setTranslations(getTranslationMap(namespaces, locale));
        resultMap.setNamespaces(namespaces);
        return resultMap;
    }
}
