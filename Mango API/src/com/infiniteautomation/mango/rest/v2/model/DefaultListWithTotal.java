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
    private final Integer total;

    public DefaultListWithTotal(List<T> items) {
        this.items = items;
        this.total = null;
    }

    public DefaultListWithTotal(List<T> items, int total) {
        this.items = items;
        this.total = total;
    }

    @Override
    public List<T> getItems() {
        return items;
    }

    @Override
    public int getTotal() {
        if (this.total != null) {
            return this.total;
        }
        return items.size();
    }

}
