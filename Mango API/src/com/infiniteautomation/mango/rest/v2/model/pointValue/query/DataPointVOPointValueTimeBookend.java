/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointVOPointValueTimeBookend {

    final DataPointVO vo;
    final boolean bookend;
    final IdPointValueTime pvt;
    
    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt) {
        this(vo, pvt, false);
    }
    
    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt, boolean bookend) {
        this.vo = vo;
        this.pvt = pvt;
        this.bookend = bookend;
    }

    /**
     * @return the vo
     */
    public DataPointVO getVo() {
        return vo;
    }

    /**
     * @return the bookend
     */
    public boolean isBookend() {
        return bookend;
    }

    /**
     * @return the pvt
     */
    public IdPointValueTime getPvt() {
        return pvt;
    }
}
