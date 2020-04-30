/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.exception;

import javax.script.ScriptException;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.infiniteautomation.mango.spring.script.MangoScriptException;
import com.infiniteautomation.mango.spring.script.MangoScriptException.ScriptEvalException;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Jared Wiltshire
 */
public class ScriptRestException extends AbstractRestV2Exception {

    private static final long serialVersionUID = 1L;
    private final Integer lineNumber;
    private final Integer columnNumber;

    public ScriptRestException(MangoScriptException cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, null, null, cause);

        if (cause instanceof ScriptEvalException) {
            ScriptException scriptException = ((ScriptEvalException) cause).getScriptExceptionCause();
            this.lineNumber = scriptException.getLineNumber() >= 0 ? scriptException.getLineNumber() : null;
            this.columnNumber = scriptException.getColumnNumber() >= 0 ? scriptException.getColumnNumber() : null;
        } else {
            this.lineNumber = null;
            this.columnNumber = null;
        }
    }

    @Override
    public TranslatableMessage getTranslatableMessage() {
        String message = Throwables.getRootCause(this).getMessage();

        if (lineNumber == null) {
            return new TranslatableMessage("script.scriptException", message);
        } else if (columnNumber == null) {
            return new TranslatableMessage("script.scriptExceptionLine", message, lineNumber);
        } else {
            return new TranslatableMessage("script.scriptExceptionLineColumn", message, lineNumber, columnNumber);
        }
    }

    @JsonProperty
    public Integer getLineNumber() {
        return lineNumber;
    }

    @JsonProperty
    public Integer getColumnNumber() {
        return columnNumber;
    }

}
