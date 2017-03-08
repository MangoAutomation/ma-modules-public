/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
@JsonEntity
public class InternalPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {
    public interface Attributes {
        int BATCH_ENTRIES = 1;
        int BATCH_INSTANCES = 2;
        int MONITOR_HIGH = 3;
        int MONITOR_MEDIUM = 4;
        int MONITOR_SCHEDULED = 5;
        int MONITOR_STACK_HEIGHT = 6;
        int MONITOR_THREAD_COUNT = 7;
        int MONITOR_DB_ACTIVE_CONNECTIONS = 8;
        int MONITOR_DB_IDLE_CONNECTIONS = 9;
        int BATCH_WRITE_SPEED_MONITOR = 10;
        int JAVA_FREE_MEMORY = 11;
        int JAVA_USED_MEMORY = 12;
        int JAVA_MAX_MEMORY = 13;
        int JAVA_PROCESSORS = 14;
    }

    // Values in this array correspond to the attribute ids above.
    public static String[] MONITOR_NAMES = { "", //
            "com.serotonin.m2m2.db.dao.PointValueDao$BatchWriteBehind.ENTRIES_MONITOR", //
            "com.serotonin.m2m2.db.dao.PointValueDao$BatchWriteBehind.INSTANCES_MONITOR", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.highPriorityServiceQueueSize", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.mediumPriorityServiceQueueSize", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.scheduledTimerTaskCount", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.maxStackHeight", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.threadCount", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.dbActiveConnections", //
            "com.serotonin.m2m2.rt.maint.WorkItemMonitor.dbIdleConnections", //
            "com.serotonin.m2m2.db.dao.PointValueDao$BatchWriteBehind.BATCH_WRITE_SPEED_MONITOR", //
            "java.lang.Runtime.freeMemory",
            "java.lang.Runtime.usedMemory",
            "java.lang.Runtime.maxMemory",
            "java.lang.Runtime.availableProcessors"
    };

    public static ExportCodes ATTRIBUTE_CODES = new ExportCodes();
    static {
    	ATTRIBUTE_CODES.addElement(Attributes.BATCH_ENTRIES, "BATCH_ENTRIES", "internal.monitor.BATCH_ENTRIES");
    	ATTRIBUTE_CODES.addElement(Attributes.BATCH_INSTANCES, "BATCH_INSTANCES", "internal.monitor.BATCH_INSTANCES");
    	ATTRIBUTE_CODES.addElement(Attributes.MONITOR_HIGH, "MONITOR_HIGH", "internal.monitor.MONITOR_HIGH");
    	ATTRIBUTE_CODES.addElement(Attributes.MONITOR_MEDIUM, "MONITOR_MEDIUM", "internal.monitor.MONITOR_MEDIUM");
        ATTRIBUTE_CODES.addElement(Attributes.MONITOR_SCHEDULED, "MONITOR_SCHEDULED",
                "internal.monitor.MONITOR_SCHEDULED");
        ATTRIBUTE_CODES.addElement(Attributes.MONITOR_STACK_HEIGHT, "MONITOR_STACK_HEIGHT",
                "internal.monitor.MONITOR_STACK_HEIGHT");
        ATTRIBUTE_CODES.addElement(Attributes.MONITOR_THREAD_COUNT, "MONITOR_THREAD_COUNT",
                "internal.monitor.MONITOR_THREAD_COUNT");
        ATTRIBUTE_CODES.addElement(Attributes.MONITOR_DB_ACTIVE_CONNECTIONS, "DB_ACTIVE_CONNECTIONS",
                "internal.monitor.DB_ACTIVE_CONNECTIONS");
        ATTRIBUTE_CODES.addElement(Attributes.MONITOR_DB_IDLE_CONNECTIONS, "DB_IDLE_CONNECTIONS",
                "internal.monitor.DB_IDLE_CONNECTIONS");
        ATTRIBUTE_CODES.addElement(Attributes.BATCH_WRITE_SPEED_MONITOR, "BATCH_WRITE_SPEED_MONITOR",
                "internal.monitor.BATCH_WRITE_SPEED_MONITOR");
        ATTRIBUTE_CODES.addElement(Attributes.JAVA_FREE_MEMORY, "JAVA_FREE_MEMORY",
                "java.monitor.JAVA_FREE_MEMORY");
        ATTRIBUTE_CODES.addElement(Attributes.JAVA_USED_MEMORY, "JAVA_USED_MEMORY",
                "java.monitor.JAVA_USED_MEMORY");
        ATTRIBUTE_CODES.addElement(Attributes.JAVA_MAX_MEMORY, "JAVA_MAX_MEMORY",
                "java.monitor.JAVA_MAX_MEMORY");
        ATTRIBUTE_CODES.addElement(Attributes.JAVA_PROCESSORS, "JAVA_PROCESSORS",
                "java.monitor.JAVA_PROCESSORS");        
        
    };

    //Map of Monitor ID to legacy JSON code (or if new then just Monitor ID)
    public static Map<String, String> MONITOR_EXPORT_CODES = new HashMap<String, String>();
    static{
    	
    	//Add in new codes for anything missing
    	for(ValueMonitor<?> monitor : Common.MONITORED_VALUES.getMonitors()){
    		//Do we have this one?
    		int attributeId = ATTRIBUTE_CODES.getId(monitor.getId());
    		if( attributeId > 0){
    			MONITOR_EXPORT_CODES.put(monitor.getId(), ATTRIBUTE_CODES.getCode(attributeId));
    		}else{
    			//Use its ID in the JSON
    			MONITOR_EXPORT_CODES.put(monitor.getId(), monitor.getId());
    		}
    	}
    }
    
    private Set<String> getCurrentMonitorIdSet() {
    	Set<String> validKeys = new HashSet<String>();
    	for(ValueMonitor<?> monitor : Common.MONITORED_VALUES.getMonitors()) {
    		validKeys.add(monitor.getId());
    	}
    	return validKeys;
    }
    
    private String monitorId = MONITOR_NAMES[Attributes.BATCH_ENTRIES];
    
    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new InternalPointLocatorRT(this);
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
    	ValueMonitor<?> monitor = Common.MONITORED_VALUES.getValueMonitor(monitorId);
    	if(monitor != null)
    		return monitor.getName();
    	else
    		return new TranslatableMessage("internal.missingMonitor", monitorId);
    }

    @Override
    public int getDataTypeId() {
        return DataTypes.NUMERIC;
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

    @Override
    public void validate(ProcessResult response) {
    	ValueMonitor<?> monitor = Common.MONITORED_VALUES.getValueMonitor(monitorId);
    	if(monitor == null)
            response.addContextualMessage("monitorId", "validate.invalidValue");
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
        		throw new TranslatableJsonException("emport.error.missing", "monitorId", getCurrentMonitorIdSet());
        	else
        		monitorId = text;
        	if(Common.MONITORED_VALUES.getValueMonitor(monitorId) == null)
        		throw new TranslatableJsonException("emport.error.invalid", "monitorId", text,
        				getCurrentMonitorIdSet());
        	
        }else{
        	//TODO this definitely doesn't work or do anything. Should fix
        	int attributeId = ATTRIBUTE_CODES.getId(text);
            if (!ATTRIBUTE_CODES.isValidId(attributeId))
                throw new TranslatableJsonException("emport.error.invalid", "attributeId", text,
                        ATTRIBUTE_CODES.getCodeList());
            
        }
    }
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#asModel()
	 */
	@Override
	public PointLocatorModel<?> asModel() {
		return new InternalPointLocatorModel(this);
	}
}
