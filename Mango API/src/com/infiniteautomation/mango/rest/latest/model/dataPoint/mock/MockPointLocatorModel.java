/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.dataPoint.mock;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.vo.dataPoint.MockPointLocatorVO;

/**
 *
 * @author Terry Packer
 */
public class MockPointLocatorModel extends AbstractPointLocatorModel<MockPointLocatorVO> {

    public static final String TYPE_NAME = "PL.MOCK";

    public MockPointLocatorModel() { }

    public MockPointLocatorModel(MockPointLocatorVO vo) {
        super(vo);
    }

    @Override
    public MockPointLocatorVO toVO() {
        MockPointLocatorVO vo = new MockPointLocatorVO();
        vo.setDataTypeId(DataTypes.CODES.getId(dataType));
        vo.setSettable(settable);
        return vo;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }

}
