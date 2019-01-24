/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.VMStatDataSourceModel;
import com.serotonin.m2m2.vmstat.VMStatDataSourceVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class VMStatDataSourceModelMapping implements RestModelMapping<VMStatDataSourceVO, VMStatDataSourceModel> {

    @Override
    public Class<? extends VMStatDataSourceVO> fromClass() {
        return VMStatDataSourceVO.class;
    }

    @Override
    public Class<? extends VMStatDataSourceModel> toClass() {
        return VMStatDataSourceModel.class;
    }

    @Override
    public VMStatDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new VMStatDataSourceModel((VMStatDataSourceVO)from);
    }

}
