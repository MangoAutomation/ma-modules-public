/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <ID> id type
 * @param <B> body type
 */
public class IndividualRequest<A, ID, B> {
    A action;
    ID id;
    B body;

    public A getAction() {
        return action;
    }
    public void setAction(A action) {
        this.action = action;
    }
    public ID getId() {
        return id;
    }
    public void setId(ID id) {
        this.id = id;
    }
    public B getBody() {
        return body;
    }
    public void setBody(B body) {
        this.body = body;
    }
}