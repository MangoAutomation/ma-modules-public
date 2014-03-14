/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

public class MaintenanceEventEmportDefinition extends EmportDefinition {
    @Override
    public String getElementId() {
        return "maintenanceEvents";
    }

    @Override
    public String getDescriptionKey() {
        return "header.maintenanceEvents";
    }

    @Override
    public Object getExportData() {
        return new MaintenanceEventDao().getMaintenanceEvents();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext) throws JsonException {
        MaintenanceEventDao maintenanceEventDao = new MaintenanceEventDao();
        JsonObject maintenanceEvent = jsonValue.toJsonObject();

        String xid = maintenanceEvent.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = maintenanceEventDao.generateUniqueXid();

        MaintenanceEventVO vo = maintenanceEventDao.getMaintenanceEvent(xid);
        if (vo == null) {
            vo = new MaintenanceEventVO();
            vo.setXid(xid);
        }

        try {
            importContext.getReader().readInto(vo, maintenanceEvent);

            // Now validate it. Use a new response object so we can distinguish errors in this vo from other errors.
            ProcessResult voResponse = new ProcessResult();
            vo.validate(voResponse);
            if (voResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(voResponse, "emport.maintenanceEvent.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = vo.isNew();
                RTMDefinition.instance.saveMaintenanceEvent(vo);
                importContext.addSuccessMessage(isnew, "emport.maintenanceEvent.prefix", xid);
            }
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.maintenanceEvent.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.maintenanceEvent.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}
