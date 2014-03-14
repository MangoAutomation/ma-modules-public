/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.component;

import javax.servlet.http.HttpServletRequest;

import com.serotonin.m2m2.jviews.JspComponentState;
import com.serotonin.m2m2.rt.RuntimeManager;

/**
 * @author Matthew Lohbihler
 */
abstract public class JspViewComponent {
    private final int id;

    public JspViewComponent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public JspComponentState createState(RuntimeManager rtm, HttpServletRequest request) {
        JspComponentState state = new JspComponentState();
        state.setId(id);
        createStateImpl(rtm, request, state);
        return state;
    }

    abstract protected void createStateImpl(RuntimeManager rtm, HttpServletRequest request, JspComponentState state);
}
