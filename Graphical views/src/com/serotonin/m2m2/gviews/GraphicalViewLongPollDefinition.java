/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.module.LongPollDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollState;

public class GraphicalViewLongPollDefinition extends LongPollDefinition implements LongPollHandler {
    private GraphicalViewDwr graphicalViewDwr;

    @Override
    public void preInitialize(boolean install, boolean upgrade) {
        graphicalViewDwr = new GraphicalViewDwr();
        graphicalViewDwr.setModule(getModule());
    }

    @Override
    public LongPollHandler getHandler() {
        return this;
    }

    @Override
    public void handleLongPoll(LongPollData data, Map<String, Object> response, User user) {
        boolean view = false;
        boolean viewEdit = false;
        boolean anon = false;
        if (data.getRequest().hasHandler("graphicalView") && user != null)
            view = true;
        else if (data.getRequest().hasHandler("graphicalViewEdit") && user != null)
            viewEdit = true;
        else if (data.getRequest().hasHandler("graphicalViewAnon"))
            anon = true;

        if (view || viewEdit || anon) {
            LongPollState state = data.getState();
            List<ViewComponentState> graphicalViewStates = GraphicalViewsCommon.getGraphicalViewListStates(data);

            List<ViewComponentState> newStates;

            synchronized (state) {
                if (anon)
                    newStates = graphicalViewDwr.getViewPointDataAnon(data.getRequest().getRefId());
                else
                    newStates = graphicalViewDwr.getViewPointData(viewEdit);

                List<ViewComponentState> differentStates = new ArrayList<ViewComponentState>();
                
                for (ViewComponentState newState : newStates) {
                    ViewComponentState oldState = getGraphicalViewState(newState.getId(), graphicalViewStates);
                    if (oldState == null)
                        differentStates.add(newState);
                    else {
                        ViewComponentState copy = newState.clone();
                        copy.removeEqualValue(oldState);
                        if (!copy.isEmpty())
                            differentStates.add(copy);
                    }
                }

                if (!differentStates.isEmpty()) {
                    response.put("viewStates", differentStates);
                    GraphicalViewsCommon.setGraphicalViewListStates(data, newStates);
                }
            }
        }
    }

    private ViewComponentState getGraphicalViewState(String id, List<ViewComponentState> graphicalViewStates) {
        for (ViewComponentState state : graphicalViewStates) {
            if (state.getId().equals(id))
                return state;
        }
        return null;
    }
}
