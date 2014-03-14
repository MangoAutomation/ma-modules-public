/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollState;

public class WatchListLongPollHandler implements LongPollHandler {
    private final WatchListDwr watchListDwr;
    private final String key; //Key in Long Poll Handler Map
    
    public WatchListLongPollHandler(WatchListDwr watchListDwr, String handlerKey) {
        this.watchListDwr = watchListDwr;
        this.key = handlerKey; 
    }

    @Override
    public void handleLongPoll(LongPollData data, Map<String, Object> response, User user) {
        if (data.getRequest().hasHandler(key) && user != null) {
            LongPollState state = data.getState();
            List<WatchListState> watchListStates = WatchListCommon.getWatchListStates(data);

            synchronized (state) {
                List<WatchListState> newStates = watchListDwr.getPointData();
                List<WatchListState> differentStates = new ArrayList<WatchListState>();

                for (WatchListState newState : newStates) {
                    WatchListState oldState = getWatchListState(newState.getId(), watchListStates);
                    if (oldState == null)
                        differentStates.add(newState);
                    else {
                        WatchListState copy = newState.clone();
                        copy.removeEqualValue(oldState);
                        if (!copy.isEmpty())
                            differentStates.add(copy);
                    }
                }

                if (!differentStates.isEmpty()) {
                    response.put("watchListStates", differentStates);
                    state.setAttribute("watchListStates", newStates);
                }
            }
        }
    }

    private WatchListState getWatchListState(String id, List<WatchListState> watchListStates) {
        for (WatchListState state : watchListStates) {
            if (state.getId().equals(id))
                return state;
        }
        return null;
    }
}
