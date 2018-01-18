/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

import java.util.List;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <B> body type
 * @param <IR> individual request type
 */
public class BulkRequest<A, B, IR extends IndividualRequest<A, B>> {
    private A action;
    private B body;
    private List<IR> requests;
    
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
    public List<IR> getRequests() {
        return requests;
    }
    public void setRequests(List<IR> requests) {
        this.requests = requests;
    }
}
