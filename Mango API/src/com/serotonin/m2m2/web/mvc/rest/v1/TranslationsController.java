/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Jared Wiltshire
 */
@Api(value="Translations", description="Translations")
@RestController
@RequestMapping("/v1/translations")
public class TranslationsController extends MangoRestController {
    
	//Namespaces available for public endpoint
	private final List<String> publicNamespaces;
	
	public TranslationsController(){
		this.publicNamespaces = new ArrayList<String>();
		this.publicNamespaces.add("public");
		this.publicNamespaces.add("login");
		this.publicNamespaces.add("header");
	}
	
	@ApiOperation(value = "Get all translations", notes = "Kitchen sink of translations")
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<Map<String, ?>> translations(
    		@ApiParam(value = "Language for translations", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            HttpServletRequest request) {
        return namespacedTranslations(null, language, request);
    }
    
	@ApiOperation(value = "Get translations based on namespaces", notes = "Namespace must be base namespace, ie common not common.messages. Returns sub-namespaces too.  For > 1 use comma common,public")
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value = "/{namespaces}")
    public ResponseEntity<Map<String, ?>> namespacedTranslations(
            @ApiParam(value = "Message Namespaces, simmilar to java package structure", allowMultiple = true)
            @PathVariable String[] namespaces,
            @ApiParam(value = "Language for translation (must have language pack installed)", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            
            HttpServletRequest request) {
        
        RestProcessResult<Map<String, ?>> result =
                new RestProcessResult<Map<String, ?>>(HttpStatus.OK);
        this.checkUser(request, result);
        
        if (result.isOk()) {
           
        	Map<String, Object> resultMap = new HashMap<String, Object>();
            Locale locale = this.getLocale(language, request);
            resultMap.put("locale", locale.toLanguageTag());
        	resultMap.put("translations", getTranslationMap(namespaces, locale));
            
            return result.createResponseEntity(resultMap);
        }
        
        return result.createResponseEntity();
    }
	
	@ApiOperation(value = "Get translations for public namespaces", notes = "Namespace must be base , ie public not public.messages. Returns sub-namespaces too. For > 1 use comma common,public")
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value = "/public/{namespaces}")
    public ResponseEntity<Map<String, ?>> publicNamespacedTranslations(
            @ApiParam(value = "Message Namespaces, simmilar to java package structure", allowMultiple = true)
            @PathVariable String[] namespaces,
            @ApiParam(value = "Language for translation (must have language pack installed)", allowMultiple = false)
            @RequestParam(value = "language", required = false) String language,
            
            HttpServletRequest request) {
        
        RestProcessResult<Map<String, ?>> result = new RestProcessResult<Map<String, ?>>(HttpStatus.OK);
        
        //Confirm the requested namespace is indeed public
        for(String namespace : namespaces){
	        if(!this.publicNamespaces.contains(namespace)){
	        	result.addRestMessage(getUnauthorizedMessage());
	        	return result.createResponseEntity();
	        }
        }
        
        if (result.isOk()) {

        	Map<String, Object> resultMap = new HashMap<String, Object>();
            Locale locale = this.getLocale(language, request);
            resultMap.put("locale", locale.toLanguageTag());
        	resultMap.put("translations", getTranslationMap(namespaces, locale));
            
            return result.createResponseEntity(resultMap);
        }
        
        return result.createResponseEntity();
    }
	

	/**
	 * Get the locale for the request
	 * @param language
	 * @param request
	 * @return
	 */
	private Locale getLocale(String language, HttpServletRequest request){
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
        return locale;
	}

	/**
	 * Get a set of translations for many namespaces
	 * @param namespaces
	 * @param locale
	 * @return
	 */
	private Map<String, Map<String,String>> getTranslationMap(String[] namespaces, Locale locale){
        Translations translations = Translations.getTranslations(locale);
        Map<String, Map<String,String>> resultMap = new HashMap<String, Map<String,String>>();
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
        return resultMap;
	}
	
}
