/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class GraphicalViewHandler implements UrlHandler {
    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        GraphicalViewDao viewDao = new GraphicalViewDao();
        User user = Common.getUser(request);

        List<IntStringPair> views = viewDao.getViewNames(user.getId());
        model.put("views", views);

        // Set the current view.
        GraphicalView currentView = null;
        String vid = request.getParameter("viewId");
        if (StringUtils.isBlank(vid)) {
            String xid = request.getParameter("xid");
            if (xid != null)
                currentView = viewDao.getViewByXid(xid);
        }
        else {
            try {
                currentView = viewDao.getView(Integer.parseInt(vid));
            }
            catch (NumberFormatException e) {
                // no op
            }
        }

        if (currentView == null && views.size() > 0)
            currentView = viewDao.getView(views.get(0).getKey());

        if (currentView != null) {
            GraphicalViewsCommon.ensureViewPermission(user, currentView);

            // Make sure the owner still has permission to all of the points in the view, and that components are
            // otherwise valid.
            currentView.validateViewComponents(false);

            // Add the view to the session for the dwr access stuff.
            model.put("currentView", currentView);
            model.put("owner", currentView.getUserAccess(user) == ShareUser.ACCESS_OWNER);

            GraphicalViewsCommon.setUserView(user, currentView);
        }

        return null;
    }
}
