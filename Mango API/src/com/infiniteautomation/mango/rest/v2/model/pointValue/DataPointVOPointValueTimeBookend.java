/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;

import com.goebl.simplify.Point;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointVOPointValueTimeBookend implements DataPointValueTime {

    final DataPointVO vo;
    final IdPointValueTime pvt;
    final boolean firstBookend;
    final boolean lastBookend;
    final boolean cached;
    
    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt) {
        this(vo, pvt, false, false, false);
    }
    
    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt, boolean firstBookend, boolean lastBookend, boolean cached) {
        this.vo = vo;
        this.pvt = pvt;
        this.firstBookend = firstBookend;
        this.lastBookend = lastBookend;
        this.cached = cached;
    }

    /**
     * @return the vo
     */
    public DataPointVO getVo() {
        return vo;
    }
    /**
     * @return the pvt
     */
    public IdPointValueTime getPvt() {
        return pvt;
    }
    
    public long getTime() {
        return pvt.getTime();
    }
    
    public int getId() {
        return vo.getId();
    }
    
    public boolean isFirstBookend() {
        return firstBookend;
    }
    public boolean isLastBookend() {
        return lastBookend;
    }
    
    /**
     * @return the bookend
     */
    public boolean isBookend() {
        return firstBookend || lastBookend;
    }
    
    public boolean isCached() {
        return cached;
    }
    
    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getX()
     */
    @Override
    public double getX() {
        return pvt.getTime();
    }

    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getY()
     */
    @Override
    public double getY() {
        return pvt.getDoubleValue();
    }
    
    /*
     * (non-Javadoc)
     * @see com.goebl.simplify.Point#isProcessable()
     */
    @Override
    public boolean isProcessable() {
        //TODO Could check to see if we are image etc.
        return pvt.getValue() != null;
    }
    
    /* (non-Javadoc)
     * @see com.goebl.simplify.SimplifiableValue#writeValue(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter, boolean, boolean)
     */
    @Override
    public void writeEntry(PointValueTimeWriter writer, boolean useXid, boolean allowTimestamp)
            throws IOException {
        for(PointValueField field : writer.getInfo().getFields()) {
            if(!allowTimestamp && field == PointValueField.TIMESTAMP)
                continue;
            field.writeValue(this, writer.getInfo(), writer.getTranslations(), useXid, writer);
        } 
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Point that) {
        if (getX() < that.getX())
            return -1;
        if (getX() > that.getX())
            return 1;
        return 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return vo.getXid() + " - " + pvt.toString();
    }
}
