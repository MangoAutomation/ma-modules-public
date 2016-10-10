/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.gviews.GraphicalView;
import com.serotonin.m2m2.gviews.GraphicalViewDao;
import com.serotonin.m2m2.gviews.GraphicalViewsCommon;
import com.serotonin.m2m2.gviews.component.ViewComponent;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class GraphicalViewEditHandler implements UrlHandler {
    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model)
            throws Exception {
        GraphicalView view;
        User user = Common.getUser(request);

        // Fresh hit. Get the id.
        String viewIdStr = request.getParameter("viewId");
        if (viewIdStr != null) {
            // An existing view.
            view = new GraphicalViewDao().getView(Integer.parseInt(viewIdStr));
            if (view == null)
                // Doesn't exist. Redirect back to the views page.
                return new RedirectView("/views.shtm");

            GraphicalViewsCommon.ensureViewEditPermission(user, view);

            if ("true".equals(request.getParameter("copy"))) {
                // Make a copy
                GraphicalView copy = new GraphicalView();
                copy.setId(Common.NEW_ID);
                copy.setUserId(user.getId());
                copy.setXid(new GraphicalViewDao().generateUniqueXid());
                copy.setName(StringUtils.abbreviate(TranslatableMessage.translate(
                        ControllerUtils.getTranslations(request), "common.copyPrefix", view.getName()), 100));
                copy.setBackgroundFilename(GraphicalViewsCommon.copyImage(view.getBackgroundFilename()));
                for (ViewComponent vc : view.getViewComponents())
                    copy.addViewComponent(vc);

                view = copy;
            }
        }
        else {
            // A new view.
        	GraphicalViewsCommon.ensureCanCreate(user);
            view = new GraphicalView();
            view.setId(Common.NEW_ID);
            view.setUserId(user.getId());
            view.setXid(new GraphicalViewDao().generateUniqueXid());
        }

        GraphicalViewsCommon.setUserEditView(user, view);
        view.validateViewComponents(false);

        model.put("imageSets", Common.imageSets);
        model.put("dynamicImages", Common.dynamicImages);
        model.put("view", view);

        return null;
    }
}
