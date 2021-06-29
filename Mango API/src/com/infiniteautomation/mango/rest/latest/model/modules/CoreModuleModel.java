/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.modules;

import com.fasterxml.jackson.annotation.JsonView;
import com.serotonin.m2m2.module.Module;

/**
 * @author Jared Wiltshire
 */
public class CoreModuleModel extends ModuleModel {

    @JsonView(AdminView.class)
	String guid;
    String instanceDescription;
    String distributor;
    @JsonView(AdminView.class)
    int upgradeVersionState;
    String storeUrl;

    public CoreModuleModel() {
        super();
    }

    public CoreModuleModel(Module module) {
        super(module);
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public void setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public int getUpgradeVersionState() {
        return upgradeVersionState;
    }

    public void setUpgradeVersionState(int upgradeVersionState) {
        this.upgradeVersionState = upgradeVersionState;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}
