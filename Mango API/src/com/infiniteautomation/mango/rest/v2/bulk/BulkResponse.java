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
    List<IR> results;
    
    public BulkResponse(int size) {
        this.hasError = false;
        this.results = new ArrayList<>(size);
    }

    public void addResult(IR result) {
        if (result.getError() != null) {
            this.hasError = true;
        }
        this.results.add(result);
    }
    
    public boolean isHasError() {
        return hasError;
    }

    public List<IR> getResults() {
        return results;
    }
}
