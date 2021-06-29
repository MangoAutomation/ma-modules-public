/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Terry Packer
 *
 */
public class PurgePointValuesResponseModel {

    private List<String> successfullyPurged;
    private List<String> noEditPermission;
    private List<String> notFound;
    
    public PurgePointValuesResponseModel() {
        this.successfullyPurged = new ArrayList<>();
        this.noEditPermission = new ArrayList<>();
        this.notFound = new ArrayList<>();
    }
    
    /**
     * @return the successfullyPurged
     */
    public List<String> getSuccessfullyPurged() {
        return successfullyPurged;
    }
    /**
     * @param successfullyPurged the successfullyPurged to set
     */
    public void setSuccessfullyPurged(List<String> successfullyPurged) {
        this.successfullyPurged = successfullyPurged;
    }
    /**
     * @return the noEditPermission
     */
    public List<String> getNoEditPermission() {
        return noEditPermission;
    }
    /**
     * @param noEditPermission the noEditPermission to set
     */
    public void setNoEditPermission(List<String> noEditPermission) {
        this.noEditPermission = noEditPermission;
    }
    /**
     * @return the notFound
     */
    public List<String> getNotFound() {
        return notFound;
    }
    /**
     * @param notFound the notFound to set
     */
    public void setNotFound(List<String> notFound) {
        this.notFound = notFound;
    }
    
    
}
