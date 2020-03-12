/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.role.Role;

import io.swagger.annotations.ApiModelProperty;

/**
 * Class for to subset the settings for a data source for read only access
 *
 * @author Terry Packer
 *
 */
public class ReadOnlyDataSourceModel extends AbstractDataSourceModel<DataSourceVO> {

    public static final String MODEL_TYPE = "modelType";

    @ApiModelProperty("Read only description of data source connection")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage connectionDescription;

    @ApiModelProperty("Read only description of data source type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslatableMessage description;

    private boolean enabled;
    private List<EventTypeAlarmLevelModel> eventAlarmLevels;
    private PurgeSettings purgeSettings;
    private Set<String> editPermission;
    private Set<String> readPermission;

    private String modelType;

    public ReadOnlyDataSourceModel() {

    }

    public ReadOnlyDataSourceModel(DataSourceVO vo) {
        fromVO(vo);
    }

    /**
     * Return the TYPE_NAME from the Data Source definition
     * @return
     */
    @Override
    public String getModelType() {
        return modelType;
    }

    @Override
    protected DataSourceVO newVO() {
        DataSourceDefinition<?> def = getDefinition();
        DataSourceVO vo = def.baseCreateDataSourceVO();
        vo.setDefinition(def);
        return vo;
    }

    @Override
    @JsonIgnore
    public DataSourceDefinition<DataSourceVO> getDefinition() {
        DataSourceDefinition<DataSourceVO> definition = ModuleRegistry.getDataSourceDefinition(getModelType());
        if(definition == null)
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("rest.exception.modelNotFound", getModelType()));
        return definition;
    }

    @Override
    public void fromVO(DataSourceVO vo) {
        super.fromVO(vo);
        this.connectionDescription = vo.getConnectionDescription();
        this.description = new TranslatableMessage(vo.getDefinition().getDescriptionKey());

        this.enabled = vo.isEnabled();
        this.eventAlarmLevels = new ArrayList<>();
        ExportCodes eventCodes = vo.getEventCodes();

        for(EventTypeVO evt : vo.getEventTypes()) {
            DataSourceEventType dsEvt = (DataSourceEventType)evt.getEventType();
            EventTypeAlarmLevelModel model = new EventTypeAlarmLevelModel(
                    eventCodes.getCode(dsEvt.getReferenceId2()),
                    dsEvt.getDuplicateHandling(),
                    dsEvt.getAlarmLevel(),
                    evt.getDescription()
                    );
            this.eventAlarmLevels.add(model);
        }

        this.purgeSettings = new PurgeSettings(vo);
        this.editPermission = new HashSet<>();
        for(Role role : vo.getEditRoles()) {
            this.editPermission.add(role.getXid());
        }
        this.readPermission = new HashSet<>();
        for(Role role : vo.getReadRoles()) {
            this.readPermission.add(role.getXid());
        }

        this.modelType = vo.getDefinition().getDataSourceTypeName();
    }

    @Override
    public DataSourceVO toVO() {
        DataSourceVO vo = super.toVO();
        vo.setEnabled(enabled);
        if(eventAlarmLevels != null) {
            for(EventTypeAlarmLevelModel eval : eventAlarmLevels) {
                vo.setAlarmLevel(eval.getEventType(), eval.getLevel());
            }
        }

        if(purgeSettings != null)
            purgeSettings.toVO(vo);
        PermissionService service = Common.getBean(PermissionService.class);
        vo.setEditRoles(service.explodeLegacyPermissionGroupsToRoles(editPermission));
        vo.setReadRoles(service.explodeLegacyPermissionGroupsToRoles(readPermission));
        return vo;
    }

    /**
     * Get the description for the data source's connection
     * @return
     */
    @Override
    public TranslatableMessage getConnectionDescription() {
        return connectionDescription;
    }

    /**
     * Get the description for the type of data source
     * @return
     */
    @Override
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * Get the description translation key for the type of data source
     * @return
     */
    @Override
    public String getDescriptionKey() {
        return description.getKey();
    }

    /**
     * @return the enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the alarmLevels
     */
    @Override
    public List<EventTypeAlarmLevelModel> getEventAlarmLevels() {
        return eventAlarmLevels;
    }

    /**
     * @return the purgeSettings
     */
    @Override
    public PurgeSettings getPurgeSettings() {
        return purgeSettings;
    }


    /**
     * @return the editPermission
     */
    @Override
    public Set<String> getEditPermission() {
        return editPermission;
    }

    @Override
    public Set<String> getReadPermission() {
        return readPermission;
    }

}
