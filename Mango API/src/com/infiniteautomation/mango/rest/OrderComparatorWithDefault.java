/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * @author Jared Wiltshire
 */
public class OrderComparatorWithDefault extends AnnotationAwareOrderComparator {

    private final int defaultOrder;

    public OrderComparatorWithDefault(int defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    @Override
    protected int getOrder(Object obj) {
        if (obj != null) {
            Integer order = findOrder(obj);
            if (order != null) {
                return order;
            }
        }
        return defaultOrder;
    }
}
