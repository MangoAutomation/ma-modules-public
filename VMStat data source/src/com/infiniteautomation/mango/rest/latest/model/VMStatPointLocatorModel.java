/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.vmstat.VMStatPointLocatorVO;

/**
 *
 * @author Terry Packer
 */
public class VMStatPointLocatorModel extends AbstractPointLocatorModel<VMStatPointLocatorVO> {

    public static final String TYPE_NAME = "PL.VMSTAT";

    private String attribute;

    public VMStatPointLocatorModel() {

    }

    public VMStatPointLocatorModel(VMStatPointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public VMStatPointLocatorVO toVO() {
        VMStatPointLocatorVO vo = new VMStatPointLocatorVO();
        vo.setAttributeId(VMStatPointLocatorVO.ATTRIBUTE_CODES.getId(attribute));
        return vo;
    }

    @Override
    public void fromVO(VMStatPointLocatorVO locator) {
        super.fromVO(locator);
        this.attribute = VMStatPointLocatorVO.ATTRIBUTE_CODES.getCode(locator.getAttributeId());
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }

}
