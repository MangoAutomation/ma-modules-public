/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.infiniteautomation.mango.spring.script.MangoScriptException;
import com.infiniteautomation.mango.spring.script.MangoScriptException.ScriptEvalException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.SourceLocation;

/**
 * @author Jared Wiltshire
 */
public class ScriptRestException extends AbstractRestV2Exception {

    private static final long serialVersionUID = 1L;
    private final String fileName;
    private final Integer lineNumber;
    private final Integer columnNumber;
    private final String scriptStackTrace;

    public ScriptRestException(MangoScriptException cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, MangoRestErrorCode.SCRIPT_EXCEPTION, null, cause);

        if (cause instanceof ScriptEvalException) {
            SourceLocation location = ((ScriptEvalException) cause).getSourceLocation();
            this.fileName = location.getFileName();
            this.lineNumber = location.getLineNumber();
            this.columnNumber = location.getColumnNumber();
            this.scriptStackTrace = location.getStackTrace();
        } else {
            this.fileName = null;
            this.lineNumber = null;
            this.columnNumber = null;
            this.scriptStackTrace = null;
        }
    }

    @Override
    public TranslatableMessage getTranslatableMessage() {
        String message = getShortMessage();

        if (lineNumber == null) {
            return new TranslatableMessage("script.scriptException", message, fileName);
        } else if (columnNumber == null) {
            return new TranslatableMessage("script.scriptExceptionLine", message, fileName, lineNumber);
        } else {
            return new TranslatableMessage("script.scriptExceptionLineColumn", message, fileName, lineNumber, columnNumber);
        }
    }

    @JsonProperty
    public String getShortMessage() {
        return Throwables.getRootCause(this).getMessage();
    }

    @JsonProperty
    public Integer getLineNumber() {
        return lineNumber;
    }

    @JsonProperty
    public Integer getColumnNumber() {
        return columnNumber;
    }

    @JsonProperty
    public String getFileName() {
        return fileName;
    }

    @JsonProperty
    public String getScriptStackTrace() {
        return scriptStackTrace;
    }

}
