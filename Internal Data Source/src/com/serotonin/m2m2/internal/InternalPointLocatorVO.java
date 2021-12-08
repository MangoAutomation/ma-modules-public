/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.infiniteautomation.mango.spring.components.ServerMonitoringService;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
@JsonEntity
public class InternalPointLocatorVO extends AbstractPointLocatorVO<InternalPointLocatorVO> implements JsonSerializable {

    public static String[] MONITOR_NAMES = { "", //
            ServerMonitoringService.ENTRIES_MONITOR_ID, //
            ServerMonitoringService.INSTANCES_MONITOR_ID, //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.highPriorityServiceQueueSize", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.mediumPriorityServiceQueueSize", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.scheduledTimerTaskCount", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.maxStackHeight", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.threadCount", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.dbActiveConnections", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.dbIdleConnections", //
            ServerMonitoringService.BATCH_WRITE_SPEED_MONITOR_ID, //
            "java.lang.Runtime.freeMemory",
            "java.lang.Runtime.usedMemory",
            "java.lang.Runtime.maxMemory",
            "java.lang.Runtime.availableProcessors"
    };

    private static Map<String, String> LEGACY_ID_MAP = new HashMap<String, String>();
    static {
        LEGACY_ID_MAP.put("BATCH_ENTRIES", MONITOR_NAMES[1]);
        LEGACY_ID_MAP.put("BATCH_INSTANCES", MONITOR_NAMES[2]);
        LEGACY_ID_MAP.put("MONITOR_HIGH", MONITOR_NAMES[3]);
        LEGACY_ID_MAP.put("MONITOR_MEDIUM", MONITOR_NAMES[4]);
        LEGACY_ID_MAP.put("MONITOR_SCHEDULED", MONITOR_NAMES[5]);
        LEGACY_ID_MAP.put("MONITOR_STACK_HEIGHT", MONITOR_NAMES[6]);
        LEGACY_ID_MAP.put("MONITOR_THREAD_COUNT", MONITOR_NAMES[7]);
        LEGACY_ID_MAP.put("DB_ACTIVE_CONNECTIONS", MONITOR_NAMES[8]);
        LEGACY_ID_MAP.put("DB_IDLE_CONNECTIONS", MONITOR_NAMES[9]);
        LEGACY_ID_MAP.put("BATCH_WRITE_SPEED_MONITOR", MONITOR_NAMES[10]);
        LEGACY_ID_MAP.put("JAVA_FREE_MEMORY", MONITOR_NAMES[11]);
        LEGACY_ID_MAP.put("JAVA_USED_MEMORY", MONITOR_NAMES[12]);
        LEGACY_ID_MAP.put("JAVA_MAX_MEMORY", MONITOR_NAMES[13]);
        LEGACY_ID_MAP.put("JAVA_PROCESSORS", MONITOR_NAMES[14]);
    }

    private List<String> getCurrentMonitorIdList() {
        List<String> validKeys = new ArrayList<String>();
        for(ValueMonitor<?> monitor : Common.MONITORED_VALUES.getMonitors()) {
            validKeys.add(monitor.getId());
        }
        return validKeys;
    }

    private String monitorId = MONITOR_NAMES[1];

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public InternalPointLocatorRT createRuntime() {
        return new InternalPointLocatorRT(this);
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
        try {
            ValueMonitor<?> monitor = Common.MONITORED_VALUES.getMonitor(monitorId);
            return monitor.getName();
        }catch(Exception e) {
            return new TranslatableMessage("internal.missingMonitor", monitorId);
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.NUMERIC;
    }

    @Override
    public String getDataSourceType() {
        return InternalDataSourceDefinition.DATA_SOURCE_TYPE;
    }

    /**
     * @return the monitorId
     */
    public String getMonitorId() {
        return monitorId;
    }

    /**
     * @param monitorId the monitorId to set
     */
    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, monitorId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1){
            int attributeId = in.readInt();
            monitorId = MONITOR_NAMES[attributeId];
        }else if (ver == 2){
            monitorId = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("monitorId", monitorId);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("attributeId");
        if (text == null){
            text = jsonObject.getString("monitorId");
            if(text == null)
                throw new TranslatableJsonException("emport.error.missing", "monitorId", getCurrentMonitorIdList());
            else
                monitorId = text;

        }else{
            if(!LEGACY_ID_MAP.containsKey(text))
                throw new TranslatableJsonException("emport.error.invalid", "attributeId", text,
                        LEGACY_ID_MAP.keySet());
            monitorId = LEGACY_ID_MAP.get(text);
        }
    }
}
