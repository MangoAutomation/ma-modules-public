/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class PublicViewHandler implements UrlHandler {
    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        GraphicalViewDao graphicalViewDao = new GraphicalViewDao();

        // Get the view by id.
        String vid = request.getParameter("viewId");
        GraphicalView view = null;
        if (vid != null) {
            try {
                view = graphicalViewDao.getView(Integer.parseInt(vid));
            }
            catch (NumberFormatException e) { /* no op */
            }
        }
        else {
            String name = request.getParameter("viewName");
            if (name != null)
                view = graphicalViewDao.getView(name);
            else {
                String xid = request.getParameter("viewXid");
                if (xid != null)
                    view = graphicalViewDao.getViewByXid(xid);
            }
        }

        // Ensure the view has anonymously accessible.
        if (view != null && view.getAnonymousAccess() == ShareUser.ACCESS_NONE)
            view = null;

        if (view != null) {
            model.put("view", view);
            view.validateViewComponents(view.getAnonymousAccess() == ShareUser.ACCESS_READ);
            GraphicalViewsCommon.addAnonymousView(request, view);
        }

        return null;
    }
}
