/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Matthew Lohbihler
 */
public class EnvCanPointLocatorRT extends PointLocatorRT<EnvCanPointLocatorVO> {

    public EnvCanPointLocatorRT(EnvCanPointLocatorVO vo) {
    	super(vo);
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

}
