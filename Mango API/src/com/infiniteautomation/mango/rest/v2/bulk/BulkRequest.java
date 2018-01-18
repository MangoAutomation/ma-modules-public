/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

import java.util.List;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <ID> id type
 * @param <B> body type
 */
public class BulkRequest<A, ID, B> {
    private A action;
    private B body;
    private List<IndividualRequest<A, ID, B>> requests;
    
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
    public List<IndividualRequest<A, ID, B>> getRequests() {
        return requests;
    }
    public void setRequests(List<IndividualRequest<A, ID, B>> requests) {
        this.requests = requests;
    }
}
