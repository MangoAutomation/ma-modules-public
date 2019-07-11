/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointStatisticsGenerator {

    private final DataPointVO vo;
    private StatisticsGenerator generator;

    public DataPointStatisticsGenerator(DataPointVO vo) {
        this.vo = vo;
    }

    public DataPointVO getVo() {
        return vo;
    }

    public StatisticsGenerator getGenerator() {
        return generator;
    }
    
    public void setGenerator(StatisticsGenerator generator) {
        this.generator = generator;
    }
}
