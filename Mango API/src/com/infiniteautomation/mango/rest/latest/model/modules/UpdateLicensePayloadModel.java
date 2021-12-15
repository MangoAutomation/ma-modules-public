/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.modules;

import java.util.Map;

/**
 *
 * @author Terry Packer
 */
public class UpdateLicensePayloadModel {
    private String guid;
    private String description;
    private String distributor;
    private Map<String, String> modules;
    private String storeUrl;
    private int upgradeVersionState;
    private int currentVersionState;

    public UpdateLicensePayloadModel(){ }

    /**
     */
    public UpdateLicensePayloadModel(String guid, String description, String distributor,
            Map<String, String> modules, String storeUrl, int upgradeVersionState,
            int currentVersionState) {
        super();
        this.guid = guid;
        this.description = description;
        this.distributor = distributor;
        this.modules = modules;
        this.storeUrl = storeUrl;
        this.upgradeVersionState = upgradeVersionState;
        this.currentVersionState = currentVersionState;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public Map<String, String> getModules() {
        return modules;
    }

    public void setModules(Map<String, String> modules) {
        this.modules = modules;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }

    /**
     * @return the upgradeVersionState
     */
    public int getUpgradeVersionState() {
        return upgradeVersionState;
    }

    /**
     * @param upgradeVersionState the upgradeVersionState to set
     */
    public void setUpgradeVersionState(int upgradeVersionState) {
        this.upgradeVersionState = upgradeVersionState;
    }

    /**
     * @return the currentVersionState
     */
    public int getCurrentVersionState() {
        return currentVersionState;
    }

    /**
     * @param currentVersionState the currentVersionState to set
     */
    public void setCurrentVersionState(int currentVersionState) {
        this.currentVersionState = currentVersionState;
    }


}