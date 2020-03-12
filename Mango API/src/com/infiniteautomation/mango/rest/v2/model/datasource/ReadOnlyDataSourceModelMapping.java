/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.datasource;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class ReadOnlyDataSourceModelMapping implements RestModelMapping<DataSourceVO, ReadOnlyDataSourceModel> {

    @Override
    public Class<? extends DataSourceVO> fromClass() {
        return DataSourceVO.class;
    }

    @Override
    public Class<? extends ReadOnlyDataSourceModel> toClass() {
        return ReadOnlyDataSourceModel.class;
    }

    @Override
    public ReadOnlyDataSourceModel map(Object from, PermissionHolder user,
            RestModelMapper mapper) {
        return new ReadOnlyDataSourceModel((DataSourceVO)from);
    }

}
