/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

public class ScheduledEventEmportDefinition extends EmportDefinition {

    @Override
    public String getElementId() {
        return "scheduledEvents";
    }

    @Override
    public String getDescriptionKey() {
        return "header.scheduledEvents";
    }

    @Override
    public Object getExportData() {
        return ScheduledEventDao.instance.getScheduledEvents();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext) throws JsonException {
        JsonObject scheduledEvent = jsonValue.toJsonObject();

        String xid = scheduledEvent.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = ScheduledEventDao.instance.generateUniqueXid();

        ScheduledEventVO vo = ScheduledEventDao.instance.getScheduledEvent(xid);
        if (vo == null) {
            vo = new ScheduledEventVO();
            vo.setXid(xid);
        }

        try {
            importContext.getReader().readInto(vo, scheduledEvent);

            // Now validate it. Use a new response object so we can distinguish errors in this vo from other errors.
            ProcessResult voResponse = new ProcessResult();
            vo.validate(voResponse);
            if (voResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(voResponse, "emport.scheduledEvent.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = vo.isNew();
                RTMDefinition.instance.saveScheduledEvent(vo);
                importContext.addSuccessMessage(isnew, "emport.scheduledEvent.prefix", xid);
            }
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.scheduledEvent.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.scheduledEvent.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}
