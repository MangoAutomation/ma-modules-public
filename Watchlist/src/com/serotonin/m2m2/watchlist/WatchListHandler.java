/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
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
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class WatchListHandler implements UrlHandler {
    public static final String KEY_WATCHLISTS = "watchLists";
    public static final String KEY_SELECTED_WATCHLIST = "selectedWatchList";
    public static final String KEY_WATCHLIST_USERS = "watchListUsers";
    public static final String KEY_USER_WATCHLISTS = "userWatchLists";
    public static final String KEY_USERNAME = "username";

    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        User user = Common.getUser(request);
        prepareModel(request, model, user);
        return null;
    }

    protected void prepareModel(HttpServletRequest request, Map<String, Object> model, User user) {
        // The user's permissions may have changed since the last session, so make sure the watch lists are correct.
        WatchListDao watchListDao = new WatchListDao();
        List<WatchList> watchLists = watchListDao.getWatchLists(user);

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

        UserDao userDao = new UserDao();
        boolean found = false;
        List<IntStringPair> watchListNames = new ArrayList<>(watchLists.size());
        List<IntStringPair> watchListUsers = new ArrayList<>(watchLists.size());
        List<IntStringPair> userWatchLists = new ArrayList<>(watchLists.size());
        for (WatchList watchList : watchLists) {
            if (!found) {
                if (StringUtils.equals(watchList.getXid(), wlxid)) {
                    found = true;
                    selected = watchList.getId();
                }
                else if (watchList.getId() == selected)
                    found = true;
            }

            if (watchList.isOwner(user)) {
                // If this is the owner, check that the user still has access to the points. If not, remove the
                // unauthorized points, resave, and continue.
                boolean changed = false;
                List<DataPointVO> list = watchList.getPointList();
                List<DataPointVO> copy = new ArrayList<>(list);
                for (DataPointVO point : copy) {
                    if (point == null || !Permissions.hasDataPointReadPermission(user, point)) {
                        list.remove(point);
                        changed = true;
                    }
                }

                if (changed)
                    watchListDao.saveWatchList(watchList);
            }

            User watchListUser = userDao.getUser(watchList.getUserId());
            String username;
            if (watchListUser == null) {
                username = Common.translate("watchlist.userDNE");
            }
            else {
                username = watchListUser.getUsername();
            }

            watchListUsers.add(new IntStringPair(watchList.getId(), username));
            // Add the Username to the name to know who's it is
            userWatchLists.add(new IntStringPair(watchList.getId(), watchList.getName() + " (" + username + ")"));
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
        model.put(KEY_WATCHLIST_USERS, watchListUsers);
        model.put(KEY_USER_WATCHLISTS, userWatchLists);
        model.put(KEY_USERNAME, user.getUsername());
    }
}
