/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to hold a group of items and the total, since the items can be limited
 *
 * @author Jared Wiltshire
 */
@JsonPropertyOrder({"items", "total"})
public interface ArrayWithTotal<T> {
    T getItems();
    int getTotal();
}
