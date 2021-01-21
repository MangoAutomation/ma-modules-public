/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.permission.PermissionException;

/**
 * Serialize a TranslatableMessage into a useful model
 * @author Terry Packer
 */
public class TranslatableMessageSerializer extends JsonSerializer<TranslatableMessage> {

    @Override
    public void serialize(TranslatableMessage msg, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (msg != null) {
            Locale locale;
            try {
                locale = Common.getUser().getLocaleObject();
            } catch (PermissionException e) {
                // occurs while generating JSON error responses when the user is unauthenticated
                locale = Common.getLocale();
            }
            jgen.writeString(msg.translate(Translations.getTranslations(locale)));
        } else
            jgen.writeNull();
    }
}
