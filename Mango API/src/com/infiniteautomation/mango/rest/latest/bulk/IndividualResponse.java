/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

import org.springframework.http.HttpStatus;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <B> body type
 * @param <E> error type
 */
public class IndividualResponse<A, B, E> {
    int httpStatus = HttpStatus.OK.value();
    A action;
    B body;
    E error;

    public A getAction() {
        return action;
    }
    public void setAction(A action) {
        this.action = action;
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
