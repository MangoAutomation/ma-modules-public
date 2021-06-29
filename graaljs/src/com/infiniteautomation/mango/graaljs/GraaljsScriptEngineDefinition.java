/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.graaljs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.PolyglotException.StackFrame;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.script.MangoScript;
import com.infiniteautomation.mango.spring.script.permissions.LoadFileStorePermission;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.serotonin.m2m2.module.ScriptEngineDefinition;
import com.serotonin.m2m2.module.SourceLocation;

/**
 * @author Jared Wiltshire
 */
public class GraaljsScriptEngineDefinition extends ScriptEngineDefinition {

    @Autowired
    GraaljsPermission permission;
    @Autowired
    FileStoreService fileStoreService;
    @Autowired
    PermissionService permissionService;
    @Autowired
    LoadFileStorePermission loadFileStorePermission;

    @Override
    public boolean supports(ScriptEngineFactory engineFactory) {
        return engineFactory instanceof GraalJSEngineFactory;
    }

    @Override
    public MangoPermission requiredPermission() {
        return permission.getPermission();
    }

    @Override
    public ScriptEngine createEngine(ScriptEngineFactory engineFactory, MangoScript script) {
        MangoFileSystem fs = new MangoFileSystem(FileSystem.newDefaultFileSystem(), fileStoreService, permissionService, loadFileStorePermission);

        // TODO load('http://') doesnt work - only works with built in FS - https://github.com/graalvm/graaljs/issues/338
        // TODO import('http://') doesn't work - https://github.com/graalvm/graaljs/issues/257 & maybe https://github.com/graalvm/graaljs/issues/255
        // TODO import('filestore://') doesn't work - https://github.com/graalvm/graaljs/issues/257
        // load('filestore://') does work

        ScriptEngine engine;
        if (permissionService.hasAdminRole(script)) {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder(JavaScriptLanguage.ID)
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .fileSystem(fs)
                    .option(JSContextOptions.LOAD_FROM_URL_NAME, "true"));
        } else {
            HostAccess disableReflection = HostAccess.newBuilder()
                    .allowPublicAccess(true) // hopefully will get filtering support here - https://github.com/oracle/graal/issues/2425
                    .allowAllImplementations(true)
                    .allowArrayAccess(true)
                    .allowListAccess(true)
                    .allowIterableAccess(true)
                    .allowIteratorAccess(true)
                    .allowBufferAccess(true)
                    .allowMapAccess(true)
                    .denyAccess(ClassLoader.class)
                    .denyAccess(Member.class) // includes Method, Field and Constructor
                    .denyAccess(AnnotatedElement.class) // includes Class
                    .denyAccess(Proxy.class)
                    .denyAccess(Object.class, false) // wait(), notify(), getClass()
                    .denyAccess(System.class) // setProperty(), getProperty(), gc(), exit()
                    .denyAccess(SecurityManager.class)
                    .denyAccess(AccessController.class)
                    .build();

            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder(JavaScriptLanguage.ID)
                    .allowHostAccess(disableReflection)
                    .allowHostClassLookup(null)
                    .allowIO(true)
                    .fileSystem(fs)
                    .allowExperimentalOptions(true) // required for LOAD_FROM_URL_NAME
                    .option(JSContextOptions.LOAD_FROM_URL_NAME, "true"));
        }

        return engine;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object toScriptNative(Object value) {
        if (value instanceof Map) {
            return ProxyObject.fromMap((Map<String, Object>) value);
        } else if (value instanceof List) {
            return ProxyArray.fromList((List<Object>) value);
        } else if (value instanceof Object[]) {
            return ProxyArray.fromArray((Object[]) value);
        }
        return value;
    }

    @Override
    public SourceLocation extractSourceLocation(ScriptException e) {
        Throwable cause = e.getCause();
        if (cause instanceof PolyglotException) {
            PolyglotException polyglotException = (PolyglotException) cause;
            SourceSection location = polyglotException.getSourceLocation();

            StringWriter writer = new StringWriter();
            polyglotException.printStackTrace(new PrintWriter(writer));
            String stackTraceStr = writer.toString();

            // location is set for syntax errors but is null for errors thrown in code e.g. throw new Error();
            if (location == null) {
                Iterator<StackFrame> stackTrace = polyglotException.getPolyglotStackTrace().iterator();
                if (stackTrace.hasNext()) {
                    StackFrame frame = stackTrace.next();
                    location = frame.getSourceLocation();
                }
            }

            if (location != null && location.isAvailable()) {
                Integer lineNumber = location.hasLines() ? location.getStartLine() : null;
                Integer columnNumber = location.hasColumns() ? location.getStartColumn() : null;
                String filename = location.getSource().getName();
                return new SourceLocation(filename, lineNumber, columnNumber, stackTraceStr);
            }

        }
        return super.extractSourceLocation(e);
    }

    @Override
    public boolean singleThreadedAccess() {
        return true;
    }
}
