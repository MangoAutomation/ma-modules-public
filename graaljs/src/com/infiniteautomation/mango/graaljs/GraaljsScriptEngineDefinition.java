/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.graaljs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.script.MangoScript;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.serotonin.m2m2.module.ScriptEngineDefinition;

/**
 * @author Jared Wiltshire
 */
public class GraaljsScriptEngineDefinition extends ScriptEngineDefinition {

    @Autowired
    GraaljsPermission permission;
    @Autowired
    FileStoreService fileStoreService;

    public GraaljsScriptEngineDefinition() {
        // TODO remove when we can
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
        MangoFileSystem fs = new MangoFileSystem(newDefaultFileSystem(), fileStoreService);

        ScriptEngine engine;
        if (permissionService.hasAdminRole(script)) {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .option("js.load-from-url", "true"));
        } else {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(null)
                    .allowIO(true)
                    .fileSystem(fs)
                    .allowExperimentalOptions(true)
                    .option("js.load-from-url", "true"));
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
}
