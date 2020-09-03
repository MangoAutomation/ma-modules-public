/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.emport.ImportContext;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

public class MaintenanceEventEmportDefinition extends EmportDefinition {

    public static final String ELEMENT_ID = "maintenanceEvents";
    @Override
    public String getElementId() {
        return ELEMENT_ID;
    }

    @Override
    public String getDescriptionKey() {
        return "header.maintenanceEvents";
    }

    @Override
    public Object getExportData() {
        return MaintenanceEventDao.getInstance().getAll();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext, PermissionHolder importer) throws JsonException {
        MaintenanceEventsService service = Common.getBean(MaintenanceEventsService.class);
        JsonObject maintenanceEvent = jsonValue.toJsonObject();

        String xid = maintenanceEvent.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = service.generateUniqueXid();

        MaintenanceEventVO vo = null;
        if (StringUtils.isBlank(xid)) {
            xid = service.generateUniqueXid();
        }else {
            try {
                vo = service.get(xid);
            }catch(NotFoundException e) {

            }
        }
        if(vo == null) {
            vo = new MaintenanceEventVO();
            vo.setXid(xid);
        }
        try {
            importContext.getReader().readInto(vo, maintenanceEvent);
            boolean isnew = vo.getId() == Common.NEW_ID;
            if(isnew) {
                service.insert(vo);
            }else {
                service.update(vo.getId(), vo);
            }
            importContext.addSuccessMessage(isnew, "emport.maintenanceEvent.prefix", xid);
        }catch(ValidationException e) {
            importContext.copyValidationMessages(e.getValidationResult(), "emport.maintenanceEvent.prefix", xid);
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
