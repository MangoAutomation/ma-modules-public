/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.List;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
public class FilteredListWithTotal<T> implements ListWithTotal<T> {

    private final List<T> filtered;
    private final int total;

    public FilteredListWithTotal(List<T> items, ASTNode query) {
        if (query == null) {
            this.filtered = items;
            this.total = items.size();
        } else {
            RQLToObjectListQuery<T> filter = new RQLToObjectListQuery<>();
            this.filtered = query.accept(filter, items);
            this.total = filter.getUnlimitedSize();
        }
    }

    @Override
    public List<T> getItems() {
        return filtered;
    }

    @Override
    public int getTotal() {
        return this.total;
    }

}
