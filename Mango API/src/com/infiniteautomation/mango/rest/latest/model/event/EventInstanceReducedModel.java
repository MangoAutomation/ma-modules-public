/*
 * Copyright (C) 2023 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.vo.event.EventInstanceI;

/**
 * @author Mert Cingöz
 */
public class EventInstanceReducedModel extends BaseEventInstanceModel {
    private Map<String, String> dataPointTags;

    public EventInstanceReducedModel() { }

    public EventInstanceReducedModel(EventInstanceI event, AbstractEventTypeModel<?, ?, ?> eventType) {
        super(event);

        if (eventType instanceof DataPointEventTypeModel) {
            DataPointModel dp = (DataPointModel) eventType.getReference1();
            Map<String, String> dpTags = dp.getTags();
            Map<String, String> tagsToUse = getTagsToExport(dpTags);
            if (!tagsToUse.isEmpty()) {
                this.dataPointTags = tagsToUse;
            }
        }
    }

    public Map<String, String> getDataPointTags() {
        return dataPointTags;
    }

    public void setDataPointTags(Map<String, String> dataPointTags) {
        this.dataPointTags = dataPointTags;
    }

    private Map<String, String> getTagsToExport(Map<String, String> dpTags) {
        Map<String, String> tagsToUse = new HashMap<>();
        String toExport = SystemSettingsDao.getInstance().getValue(SystemSettingsDao.EVENTS_EXPORT_TAGS);

        if (!StringUtils.isEmpty(toExport)) {
            String[] exportTags = Common.COMMA_SPLITTER.split(toExport);

            for (String tag : exportTags) {
                if (!tag.isEmpty()) {
                    String value = dpTags.get(tag);
                    if (value != null) {
                        tagsToUse.put(tag, value);
                    }
                }
            }
        }

        return tagsToUse;
    }
}
