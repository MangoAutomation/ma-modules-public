/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.ModelNotFoundException;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractActionVoModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
@JsonPropertyOrder({"xid", "name", "enabled"})
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=AbstractDataSourceModel.MODEL_TYPE)
public abstract class AbstractDataSourceModel<T extends DataSourceVO<T>> extends AbstractActionVoModel<T>{

    public static final String MODEL_TYPE = "modelType";
    
    public AbstractDataSourceModel() {
        super(null);
        //This little hack allows us to subclass and create the vo there
        // letting Jackson use JsonTypeInfo as designed
        this.data = newVO();
    }
    public AbstractDataSourceModel(T data) throws ModelNotFoundException {
        super(data);
    }

    public T newVO() throws ModelNotFoundException {
        DataSourceDefinition def = getDefinition();
        @SuppressWarnings("unchecked")
        T vo = (T) def.baseCreateDataSourceVO();
        vo.setDefinition(def);
        return vo;
    }
    
    @JsonIgnore
    public DataSourceDefinition getDefinition() throws ModelNotFoundException {
        DataSourceDefinition definition = ModuleRegistry.getDataSourceDefinition(getModelType());
        if(definition == null)
            throw new ModelNotFoundException(getModelType());
        return definition;
    }
    
    @JsonGetter(value="alarmLevels")
    public Map<String,String> getAlarmLevels(){
        ExportCodes eventCodes = this.data.getEventCodes();
        Map<String, String> alarmCodeLevels = new HashMap<>();

        if (eventCodes != null && eventCodes.size() > 0) {

            for (int i = 0; i < eventCodes.size(); i++) {
                int eventId = eventCodes.getId(i);
                AlarmLevels level = this.data.getAlarmLevel(eventId, AlarmLevels.URGENT);
                alarmCodeLevels.put(eventCodes.getCode(eventId), level.name());
            }
        }
        return alarmCodeLevels;
    }

    @JsonSetter(value="alarmLevels")
    public void setAlarmLevels(Map<String,String> alarmCodeLevels) throws TranslatableJsonException{
        if (alarmCodeLevels != null) {
            for (String code : alarmCodeLevels.keySet()) {
                try {
                    this.data.setAlarmLevel(code, AlarmLevels.fromName(alarmCodeLevels.get(code)));
                } catch (IllegalArgumentException | NullPointerException e) {
                    ProcessResult result = new ProcessResult();
                    result.addContextualMessage("alarmLevel", "emport.error.alarmLevel", alarmCodeLevels.get(code), code,
                            Arrays.asList(AlarmLevels.values()));
                    throw new ValidationException(result);
                }
            }
        }
    }

    @JsonGetter(value="purgeSettings")
    public PurgeSettings getPurgeSettings(){
        return new PurgeSettings(this.data.isPurgeOverride(), this.data.getPurgePeriod(), this.data.getPurgeType());
    }

    @JsonSetter("purgeSettings")
    public void setPurgeSettings(PurgeSettings settings){
        this.data.setPurgeOverride(settings.isOverride());
        this.data.setPurgePeriod(settings.getFrequency().getPeriods());
        this.data.setPurgeType(TimePeriodType.convertFrom(settings.getFrequency().getType()));
    }

    @JsonGetter("editPermission")
    public String getEditPermission(){
        return this.data.getEditPermission();
    }
    @JsonSetter("editPermission")
    public void setEditPermission(String editPermission){
        this.data.setEditPermission(editPermission);
    }

    /*
     * (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel#validate(com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult)
     */
    public void validate(RestProcessResult<?> result) throws RestValidationFailedException {
        ProcessResult validation = new ProcessResult();
        this.data.validate(validation);

        if(validation.getHasMessages()){
            result.addValidationMessages(validation);
            throw new RestValidationFailedException(this, result);
        }
    }

    @Override
    @JsonIgnore
    public T getData(){
        return this.data;
    }

    /**
     * Get the description for the data source's connection
     * @return
     */
    public TranslatableMessage getConnectionDescription() {
        return this.data.getConnectionDescription();
    }
    
    /**
     * Get the description for the type of data source
     * @return
     */
    public TranslatableMessage getDescription() {
        return new TranslatableMessage(this.data.getDefinition().getDescriptionKey());
    }

}
