/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model;

import com.infiniteautomation.mango.rest.v2.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.internal.InternalPointLocatorVO;

/**
 *
 * @author Terry Packer
 */
public class InternalPointLocatorModel extends AbstractPointLocatorModel<InternalPointLocatorVO> {

    public static final String TYPE_NAME = "PL.INTERNAL";

    private String monitorId;

    public InternalPointLocatorModel() { }
    public InternalPointLocatorModel(InternalPointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(InternalPointLocatorVO vo) {
        super.fromVO(vo);
        this.monitorId = vo.getMonitorId();
    }

    @Override
    public InternalPointLocatorVO toVO() {
        InternalPointLocatorVO vo = new InternalPointLocatorVO();
        vo.setMonitorId(monitorId);
        return vo;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }
    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

}
