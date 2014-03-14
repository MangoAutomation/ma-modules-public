/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.serotonin.ShouldNeverHappenException;

/**
 * @author Matthew Lohbihler
 */
public class JspComponentState implements Cloneable {
    private int id;
    private String value;
    private Long time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public JspComponentState clone() {
        try {
            return (JspComponentState) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    public void removeEqualValue(JspComponentState that) {
        if (StringUtils.equals(value, that.value))
            value = null;
        if (ObjectUtils.equals(time, that.time))
            time = null;
    }

    public boolean isEmpty() {
        return value == null && time == null;
    }
}
