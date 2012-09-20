/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Matthew Lohbihler
 */
public class CompoundChild {
    private final String id;
    private final TranslatableMessage description;
    private final ViewComponent viewComponent;
    private final int[] dataTypesOverride;

    public CompoundChild(String id, TranslatableMessage description, ViewComponent viewComponent,
            int[] dataTypesOverride) {
        this.id = id;
        this.description = description;
        this.viewComponent = viewComponent;
        this.dataTypesOverride = dataTypesOverride;
    }

    public String getId() {
        return id;
    }

    public TranslatableMessage getDescription() {
        return description;
    }

    public ViewComponent getViewComponent() {
        return viewComponent;
    }

    public int[] getDataTypes() {
        if (dataTypesOverride != null)
            return dataTypesOverride;
        if (viewComponent.isPointComponent())
            return ((PointComponent) viewComponent).getSupportedDataTypes();
        return null;
    }
}
