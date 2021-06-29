/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

/**
 * The various states an event instance can be in
 * 
 * @author Terry Packer
 *
 */
public enum EventActionEnum {
    ACKNOWLEDGED, //Event was acknowledged by a user
    RAISED, //Event was raised due to some alarm condition
    RETURN_TO_NORMAL, //Event was returned to normal because its cause is no longer active
    DEACTIVATED //Event was deactivated due to a deletion or termination of its source
}
