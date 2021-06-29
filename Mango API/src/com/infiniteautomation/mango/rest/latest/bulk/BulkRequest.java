/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.bulk;

import java.util.List;

/**
 * @author Jared Wiltshire
 * @param <A> action type
 * @param <B> body type
 * @param <IR> individual request type
 */
public class BulkRequest<A, B, IR extends IndividualRequest<A, B>> {
    private String id;
    private A action;
    private B body;
    private List<IR> requests;
    private Long timeout;
    private Long expiration;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
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
    public Long getTimeout() {
        return timeout;
    }
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
    public Long getExpiration() {
        return expiration;
    }
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
