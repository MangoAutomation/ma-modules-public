/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

public class GraphicalViewEmportDefinition extends EmportDefinition {
    private GraphicalViewDao graphicalViewDao;

    @Override
    public String getElementId() {
        return "graphicalViews";
    }

    @Override
    public String getDescriptionKey() {
        return "header.views";
    }

    @Override
    public Object getExportData() {
        ensureDao();
        return graphicalViewDao.getViews();
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext) throws JsonException {
        JsonObject viewJson = jsonValue.toJsonObject();

        String xid = viewJson.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = graphicalViewDao.generateUniqueXid();

        ensureDao();
        GraphicalView view = graphicalViewDao.getViewByXid(xid);
        if (view == null) {
            view = new GraphicalView();
            view.setXid(xid);
        }

        try {
            importContext.getReader().readInto(view, viewJson);

            // Now validate it. Use a new response object so we can distinguish errors in this view from other
            // errors.
            ProcessResult viewResponse = new ProcessResult();
            view.validate(viewResponse);
            if (viewResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(viewResponse, "emport.view.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = view.isNew();
                graphicalViewDao.saveView(view);
                importContext.addSuccessMessage(isnew, "emport.view.prefix", xid);
            }
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.view.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.view.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }

    private void ensureDao() {
        if (graphicalViewDao == null)
            graphicalViewDao = new GraphicalViewDao();
    }
}
