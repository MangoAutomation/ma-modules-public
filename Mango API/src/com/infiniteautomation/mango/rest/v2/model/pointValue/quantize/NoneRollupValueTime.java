/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class NoneRollupValueTime extends AbstractRollupValueTime {

    private final DataPointVO vo;
    private final IdPointValueTime value;
    
    /**
     * @param value
     */
    public NoneRollupValueTime(DataPointVO vo, IdPointValueTime value) {
        this.vo = vo;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getX()
     */
    @Override
    public double getX() {
        return value.getTime();
    }

    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getY()
     */
    @Override
    public double getY() {
        return value.getDoubleValue();
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DefaultRollupValueTime#writePointValueTime(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void writePointValueTime(PointValueTimeWriter writer) throws IOException {
        writer.writePointValueTime(new DataPointVOPointValueTimeBookend(vo, value));
    }

}
