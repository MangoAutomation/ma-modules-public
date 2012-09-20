/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;

public class JspViewsCommon {
    private static final String VIEW_KEY = JspViewsCommon.class + ".view";
    private static final String STATES_KEY = JspViewsCommon.class + ".states";

    @SuppressWarnings("unchecked")
    public static List<JspComponentState> getJspViewListStates(LongPollData data) {
        List<JspComponentState> states = (List<JspComponentState>) data.getState().getAttribute(STATES_KEY);
        if (states == null) {
            synchronized (data) {
                states = (List<JspComponentState>) data.getState().getAttribute(STATES_KEY);
                if (states == null) {
                    states = new ArrayList<JspComponentState>();
                    data.getState().setAttribute(STATES_KEY, states);
                }
            }
        }
        return states;
    }

    public static void setJspViewListStates(LongPollData data, List<JspComponentState> states) {
        data.getState().setAttribute(STATES_KEY, states);
    }

    public static JspView getJspView(HttpServletRequest request) {
        return (JspView) request.getSession().getAttribute(VIEW_KEY);
    }

    public static void setJspView(HttpServletRequest request, JspView view) {
        request.getSession().setAttribute(VIEW_KEY, view);
    }
}
