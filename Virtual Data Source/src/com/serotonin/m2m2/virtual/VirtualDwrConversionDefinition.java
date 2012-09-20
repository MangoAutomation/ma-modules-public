/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual;

import com.serotonin.m2m2.module.DwrConversionDefinition;
import com.serotonin.m2m2.virtual.vo.AlternateBooleanChangeVO;
import com.serotonin.m2m2.virtual.vo.AnalogAttractorChangeVO;
import com.serotonin.m2m2.virtual.vo.BrownianChangeVO;
import com.serotonin.m2m2.virtual.vo.IncrementAnalogChangeVO;
import com.serotonin.m2m2.virtual.vo.IncrementMultistateChangeVO;
import com.serotonin.m2m2.virtual.vo.NoChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomAnalogChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomBooleanChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomMultistateChangeVO;

public class VirtualDwrConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(AlternateBooleanChangeVO.class);
        addConversion(AnalogAttractorChangeVO.class);
        addConversion(BrownianChangeVO.class);
        addConversion(IncrementAnalogChangeVO.class);
        addConversion(IncrementMultistateChangeVO.class);
        addConversion(NoChangeVO.class);
        addConversion(RandomAnalogChangeVO.class);
        addConversion(RandomBooleanChangeVO.class);
        addConversion(RandomMultistateChangeVO.class);
    }
}
