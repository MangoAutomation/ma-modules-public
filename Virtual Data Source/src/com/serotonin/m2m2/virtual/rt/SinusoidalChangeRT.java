/**
 * Copyright (C) 2013 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.virtual.vo.SinusoidalChangeVO;

/**
 * @author Terry Packer
 *
 */
public class SinusoidalChangeRT extends ChangeTypeRT{
    private final SinusoidalChangeVO vo;
    private double time; //Time tracking

    public SinusoidalChangeRT(SinusoidalChangeVO vo) {
        this.vo = vo;
        this.time = vo.getOffset();
    }

    @Override
    public DataValue change(DataValue currentValue) {
    	
    	//This Equation needs work to make sense with the inputs
    	double angularFreq = 2d * Math.PI * (1d/vo.getPeriod());
        double newValue = vo.getOffset() + vo.getAmplitude()*Math.sin(angularFreq*time + vo.getPhaseShift());
        time = time + 1d;
        return new NumericValue(newValue);
    }
}
