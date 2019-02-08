/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.InternalDataSourceModel;
import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.internal.InternalDataSourceDefinition;
import com.serotonin.m2m2.internal.InternalDataSourceVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class InternalDataSourceModelMapping implements RestModelJacksonMapping<InternalDataSourceVO, InternalDataSourceModel> {

    @Override
    public Class<? extends InternalDataSourceVO> fromClass() {
        return InternalDataSourceVO.class;
    }

    @Override
    public Class<? extends InternalDataSourceModel> toClass() {
        return InternalDataSourceModel.class;
    }

    @Override
    public InternalDataSourceModel map(Object from, User user, RestModelMapper mapper) {
        return new InternalDataSourceModel((InternalDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return InternalDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
