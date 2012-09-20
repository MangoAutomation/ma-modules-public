/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.jviews.JspViewsCommon;
import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.vo.User;

/**
 * @author Matthew Lohbihler
 */
public class JspViewInitTag extends TagSupport {
    private static final long serialVersionUID = -1;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int doStartTag() throws JspException {
        // Check the user id.
        User user = new UserDao().getUser(username);
        if (user == null)
            throw new JspException("Username '" + username + "' not found");
        if (user.isDisabled())
            throw new JspException("Username '" + username + "' is disabled");

        JspViewsCommon.setJspView((HttpServletRequest) pageContext.getRequest(), new JspView(user));

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public void release() {
        super.release();
        username = null;
    }
}
