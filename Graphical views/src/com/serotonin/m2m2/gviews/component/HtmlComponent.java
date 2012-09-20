/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class HtmlComponent extends ViewComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("html", "HTML", "graphic.html", null);

    @JsonProperty
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public void validateDataPoint(User user, boolean makeReadOnly) {
        // no op
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean containsValidVisibleDataPoint(int dataPointId) {
        return false;
    }

    public String getStaticContent() {
        return content;
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        SerializationHelper.writeSafeUTF(out, content);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1)
            content = SerializationHelper.readSafeUTF(in);
    }
}
