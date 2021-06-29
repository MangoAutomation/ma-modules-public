/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.util;

/**
 * @author Jared Wiltshire
 */
public interface ExceptionMapper<E> {
    public E mapException(Throwable e);
}
