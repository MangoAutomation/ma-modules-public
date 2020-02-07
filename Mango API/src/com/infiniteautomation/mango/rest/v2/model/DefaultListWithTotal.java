/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.List;

/**
 * @author Jared Wiltshire
 */
public class DefaultListWithTotal<T> implements ListWithTotal<T> {

    private final List<T> items;

    public DefaultListWithTotal(List<T> items) {
        this.items = items;
    }

    @Override
    public List<T> getItems() {
        return items;
    }

    @Override
    public int getTotal() {
        return items.size();
    }

}
