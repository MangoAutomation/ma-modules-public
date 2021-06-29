/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Matthew Lohbihler
 */
public class VMStatPointLocatorRT extends PointLocatorRT<VMStatPointLocatorVO> {

    public VMStatPointLocatorRT(VMStatPointLocatorVO vo) {
    	super(vo);
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

}
