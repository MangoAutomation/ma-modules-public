/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model;

import com.infiniteautomation.mango.rest.v2.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.envcan.EnvCanPointLocatorVO;

/**
 *
 * @author Terry Packer
 */
public class EnvCanPointLocatorModel extends AbstractPointLocatorModel<EnvCanPointLocatorVO> {

    public static final String TYPE_NAME = "PL.ENV_CAN";

    private String attribute;

    public EnvCanPointLocatorModel() { }
    public EnvCanPointLocatorModel(EnvCanPointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(EnvCanPointLocatorVO vo) {
        super.fromVO(vo);
        this.attribute = EnvCanPointLocatorVO.ATTRIBUTE_CODES.getCode(vo.getAttributeId());
    }

    @Override
    public EnvCanPointLocatorVO toVO() {
        EnvCanPointLocatorVO vo = new EnvCanPointLocatorVO();
        vo.setAttributeId(EnvCanPointLocatorVO.ATTRIBUTE_CODES.getId(attribute));
        return vo;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }
    public String getAttribute() {
        return attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

}
