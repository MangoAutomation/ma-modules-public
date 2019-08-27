/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.email;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.Validatable;
import com.serotonin.web.mail.EmailContent;

/**
 * @author Terry Packer
 *
 */
public class EmailContentModel implements Validatable {

    private String subject;
    private String plainContent;
    private String htmlContent;
    private String encoding;
    
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPlainContent() {
        return plainContent;
    }

    public void setPlainContent(String plainContent) {
        this.plainContent = plainContent;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public EmailContent toEmailContent() {
        return new EmailContent(plainContent, htmlContent, encoding);
    }

    @Override
    public void validate(ProcessResult response) {
        if(StringUtils.isEmpty(htmlContent) && StringUtils.isEmpty(plainContent)) {
            response.addContextualMessage("plainContent", "validate.required");
            response.addContextualMessage("htmlContent", "validate.required");
        }
        if(StringUtils.isEmpty(subject)) {
            response.addContextualMessage("subject", "validate.required");
        }
        if(StringUtils.isEmpty(encoding)) {
            encoding = Common.UTF8;
        }
    }
}
