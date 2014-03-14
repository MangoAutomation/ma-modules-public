/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.web.dwr.beans.BasePointState;

/**
 * This class is used by DWR to package up information needed at the browser for the display of the current state of
 * point information.
 * 
 * @author Matthew Lohbihler
 */
public class ViewComponentState extends BasePointState {
    private String content;
    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content != null)
            this.content = content.trim();
        else
            this.content = content;
    }

    @Override
    public ViewComponentState clone() {
        try {
            return (ViewComponentState) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    public void removeEqualValue(ViewComponentState that) {
        super.removeEqualValue(that);
        if (StringUtils.equals(content, that.content))
            content = null;
        if (StringUtils.equals(info, that.info))
            info = null;
    }

    @Override
    public boolean isEmpty() {
        return content == null && info == null && super.isEmpty();
    }
}
