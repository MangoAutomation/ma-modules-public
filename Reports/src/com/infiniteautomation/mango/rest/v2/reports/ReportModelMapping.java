/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class ReportModelMapping implements RestModelMapping<ReportVO, ReportModel> {

    @Override
    public ReportModel map(Object o, User user, RestModelMapper mapper) {
        return new ReportModel((ReportVO)o);
    }

    @Override
    public Class<ReportModel> toClass() {
        return ReportModel.class;
    }

    @Override
    public Class<ReportVO> fromClass() {
        return ReportVO.class;
    }
}
