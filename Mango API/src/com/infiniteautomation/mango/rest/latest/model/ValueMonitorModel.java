/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 *
 * @author Terry Packer
 */
public class ValueMonitorModel {

    private String id;
    private TranslatableMessage name;
    private Object value;
    private boolean uploadToStore;

    public ValueMonitorModel(ValueMonitor<?> vo) {
        this.id = vo.getId();
        this.name = vo.getName();
        this.value = vo.getValue();
        this.uploadToStore = vo.isUploadToStore();
    }

    public ValueMonitorModel() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TranslatableMessage getName() {
        return name;
    }

    public void setName(TranslatableMessage name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isUploadToStore() {
        return uploadToStore;
    }

    public void setUploadToStore(boolean uploadToStore) {
        this.uploadToStore = uploadToStore;
    }
}
