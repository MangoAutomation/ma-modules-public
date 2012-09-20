/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class WatchListHandler implements UrlHandler {
    public static final String KEY_WATCHLISTS = "watchLists";
    public static final String KEY_SELECTED_WATCHLIST = "selectedWatchList";

    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        User user = Common.getUser(request);
        prepareModel(request, model, user);
        return null;
    }

    protected void prepareModel(HttpServletRequest request, Map<String, Object> model, User user) {
        // The user's permissions may have changed since the last session, so make sure the watch lists are correct.
        WatchListDao watchListDao = new WatchListDao();
        List<WatchList> watchLists = watchListDao.getWatchLists(user.getId());

        if (watchLists.size() == 0) {
            // Add a default watch list if none exist.
            WatchList watchList = new WatchList();
            watchList.setName(ControllerUtils.getTranslations(request).translate("common.newName"));
            watchLists.add(watchListDao.createNewWatchList(watchList, user.getId()));
        }

        int selected = 0;
        WatchList selectedWatchList = watchListDao.getSelectedWatchList(user.getId());
        if (selectedWatchList != null)
            selected = selectedWatchList.getId();

        // Check if a parameter was provided.
        String wlid = request.getParameter("wlid");
        if (!StringUtils.isBlank(wlid)) {
            try {
                selected = Integer.parseInt(wlid);
            }
            catch (NumberFormatException e) {
                // ignore
            }
        }

        String wlxid = request.getParameter("wlxid");

        boolean found = false;
        List<IntStringPair> watchListNames = new ArrayList<IntStringPair>(watchLists.size());
        for (WatchList watchList : watchLists) {
            if (!found) {
                if (StringUtils.equals(watchList.getXid(), wlxid)) {
                    found = true;
                    selected = watchList.getId();
                }
                else if (watchList.getId() == selected)
                    found = true;
            }

            if (watchList.getUserAccess(user) == ShareUser.ACCESS_OWNER) {
                // If this is the owner, check that the user still has access to the points. If not, remove the
                // unauthorized points, resave, and continue.
                boolean changed = false;
                List<DataPointVO> list = watchList.getPointList();
                List<DataPointVO> copy = new ArrayList<DataPointVO>(list);
                for (DataPointVO point : copy) {
                    if (point == null || !Permissions.hasDataPointReadPermission(user, point)) {
                        list.remove(point);
                        changed = true;
                    }
                }

                if (changed)
                    watchListDao.saveWatchList(watchList);
            }

            watchListNames.add(new IntStringPair(watchList.getId(), watchList.getName()));
        }

        if (!found) {
            // The user's default watch list was not found. It was either deleted or unshared from them. Find a new one.
            // The list will always contain at least one, so just use the id of the first in the list.
            selected = watchLists.get(0).getId();
            new WatchListDao().saveSelectedWatchList(user.getId(), selected);
        }

        model.put(KEY_WATCHLISTS, watchListNames);
        model.put(KEY_SELECTED_WATCHLIST, selected);
    }
}
