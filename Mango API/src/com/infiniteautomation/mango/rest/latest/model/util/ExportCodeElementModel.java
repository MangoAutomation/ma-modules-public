/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.util;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes.Element;

/**
 *
 * @author Terry Packer
 */
public class ExportCodeElementModel {
    private int id;
    private String code;
    private String key;
    private TranslatableMessage message;
    
    public ExportCodeElementModel(Element e) {
        this.id = e.getId();
        this.code = e.getCode();
        this.key = e.getKey();
        this.message = new TranslatableMessage(key);
    }
    
    public ExportCodeElementModel() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TranslatableMessage getMessage() {
        return message;
    }
}
