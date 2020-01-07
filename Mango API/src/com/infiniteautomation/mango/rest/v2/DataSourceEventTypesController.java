/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.model.datasource.DataSourceDefaultEventTypeModel;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.util.ExportCodes.Element;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Access to the Alarm Levels defined for data sources
 *
 * @author Terry Packer
 *
 */
@Api(value="Data source default event types")
@RestController()
@RequestMapping("/data-source-event-types")
public class DataSourceEventTypesController {

    private final DataSourceService<?> service;

    @Autowired
    public DataSourceEventTypesController(DataSourceService<?> service) {
        this.service = service;
    }

    @ApiOperation(
            value = "Get Default Event Types defined for a data source",
            notes = "User must have data source create permission"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{dataSourceType}")
    public List<DataSourceDefaultEventTypeModel> getAlarmLevelsForType(
            @PathVariable String dataSourceType,
            @AuthenticationPrincipal User user){

        DataSourceDefinition<?> def = service.getDefinition(dataSourceType, user);
        DataSourceVO<?> vo = def.baseCreateDataSourceVO();
        List<EventTypeVO> eventTypes = vo.getEventTypes();
        ExportCodes codes = vo.getEventCodes();

        List<DataSourceDefaultEventTypeModel> defaultTypes = new ArrayList<>();

        for(EventTypeVO type : eventTypes) {
            DataSourceDefaultEventTypeModel model = new DataSourceDefaultEventTypeModel();
            int referenceId2 = type.getEventType().getReferenceId2();
            model.setReferenceId2(referenceId2);
            model.setDescriptionKey(type.getDescription().getKey());
            model.setDescription(type.getDescription());
            for(Element e : codes.getElements()) {
                if(e.getId() == referenceId2) {
                    model.setCode(e.getCode());
                    break;
                }
            }
            model.setDefaultAlarmLevel(type.getAlarmLevel());
            defaultTypes.add(model);
        }

        return defaultTypes;
    }


}
