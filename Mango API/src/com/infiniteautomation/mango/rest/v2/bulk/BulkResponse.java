/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jared Wiltshire
 * @param <IR> individual response type
 */
public class BulkResponse<IR extends IndividualResponse<?, ?, ?>> {
    boolean hasError;
    List<IR> responses;
    
    public BulkResponse(int size) {
        this.hasError = false;
        this.responses = new ArrayList<>(size);
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

    public List<IR> getResponses() {
        return responses;
    }
}
