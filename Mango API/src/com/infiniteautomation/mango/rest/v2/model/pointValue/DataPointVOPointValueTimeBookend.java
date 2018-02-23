/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointVOPointValueTimeBookend {

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
}
