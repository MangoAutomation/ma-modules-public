/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Matthew Lohbihler
 */
public class VMStatPointLocatorRT extends PointLocatorRT {
    private final VMStatPointLocatorVO vo;

    public VMStatPointLocatorRT(VMStatPointLocatorVO vo) {
        this.vo = vo;
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public VMStatPointLocatorVO getPointLocatorVO() {
        return vo;
    }
}
