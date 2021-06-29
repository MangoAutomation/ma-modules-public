/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.groovy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.codehaus.groovy.syntax.SyntaxException;
import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.m2m2.module.ScriptEngineDefinition;
import com.serotonin.m2m2.module.SourceLocation;

/**
 * @author Jared Wiltshire
 */
public class GroovyScriptingEngineDefinition extends ScriptEngineDefinition {

    @Autowired
    GroovyPermission permission;

    @Override
    public boolean supports(ScriptEngineFactory engineFactory) {
        return engineFactory instanceof GroovyScriptEngineFactory;
    }

    @Override
    public MangoPermission requiredPermission() {
        return permission.getPermission();
    }

    @Override
    public SourceLocation extractSourceLocation(ScriptException e) {
        Throwable root = StackTraceUtils.extractRootCause(e);

        if (root instanceof MultipleCompilationErrorsException) {
            ErrorCollector errorCollector = ((MultipleCompilationErrorsException) root).getErrorCollector();
            Optional<? extends Message> syntaxErrorMessage = errorCollector.getErrors().stream().filter(m -> m instanceof SyntaxErrorMessage).findFirst();
            if (syntaxErrorMessage.isPresent()) {
                SyntaxErrorMessage syntaxMessage = (SyntaxErrorMessage) syntaxErrorMessage.get();
                SyntaxException syntaxException = syntaxMessage.getCause();
                return new SourceLocation(syntaxException.getSourceLocator(), syntaxException.getStartLine(), syntaxException.getStartColumn(), "");
            }
        }

        StringWriter writer = new StringWriter();
        StackTraceUtils.printSanitizedStackTrace(root, new PrintWriter(writer));
        String stackTraceStr = writer.toString();

        Integer lineNumber = null;
        String scriptFileName = null;
        StackTraceElement[] stackTrace = root.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement frame = stackTrace[i];
            String fileName = frame.getFileName();
            if (fileName != null && fileName.endsWith(".groovy")) {
                scriptFileName = fileName;
                lineNumber = frame.getLineNumber() >= 0 ? frame.getLineNumber() : null;
                break;
            }
        }

        return new SourceLocation(scriptFileName, lineNumber, null, stackTraceStr);
    }
}
