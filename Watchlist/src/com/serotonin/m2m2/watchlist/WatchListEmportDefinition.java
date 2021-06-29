/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.watchlist;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.emport.ImportContext;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.dao.WatchListDao;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

public class WatchListEmportDefinition extends EmportDefinition {

    public static final String elementId = "watchLists";

    @Autowired
    private WatchListService service;
    @Autowired
    private WatchListDao dao;

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public String getDescriptionKey() {
        return "header.watchLists";
    }

    @Override
    public Object getExportData() {
        return dao.getAll();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext, PermissionHolder importer) throws JsonException {
        JsonObject watchListJson = jsonValue.toJsonObject();

        String xid = watchListJson.getString("xid");
        WatchListVO vo = null;
        if (StringUtils.isBlank(xid)) {
            xid = service.generateUniqueXid();
        }else {
            try {
                vo = service.get(xid);
            }catch(NotFoundException e) {

            }
        }

        if(vo == null) {
            vo = new WatchListVO();
            vo.setXid(xid);
        }

        try {
            importContext.getReader().readInto(vo, watchListJson);

            //Ensure we have a default permission since null is valid in Mango 3.x
            if(vo.getReadPermission() == null) {
                vo.setReadPermission(new MangoPermission());
            }
            if(vo.getEditPermission() == null) {
                vo.setEditPermission(new MangoPermission());
            }

            boolean isnew = vo.getId() == Common.NEW_ID;
            if(isnew) {
                service.insert(vo);
            }else {
                service.update(vo.getId(), vo);
            }
            importContext.addSuccessMessage(isnew, "emport.watchList.prefix", xid);
        }catch(ValidationException e) {
            importContext.copyValidationMessages(e.getValidationResult(), "emport.watchList.prefix", xid);
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.watchList.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.watchList.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}
