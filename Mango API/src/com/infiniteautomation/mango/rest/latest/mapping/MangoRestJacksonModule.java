/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfig;
import com.infiniteautomation.mango.rest.latest.exception.ExceptionMixin;
import com.infiniteautomation.mango.rest.latest.model.JSONStreamedArray;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeStream;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * Jackson Module to extend JSON rendering
 * @author Terry Packer
 */
public class MangoRestJacksonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public MangoRestJacksonModule() {
        super("MangoRestJacksonModule", new Version(0, 0, 1, "SNAPSHOT", "com.infiniteautomation",
                "mango"));
        this.setMixInAnnotation(Exception.class, ExceptionMixin.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setupModule(SetupContext context) {
        this.addSerializer(JSONStreamedArray.class, new JSONStreamedArraySerializer());
        this.addSerializer(TranslatableMessage.class, new TranslatableMessageSerializer());
        this.addDeserializer(VirtualSerialPortConfig.class, new VirtualSerialPortConfigDeserializer());
        this.addSerializer(PointValueTimeStream.class, new PointValueTimeStreamJsonSerializer());
        this.addSerializer(MangoPermissionModel.class, new MangoPermissionModelSerializer());
        this.addDeserializer(MangoPermissionModel.class, new MangoPermissionModelDeserializer());
        super.setupModule(context);
    }
}
