/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.publisher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.EventTypeAlarmLevelModel;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriodType;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PublisherDefinition;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractPublisherModel.MODEL_TYPE)
public abstract class AbstractPublisherModel<POINT extends PublishedPointVO, PUBLISHER extends PublisherVO> extends AbstractVoModel<PUBLISHER> {

    public static final String MODEL_TYPE = "modelType";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPublisherModel.class);

    @JsonIgnore
    protected PublisherDefinition<?> definition;

    @ApiModelProperty("Read only description of publisher connection")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected TranslatableMessage connectionDescription;

    @ApiModelProperty("Read only description of publisher type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected TranslatableMessage description;

    protected boolean enabled;
    protected List<EventTypeAlarmLevelModel> eventAlarmLevels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected List<AbstractPublishedPointModel<POINT>> points = new ArrayList<>();
    protected String publishType;
    protected int cacheWarningSize;
    protected int cacheDiscardSize;
    protected boolean sendSnapshot;
    protected TimePeriod snapshotSendPeriod;
    protected boolean publishAttributeChanges;


    /**
     * Return the TYPE_NAME from the Publisher Source definition
     */
    public abstract String getModelType();

    @SuppressWarnings("unchecked")
    @Override
    protected PUBLISHER newVO() {
        PublisherDefinition<?> def = getDefinition();
        PUBLISHER vo = (PUBLISHER) def.baseCreatePublisherVO();
        vo.setDefinition(def);
        return vo;
    }

    @JsonIgnore
    public PublisherDefinition<?> getDefinition() {
        PublisherDefinition<?> definition = ModuleRegistry.getPublisherDefinition(getModelType());
        if(definition == null)
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("rest.exception.modelNotFound", getModelType()));
        return definition;
    }

    @Override
    public void fromVO(PUBLISHER vo) {
        super.fromVO(vo);
        this.connectionDescription = vo.getConfigDescription();
        this.description = new TranslatableMessage(vo.getDefinition().getDescriptionKey());

        this.enabled = vo.isEnabled();
        this.eventAlarmLevels = new ArrayList<>();
        ExportCodes eventCodes = vo.getEventCodes();

        for(EventTypeVO evt : vo.getEventTypes()) {
            PublisherEventType dsEvt = (PublisherEventType)evt.getEventType();
            EventTypeAlarmLevelModel model = new EventTypeAlarmLevelModel(
                    eventCodes.getCode(dsEvt.getReferenceId2()),
                    dsEvt.getDuplicateHandling(),
                    evt.getAlarmLevel(),
                    evt.getDescription()
                    );
            this.eventAlarmLevels.add(model);
        }

        this.publishType = PublisherVO.PUBLISH_TYPE_CODES.getCode(vo.getPublishType());
        this.cacheWarningSize = vo.getCacheWarningSize();
        this.cacheDiscardSize = vo.getCacheDiscardSize();
        this.sendSnapshot = vo.isSendSnapshot();
        this.snapshotSendPeriod = new TimePeriod(vo.getSnapshotSendPeriods(), TimePeriodType.convertTo(vo.getSnapshotSendPeriodType()));
        this.publishAttributeChanges = vo.isPublishAttributeChanges();
    }

    @Override
    public PUBLISHER toVO() {
        PUBLISHER vo = super.toVO();
        vo.setEnabled(enabled);

        if(eventAlarmLevels != null) {
            for(EventTypeAlarmLevelModel eval : eventAlarmLevels) {
                vo.setAlarmLevel(eval.getEventType(), eval.getLevel());
            }
        }

        vo.setPublishType(PublisherVO.PUBLISH_TYPE_CODES.getId(publishType));
        vo.setCacheWarningSize(cacheWarningSize);
        vo.setCacheDiscardSize(cacheDiscardSize);
        vo.setSendSnapshot(sendSnapshot);
        if(snapshotSendPeriod != null) {
            vo.setSnapshotSendPeriods(snapshotSendPeriod.getPeriods());
            vo.setSnapshotSendPeriodType(TimePeriodType.convertFrom(snapshotSendPeriod.getType()));
        }
        vo.setPublishAttributeChanges(publishAttributeChanges);
        return vo;
    }


    /**
     * Get the description for the publisher's connection
     */
    public TranslatableMessage getConnectionDescription() {
        return connectionDescription;
    }

    /**
     * Get the description for the type of publisher
     */
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * Get the description translation key for the type of publisher
     */
    public String getDescriptionKey() {
        return description.getKey();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<EventTypeAlarmLevelModel> getEventAlarmLevels() {
        return eventAlarmLevels;
    }

    public void setEventAlarmLevels(List<EventTypeAlarmLevelModel> alarmLevels) {
        this.eventAlarmLevels = alarmLevels;
    }

    public String getPublishType() {
        return publishType;
    }

    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }

    public int getCacheWarningSize() {
        return cacheWarningSize;
    }

    public void setCacheWarningSize(int cacheWarningSize) {
        this.cacheWarningSize = cacheWarningSize;
    }

    public int getCacheDiscardSize() {
        return cacheDiscardSize;
    }

    public void setCacheDiscardSize(int cacheDiscardSize) {
        this.cacheDiscardSize = cacheDiscardSize;
    }

    public boolean isSendSnapshot() {
        return sendSnapshot;
    }

    public void setSendSnapshot(boolean sendSnapshot) {
        this.sendSnapshot = sendSnapshot;
    }

    public TimePeriod getSnapshotSendPeriod() {
        return snapshotSendPeriod;
    }

    public void setSnapshotSendPeriod(TimePeriod snapshotSendPeriod) {
        this.snapshotSendPeriod = snapshotSendPeriod;
    }

    public boolean isPublishAttributeChanges() {
        return publishAttributeChanges;
    }

    public void setPublishAttributeChanges(boolean publishAttributeChanges) {
        this.publishAttributeChanges = publishAttributeChanges;
    }

    public void setConnectionDescription(TranslatableMessage connectionDescription) {
        this.connectionDescription = connectionDescription;
    }

    public List<AbstractPublishedPointModel<POINT>> getPoints() {
        return points;
    }

    public void setPoints(List<AbstractPublishedPointModel<POINT>> points) {
        this.points = points;
    }

}
