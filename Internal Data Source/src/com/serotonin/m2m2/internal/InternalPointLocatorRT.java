/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Matthew Lohbihler
 */
public class InternalPointLocatorRT extends PointLocatorRT {
    private final InternalPointLocatorVO vo;

    public InternalPointLocatorRT(InternalPointLocatorVO vo) {
        this.vo = vo;
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public InternalPointLocatorVO getPointLocatorVO() {
        return vo;
    }
}
