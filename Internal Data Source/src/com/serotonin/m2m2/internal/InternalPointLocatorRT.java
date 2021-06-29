/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Matthew Lohbihler
 */
public class InternalPointLocatorRT extends PointLocatorRT<InternalPointLocatorVO> {

    public InternalPointLocatorRT(InternalPointLocatorVO vo) {
    	super(vo);
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public InternalPointLocatorVO getPointLocatorVO() {
        return vo;
    }
}
