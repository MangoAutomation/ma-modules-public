/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

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
