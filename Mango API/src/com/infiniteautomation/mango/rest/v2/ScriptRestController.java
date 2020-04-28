/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngineFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.resolver.RemainingPath;
import com.infiniteautomation.mango.spring.script.EvalContext;
import com.infiniteautomation.mango.spring.script.PathMangoScript;
import com.infiniteautomation.mango.spring.script.ScriptService;
import com.infiniteautomation.mango.spring.script.permissions.RequestResponsePermission;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Jared Wiltshire
 */
@RestController
@RequestMapping("/script")
public class ScriptRestController {

    final ScriptService scriptService;
    final FileStoreService fileStoreService;
    final RoleService roleService;
    final PermissionService permissionService;
    final RequestResponsePermission requestResponsePermission;

    @Autowired
    public ScriptRestController(ScriptService scriptService, FileStoreService fileStoreService, RoleService roleService,
            PermissionService permissionService, RequestResponsePermission requestResponsePermission) {
        this.scriptService = scriptService;
        this.fileStoreService = fileStoreService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.requestResponsePermission = requestResponsePermission;
    }

    @RequestMapping(method = {RequestMethod.GET}, value = "/engines")
    public Stream<ScriptEngineModel> getEngines() {
        return this.scriptService.getEngineFactories().map(f -> new ScriptEngineModel(f));
    }

    @Async
    @ApiOperation(value = "Evaluate a filestore file as a script on the backend using a scripting engine")
    @RequestMapping(method = RequestMethod.POST, value="/eval-file-store/{fileStoreName}/**")
    public CompletableFuture<Void> evalScript(
            @ApiParam(value = "File store name", required = true)
            @PathVariable(required = true) String fileStoreName,

            @ApiParam(value = "Script engine name", required = false)
            @RequestParam(required = false) String engineName,

            @ApiParam(value = "Script file character set", required = false, defaultValue = "UTF-8")
            @RequestParam(required = false, defaultValue = "UTF-8") String fileCharset,

            @ApiParam(value = "Script roles", required = false, allowMultiple = true)
            @RequestParam(required = false) String[] roles,

            @RemainingPath String path,

            @AuthenticationPrincipal User user,

            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Path filePath = fileStoreService.getPathForRead(fileStoreName, path);
        if (!Files.exists(filePath)) {
            throw new NotFoundException();
        }

        if (engineName == null) {
            engineName = scriptService.findEngineForFile(filePath);
        }

        Charset fileCharsetParsed = Charset.forName(fileCharset);

        Set<Role> roleSet;
        if (roles != null) {
            roleSet = Arrays.stream(roles).map(xid -> this.roleService.get(xid).getRole()).collect(Collectors.toSet());
        } else {
            roleSet = user.getRoles();
        }

        EvalContext evalContext = new EvalContext();

        Reader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), Charset.forName(request.getCharacterEncoding())));
        Writer writer = new OutputStreamWriter(response.getOutputStream(), Charset.forName(response.getCharacterEncoding()));
        evalContext.setReader(reader);
        evalContext.setWriter(writer);
        evalContext.addBinding("reader", reader);
        evalContext.addBinding("writer", writer);

        if (permissionService.hasPermission(user, requestResponsePermission.getPermission())) {
            evalContext.addBinding("request", request);
            evalContext.addBinding("response", response);
        }

        this.scriptService.eval(new PathMangoScript(engineName, roleSet, filePath, fileCharsetParsed), evalContext);

        return CompletableFuture.completedFuture(null);
    }

    public static class ScriptEvalModel {
        String engineName;
        Set<String> roles;
        String script;
        String filePath;
        String fileCharset;
        Map<String, Object> bindings;

        public String getEngineName() {
            return engineName;
        }
        public void setEngineName(String engineName) {
            this.engineName = engineName;
        }
        public Set<String> getRoles() {
            return roles;
        }
        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }
        public String getScript() {
            return script;
        }
        public void setScript(String script) {
            this.script = script;
        }
        public String getFilePath() {
            return filePath;
        }
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        public Map<String, Object> getBindings() {
            return bindings;
        }
        public void setBindings(Map<String, Object> bindings) {
            this.bindings = bindings;
        }
        public String getFileCharset() {
            return fileCharset;
        }
        public void setFileCharset(String fileCharset) {
            this.fileCharset = fileCharset;
        }
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
