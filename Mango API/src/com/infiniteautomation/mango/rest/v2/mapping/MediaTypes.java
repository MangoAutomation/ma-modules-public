/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import org.springframework.http.MediaType;

/**
 * @author Jared Wiltshire
 */
// TODO Mango 3.5 - Use Common.MediaTypes from Core where possible
public final class MediaTypes {
    private MediaTypes() {}

    public static final MediaType CSV = new MediaType("text", "csv");
    public static final MediaType GENERIC_CSV = new MediaType("text", "vnd.infinite-automation-systems.mango.generic-csv");
    public static final MediaType SEROTONIN_JSON = new MediaType("application", "vnd.infinite-automation-systems.mango.serotonin-json");

    // TODO Mango 3.5 remove this mime type - should be vendor prefixed
    public static final MediaType SEROTONIN_JSON_OLD = new MediaType("application", "sero-json");
}
