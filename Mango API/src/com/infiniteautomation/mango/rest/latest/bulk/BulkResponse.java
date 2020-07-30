/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.bulk;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Jared Wiltshire
 * @param <IR> individual response type
 */
public class BulkResponse<IR extends IndividualResponse<?, ?, ?>> {
    private boolean hasError;
    private ConcurrentLinkedQueue<IR> responses;
    private Collection<IR> unmodifiableResponses;
    
    public BulkResponse() {
        this.hasError = false;
        this.responses = new ConcurrentLinkedQueue<>();
        this.unmodifiableResponses = Collections.unmodifiableCollection(this.responses);
    }

    public void addResponse(IR response) {
        if (response.getError() != null) {
            this.hasError = true;
        }
        this.responses.add(response);
    }
    
    public boolean isHasError() {
        return hasError;
    }

    public Collection<IR> getResponses() {
        return unmodifiableResponses;
    }
}
