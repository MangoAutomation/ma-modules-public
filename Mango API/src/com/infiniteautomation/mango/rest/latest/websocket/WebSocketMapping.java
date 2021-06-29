/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Jared Wiltshire
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface WebSocketMapping {
    String[] value() default {};
}
