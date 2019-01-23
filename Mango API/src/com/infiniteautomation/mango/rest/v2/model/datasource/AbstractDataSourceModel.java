/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.permission.Permissions;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=AbstractDataSourceModel.MODEL_TYPE)
public abstract class AbstractDataSourceModel<T extends DataSourceVO<T>> extends AbstractVoModel<T> {

    public static final String MODEL_TYPE = "modelType";

    @JsonIgnore
    protected DataSourceDefinition definition;
    
    @ApiModelProperty("Read only description of data source connection")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage connectionDiscription;
    
    @ApiModelProperty("Read only description of data source type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage description;
    
    private boolean enabled;
    private List<EventTypeAlarmLevelModel> eventAlarmLevels;
    private PurgeSettings purgeSettings;
    private Set<String> editPermission;

    public AbstractDataSourceModel() {
        super();
    }
    
    public AbstractDataSourceModel(T vo) {
        super(vo);
    }
    
    /**
     * Return the TYPE_NAME from the Data Source definition
     * @return
     */
    public abstract String getModelType();
    
    
    @SuppressWarnings("unchecked")
    @Override
    protected T newVO() {
        DataSourceDefinition def = getDefinition();
        T vo = (T) def.baseCreateDataSourceVO();
        vo.setDefinition(def);
        return vo;
    }
    
    @JsonIgnore
    public DataSourceDefinition getDefinition() {
        DataSourceDefinition definition = ModuleRegistry.getDataSourceDefinition(getModelType());
        if(definition == null)
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("rest.exception.modelNotFound", getModelType()));
        return definition;
    }
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.definition = vo.getDefinition();
        this.connectionDiscription = vo.getConnectionDescription();
        this.description = new TranslatableMessage(vo.getDefinition().getDescriptionKey());
        
        this.enabled = vo.isEnabled();
        this.eventAlarmLevels = new ArrayList<>();
        for(EventTypeVO evt : vo.getEventTypes()) {
            DataSourceEventType dsEvt = (DataSourceEventType)evt.getEventType();
            EventTypeAlarmLevelModel model = new EventTypeAlarmLevelModel(
                    vo.getXid(),
                    dsEvt.getEventSubtype(),
                    dsEvt.getDuplicateHandling(),
                    dsEvt.getAlarmLevel(),
                    evt.getDescription()
                    );
            this.eventAlarmLevels.add(model);
        }
        
        this.purgeSettings = new PurgeSettings(vo);
        this.editPermission = Permissions.explodePermissionGroups(vo.getEditPermission());
    }
    
    @Override
    public T toVO() {
        T vo = super.toVO();
        vo.setEnabled(enabled);
        if(eventAlarmLevels != null) {
            for(EventTypeAlarmLevelModel eval : eventAlarmLevels) {
                vo.setAlarmLevel(eval.getEventType(), eval.getLevel());
            }
        }
        
        if(purgeSettings != null)
            purgeSettings.toVO(vo);
        vo.setEditPermission(Permissions.implodePermissionGroups(editPermission));
        return vo;
    }
    
    /**
     * Get the description for the data source's connection
     * @return
     */
    public TranslatableMessage getConnectionDescription() {
        return connectionDiscription;
    }
    
    /**
     * Get the description for the type of data source
     * @return
     */
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * @return the connectionDiscription
     */
    public TranslatableMessage getConnectionDiscription() {
        return connectionDiscription;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the alarmLevels
     */
    public List<EventTypeAlarmLevelModel> getEventAlarmLevels() {
        return eventAlarmLevels;
    }

    /**
     * @param alarmLevels the alarmLevels to set
     */
    public void setEventAlarmLevels(List<EventTypeAlarmLevelModel> alarmLevels) {
        this.eventAlarmLevels = alarmLevels;
    }

    /**
     * @return the purgeSettings
     */
    public PurgeSettings getPurgeSettings() {
        return purgeSettings;
    }

    /**
     * @param purgeSettings the purgeSettings to set
     */
    public void setPurgeSettings(PurgeSettings purgeSettings) {
        this.purgeSettings = purgeSettings;
    }

    /**
     * @return the editPermission
     */
    public Set<String> getEditPermission() {
        return editPermission;
    }

    /**
     * @param editPermission the editPermission to set
     */
    public void setEditPermission(Set<String> editPermission) {
        this.editPermission = editPermission;
    }
    
}
