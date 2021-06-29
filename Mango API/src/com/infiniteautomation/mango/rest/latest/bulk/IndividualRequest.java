/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.bulk;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <B> body type
 */
public class IndividualRequest<A, B> {
    A action;
    B body;

    public A getAction() {
        return action;
    }
    public void setAction(A action) {
        this.action = action;
    }
    public B getBody() {
        return body;
    }
    public void setBody(B body) {
        this.body = body;
    }
}
