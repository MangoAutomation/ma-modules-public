/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.exception;

import org.springframework.http.HttpStatus;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * Container to include SMTP session info for failed test email's
 * 
 * @author Terry Packer
 *
 */
public class SendEmailFailedRestException extends AbstractRestV2Exception {

    private static final long serialVersionUID = 1L;
    
    private final String smtpSessionLog;

    public SendEmailFailedRestException(Throwable t, String smtpSessionLog) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, MangoRestErrorCode.GENERIC_500, new TranslatableMessage("common.default", t.getMessage()));
        this.smtpSessionLog = smtpSessionLog;
    }

    public String getSmtpSessionLog() {
        return smtpSessionLog;
    }
}
