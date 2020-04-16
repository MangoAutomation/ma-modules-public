/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.List;
import java.util.stream.Stream;

import javax.script.ScriptEngineFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.script.ScriptService;

/**
 * @author Jared Wiltshire
 */
@RestController
@RequestMapping("/script")
public class ScriptRestController {

    final ScriptService scriptService;

    @Autowired
    public ScriptRestController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @RequestMapping(method = {RequestMethod.GET}, value = "/engines")
    public Stream<ScriptEngineModel> getEngines() {
        return this.scriptService.getEngineFactories().stream().map(f -> new ScriptEngineModel(f));
    }

    public static class ScriptEngineModel {
        String engineName;
        String engineVersion;
        List<String> extensions;
        String languageName;
        String languageVersion;
        List<String> mimeTypes;
        List<String> names;

        private ScriptEngineModel(ScriptEngineFactory factory) {
            this.engineName = factory.getEngineName();
            this.engineVersion = factory.getEngineVersion();
            this.extensions = factory.getExtensions();
            this.languageName = factory.getLanguageName();
            this.languageVersion = factory.getLanguageVersion();
            this.mimeTypes = factory.getMimeTypes();
            this.names = factory.getNames();
        }

        public String getEngineName() {
            return engineName;
        }

        public void setEngineName(String engineName) {
            this.engineName = engineName;
        }

        public String getEngineVersion() {
            return engineVersion;
        }

        public void setEngineVersion(String engineVersion) {
            this.engineVersion = engineVersion;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public void setExtensions(List<String> extensions) {
            this.extensions = extensions;
        }

        public String getLanguageName() {
            return languageName;
        }

        public void setLanguageName(String languageName) {
            this.languageName = languageName;
        }

        public String getLanguageVersion() {
            return languageVersion;
        }

        public void setLanguageVersion(String languageVersion) {
            this.languageVersion = languageVersion;
        }

        public List<String> getMimeTypes() {
            return mimeTypes;
        }

        public void setMimeTypes(List<String> mimeTypes) {
            this.mimeTypes = mimeTypes;
        }

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }
    }

}
