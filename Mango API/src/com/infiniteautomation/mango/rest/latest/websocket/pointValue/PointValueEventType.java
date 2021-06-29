/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.pointValue;

/**
 * @author Terry Packer
 *
 */
public enum PointValueEventType {

    INITIALIZE,
    UPDATE, //Value was updated to possibly the same value
    CHANGE, //Value has changed
    SET,
    BACKDATE,
    TERMINATE,
    REGISTERED, //We registered and this is our first response
    ATTRIBUTE_CHANGE,
    LOGGED

}
