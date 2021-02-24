/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.nashorn;

import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import com.infiniteautomation.mango.spring.script.engines.NashornScriptEngineDefinition;
import com.serotonin.m2m2.module.ConditionalDefinition;

/**
 * @author Jared Wiltshire
 */
@ConditionalDefinition(requireClasses = {"org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory"})
public class StandaloneNashornScriptEngineDefinition extends NashornScriptEngineDefinition {

    @Override
    public boolean supports(ScriptEngineFactory engineFactory) {
        return "org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory".equals(engineFactory.getClass().getName());
    }

    @Override
    protected Object callFunction(Object function, Object thiz, Object... args) {
        return ((JSObject) function).call(thiz, args);
    }

    @Override
    public ScriptEngine createScriptEngine(ScriptEngineFactory engineFactory, Function<String, Boolean> filter) {
        if (filter == null) {
            return engineFactory.getScriptEngine();
        }
        return ((NashornScriptEngineFactory) engineFactory).getScriptEngine(filter::apply);
    }

}
