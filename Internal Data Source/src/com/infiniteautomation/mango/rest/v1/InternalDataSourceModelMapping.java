/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.internal.InternalDataSourceModel;
import com.serotonin.m2m2.internal.InternalDataSourceVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapping;

/**
 * @author Terry Packer
 *
 */
@Component
public class InternalDataSourceModelMapping implements RestModelMapping<InternalDataSourceVO, InternalDataSourceModel> {

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

}
