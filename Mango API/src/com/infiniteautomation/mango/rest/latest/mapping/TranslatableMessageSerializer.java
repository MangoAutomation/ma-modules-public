/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Serialize a TranslatableMessage into a useful model
 * @author Terry Packer
 */
public class TranslatableMessageSerializer extends JsonSerializer<TranslatableMessage> {

    @Override
    public void serialize(TranslatableMessage msg, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (msg != null) {
            PermissionHolder holder = Common.getUser();
            jgen.writeString(msg.translate(Translations.getTranslations(holder.getLocaleObject())));
        } else
            jgen.writeNull();
    }
}
