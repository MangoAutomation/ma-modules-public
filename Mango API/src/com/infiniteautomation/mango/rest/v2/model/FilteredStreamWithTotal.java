/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.stream.Stream;

import com.infiniteautomation.mango.db.query.pojo.RQLFilter;

/**
 * @author Jared Wiltshire
 */
public class FilteredStreamWithTotal<T> implements ArrayWithTotal<Stream<T>> {

    private final Stream<T> items;
    private final RQLFilter<T> filter;

    public FilteredStreamWithTotal(Stream<T> items, RQLFilter<T> filter) {
        this.items = items;
        this.filter = filter;
    }

    @Override
    public Stream<T> getItems() {
        return filter.apply(items);
    }

    @Override
    public int getTotal() {
        return (int) filter.getTotal();
    }

}
