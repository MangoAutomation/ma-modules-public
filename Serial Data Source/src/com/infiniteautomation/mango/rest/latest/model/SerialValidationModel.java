/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

/**
 * @author Terry Packer
 *
 */
public class SerialValidationModel {

    private boolean hex;
    private String message;
    private String messageRegex;
    private String messageTerminator;
    private int pointIdentifierIndex;
    private boolean useTerminator;
    public boolean isHex() {
        return hex;
    }
    public void setHex(boolean hex) {
        this.hex = hex;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessageRegex() {
        return messageRegex;
    }
    public void setMessageRegex(String messageRegex) {
        this.messageRegex = messageRegex;
    }
    public String getMessageTerminator() {
        return messageTerminator;
    }
    public void setMessageTerminator(String messageTerminator) {
        this.messageTerminator = messageTerminator;
    }
    public int getPointIdentifierIndex() {
        return pointIdentifierIndex;
    }
    public void setPointIdentifierIndex(int pointIdentifierIndex) {
        this.pointIdentifierIndex = pointIdentifierIndex;
    }
    public boolean isUseTerminator() {
        return useTerminator;
    }
    public void setUseTerminator(boolean useTerminator) {
        this.useTerminator = useTerminator;
    }
    
}
