/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infiniteautomation.mango.db.query.RQLToCondition.RQLVisitException;
import com.serotonin.m2m2.i18n.TranslatableMessage;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
public class RQLVisitRestException extends AbstractRestException {

    private static final long serialVersionUID = 1L;
    @JsonProperty
    private final ASTNode node;

    public RQLVisitRestException(RQLVisitException cause) {
        super(HttpStatus.BAD_REQUEST, MangoRestErrorCode.RQL_VISIT_ERROR, new TranslatableMessage("common.invalidRql"), cause.getCause());
        this.node = cause.getNode();
    }

    public ASTNode getNode() {
        return node;
    }

}
