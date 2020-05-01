/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.graaljs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
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
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.PermissionService;
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

    public GraaljsScriptEngineDefinition() {
        // TODO remove when this issue is resolved https://github.com/graalvm/graaljs/issues/279
        try {
            Class<?> unmodifiableListClazz = Class.forName("java.util.Collections$UnmodifiableList");
            Field unmodifiableListField = unmodifiableListClazz.getDeclaredField("list");
            unmodifiableListField.setAccessible(true);

            Field mimeTypesField = GraalJSEngineFactory.class.getDeclaredField("mimeTypes");
            mimeTypesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> mimeTypes = (List<String>) unmodifiableListField.get(mimeTypesField.get(null));
            mimeTypes.add("application/javascript+module");

            Field extensionsField = GraalJSEngineFactory.class.getDeclaredField("extensions");
            extensionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> extensions = (List<String>) unmodifiableListField.get(extensionsField.get(null));
            extensions.add("mjs");
        } catch (Exception e) {
        }
    }

    @Override
    public boolean supports(ScriptEngineFactory engineFactory) {
        return engineFactory instanceof GraalJSEngineFactory;
    }

    @Override
    public MangoPermission requiredPermission() {
        return permission.getPermission();
    }

    /**
     * No way to obtain the default file system for delegation, remove when <a href="https://github.com/oracle/graal/issues/2190">issue #2190</a> is resolved
     * @return
     */
    private FileSystem newDefaultFileSystem() {
        try {
            Class<?> fsClass = GraaljsScriptEngineDefinition.class.getClassLoader().loadClass("com.oracle.truffle.polyglot.FileSystems");
            Method newDefaultFileSystem = fsClass.getDeclaredMethod("newDefaultFileSystem");
            newDefaultFileSystem.setAccessible(true);
            return (FileSystem) newDefaultFileSystem.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Error getting default filesystem", e);
        }
    }

    @Override
    public ScriptEngine createEngine(ScriptEngineFactory engineFactory, MangoScript script) {
        MangoFileSystem fs = new MangoFileSystem(newDefaultFileSystem(), fileStoreService, permissionService);

        ScriptEngine engine;
        if (!permissionService.hasAdminRole(script)) {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .fileSystem(fs)
                    .option(JSContextOptions.LOAD_FROM_URL_NAME, "true"));
        } else {
            HostAccess disableReflection = HostAccess.newBuilder()
                    .allowPublicAccess(true)
                    .allowAllImplementations(true)
                    .allowArrayAccess(true)
                    .allowListAccess(true)
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
                    Context.newBuilder("js")
                    .allowHostAccess(disableReflection)
                    .allowHostClassLookup(null)
                    .allowIO(true)
                    .fileSystem(fs)
                    .allowExperimentalOptions(true)
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
}
