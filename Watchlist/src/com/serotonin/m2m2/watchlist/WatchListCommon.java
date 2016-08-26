/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;

public class WatchListCommon {
    @SuppressWarnings("unchecked")
    public static List<WatchListState> getWatchListStates(LongPollData data) {
        List<WatchListState> watchListStates = (List<WatchListState>) data.getState().getAttribute("watchListStates");
        if (watchListStates == null) {
            synchronized (data) {
                watchListStates = (List<WatchListState>) data.getState().getAttribute("watchListStates");
                if (watchListStates == null) {
                    watchListStates = new ArrayList<>();
                    data.getState().setAttribute("watchListStates", watchListStates);
                }
            }
        }
        return watchListStates;
    }

    public static void ensureWatchListPermission(User user, WatchListVO watchList) throws PermissionException {
        if (!watchList.isReader(user))
            throw new PermissionException("User does not have permission to the watch list", user);
    }

    public static void ensureWatchListEditPermission(User user, WatchListVO watchList) throws PermissionException {
        if (!watchList.isEditor(user))
            throw new PermissionException("User does not have permission to edit the watch list", user);
    }
}
