/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.infiniteautomation.mango.db.query.pojo.RQLFilter;

/**
 * @author Jared Wiltshire
 */
public class FilteredStreamWithTotal<T> implements StreamWithTotal<T> {

    private final Supplier<Stream<T>> streamSupplier;
    private final RQLFilter<T> filter;

    public FilteredStreamWithTotal(Iterable<T> iterable, RQLFilter<T> filter) {
        this(() -> {
            if (iterable instanceof Collection) {
                return ((Collection<T>) iterable).stream();
            }
            return StreamSupport.stream(iterable.spliterator(), false);
        }, filter);
    }

    public FilteredStreamWithTotal(Supplier<Stream<T>> streamSupplier, RQLFilter<T> filter) {
        this.streamSupplier = streamSupplier;
        this.filter = filter;
    }

    @Override
    public Stream<T> getItems() {
        return filter.apply(streamSupplier.get());
    }

    @Override
    public int getTotal() {
        return (int) filter.count(streamSupplier.get());
    }

}
