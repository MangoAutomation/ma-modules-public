/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.genericcsv;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Jared Wiltshire
 */
public class CsvJacksonModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public CsvJacksonModule() {
        super("CsvJacksonModule");
        LongAsDateSerializer longAsDateSerializer = new LongAsDateSerializer();
        this.addSerializer(Long.class, longAsDateSerializer);
        this.addSerializer(long.class, longAsDateSerializer);
    }

}