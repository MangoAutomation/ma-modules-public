/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.ArrayList;
import java.util.List;

import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * Container to simplify validation errors
 *
 * @author Terry Packer
 */
public class RestValidationResult {

    private final List<RestValidationMessage> messages;

    /**
     * For adding validation results from a VO
     */
    public RestValidationResult(ProcessResult result){
        this();
        for(ProcessMessage message : result.getMessages()){
            TranslatableMessage msg;
            if(message.getContextualMessage() != null)
                msg = message.getContextualMessage();
            else
                msg = message.getGenericMessage();
            if(message.getLevel() != null) {
                switch(message.getLevel()) {
                    case info:
                        this.messages.add(new RestValidationMessage(msg, RestMessageLevel.INFORMATION, message.getContextKey()));
                        break;
                    case warning:
                        this.messages.add(new RestValidationMessage(msg, RestMessageLevel.WARNING, message.getContextKey()));
                        break;
                    case error:
                    default:
                        this.messages.add(new RestValidationMessage(msg, RestMessageLevel.ERROR, message.getContextKey()));
                        break;
                }
            }else {
                //Doubtful this will happen but whatever
                this.messages.add(new RestValidationMessage(msg, RestMessageLevel.ERROR, message.getContextKey()));
            }
        }

    }

    public RestValidationResult(){
        this.messages = new ArrayList<RestValidationMessage>();
    }

    /**
     * Create a result with one message
     */
    public RestValidationResult(TranslatableMessage msg, String property){
        this();
        this.addError(msg, property);
    }

    public List<RestValidationMessage> getMessages(){
        return messages;
    }

    /**
     * Add Validation Error
     * @param property with validation error
     */
    public void addError(TranslatableMessage msg, String property){
        this.messages.add(new RestValidationMessage(msg, RestMessageLevel.ERROR, property));
    }

    /**
     * Add Validation Error
     * @param key - i18n key
     * @param property with validation error
     */
    public void addError(String key, String property){
        this.messages.add(new RestValidationMessage(new TranslatableMessage(key), RestMessageLevel.ERROR, property));
    }

    /**
     * Add an invalid value message for a property
     */
    public void addInvalidValueError(String property){
        this.messages.add(new RestValidationMessage(new TranslatableMessage("validate.invalidValue"), RestMessageLevel.ERROR, property));
    }

    /**
     * Add a 'required' message for a property
     */
    public void addRequiredError(String property){
        this.messages.add(new RestValidationMessage(new TranslatableMessage("validate.required"), RestMessageLevel.ERROR, property));
    }

    /**
     * If there are messages throw exception
     */
    public void ensureValid() throws ValidationFailedRestException{
        if(messages.size() > 0)
            throw new ValidationFailedRestException(this);
    }

    /**
     *
     * @author Terry Packer
     *
     */
    public static class RestValidationMessage {

        private RestMessageLevel level;
        private TranslatableMessage message;
        private String property;

        public RestValidationMessage(){
            super();
        }

        /**
         */
        public RestValidationMessage(TranslatableMessage message, RestMessageLevel level, String propertyName) {
            this.message = message;
            this.level = level;
            this.property = propertyName;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public RestMessageLevel getLevel() {
            return level;
        }

        public void setLevel(RestMessageLevel level) {
            this.level = level;
        }

        public TranslatableMessage getMessage() {
            return message;
        }

        public void setMessage(TranslatableMessage message) {
            this.message = message;
        }

    }

    /**
     * @author Terry Packer
     *
     */
    public static enum RestMessageLevel {
        INFORMATION,
        WARNING,
        ERROR
    }
}
