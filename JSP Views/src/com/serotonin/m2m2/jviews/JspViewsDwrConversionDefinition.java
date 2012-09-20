/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews;

import com.serotonin.m2m2.module.DwrConversionDefinition;

public class JspViewsDwrConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(JspComponentState.class);
    }
}
