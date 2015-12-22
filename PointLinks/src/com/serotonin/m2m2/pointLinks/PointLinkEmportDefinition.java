/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

public class PointLinkEmportDefinition extends EmportDefinition {
    @Override
    public String getElementId() {
        return "pointLinks";
    }

    @Override
    public String getDescriptionKey() {
        return "header.pointLinks";
    }

    @Override
    public Object getExportData() {
        return new PointLinkDao().getPointLinks();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext) throws JsonException {
        JsonObject pointLink = jsonValue.toJsonObject();
        PointLinkDao pointLinkDao = new PointLinkDao();

        String xid = pointLink.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = pointLinkDao.generateUniqueXid();

        PointLinkVO vo = pointLinkDao.getPointLink(xid);
        if (vo == null) {
            vo = new PointLinkVO();
            vo.setXid(xid);
        }

        try {
            importContext.getReader().readInto(vo, pointLink);

            // Now validate it. Use a new response object so we can distinguish errors in this vo from other errors.
            ProcessResult voResponse = new ProcessResult();
            vo.validate(voResponse);
            if (voResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(voResponse, "emport.pointLink.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = vo.isNew();
                RTMDefinition.instance.savePointLink(vo);
                importContext.addSuccessMessage(isnew, "emport.pointLink.prefix", xid);
            }
        }

        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.pointLink.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.pointLink.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}
