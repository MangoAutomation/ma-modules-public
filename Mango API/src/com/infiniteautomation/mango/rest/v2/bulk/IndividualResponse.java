/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Jared Wiltshire
 * @param <ID> id type
 * @param <B> body type
 * @param <E> error type
 */
public class IndividualResponse<ID, B, E> {
    int httpStatus = HttpStatus.OK.value();
    ID id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    B body;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    E error;

    public void setId(ID id) {
        this.id = id;
    }
    public ID getId() {
        return id;
    }
    public int getHttpStatus() {
        return httpStatus;
    }
    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
    public B getBody() {
        return body;
    }
    public void setBody(B body) {
        this.body = body;
    }
    public E getError() {
        return error;
    }
    public void setError(E error) {
        this.error = error;
    }
}
