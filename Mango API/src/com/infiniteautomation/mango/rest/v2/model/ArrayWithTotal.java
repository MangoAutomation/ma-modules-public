/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

/**
 * @author Jared Wiltshire
 */
public interface ArrayWithTotal<T> {
    T getItems();
    int getTotal();
}
