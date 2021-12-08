/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.realtime.RealTimeDataPointValueModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Api(value="Access to current values combined with data point information for all currently running data points.")
@RestController()
@RequestMapping("/realtime")
public class RealTimeDataRestController {

    private final String OK = "OK";
    private final String UNRELIABLE = "UNRELIABLE";

    private final PermissionService permissionService;
    private final BiFunction<DataPointRT, PermissionHolder, RealTimeDataPointValueModel> map;
    private final DataPointService dataPointService;


    @Autowired
    public RealTimeDataRestController(PermissionService permissionService, RestModelMapper modelMapper,
                                      DataPointService dataPointService){
        this.permissionService = permissionService;
        this.dataPointService = dataPointService;
        this.map = (rt, user) -> {
            RealTimeDataPointValueModel model = new RealTimeDataPointValueModel();

            model.setXid(rt.getVO().getXid());
            model.setDeviceName(rt.getVO().getDeviceName());
            model.setName(rt.getVO().getName());

            PointValueTime pvt = rt.getPointValue();
            if(pvt != null) {
                model.setValue(pvt.getValue().getObjectValue());
                model.setTimestamp(pvt.getTime());
                model.setRenderedValue(rt.getVO().getTextRenderer().getText(pvt, TextRenderer.HINT_FULL));
            }

            Translations translations = Translations.getTranslations(user.getLocaleObject());
            model.setType(rt.getVO().getPointLocator().getDataTypeMessage().translate(translations));

            model.setAttributes(new HashMap<>(rt.getAttributes()));

            Object unreliable = rt.getAttribute(DataSourceRT.ATTR_UNRELIABLE_KEY);
            if ((unreliable instanceof Boolean) && ((Boolean) unreliable)) {
                model.setStatus(UNRELIABLE);
            }else {
                model.setStatus(OK);
            }

            model.setTags(rt.getVO().getTags());
            return model;
        };
    }

    /**
     * Query the User's Real Time Data
     */
    @ApiOperation(value = "Query realtime values",
            notes = "Check the status member to ensure the point is OK not DISABLED or UNRELIABLE")
    @RequestMapping(method = RequestMethod.GET)
    public StreamWithTotal<RealTimeDataPointValueModel> query(
            @AuthenticationPrincipal PermissionHolder user,
            ASTNode query,
            Translations translations) {
        //First build all the models
        List<RealTimeDataPointValueModel> models = new ArrayList<>();
        for(DataPointRT rt : Common.runtimeManager.getRunningDataPoints()) {
            if(dataPointService.hasReadPermission(user,rt.getVO())) {
                models.add(map.apply(rt, user));
            }
        }

        //Query the results
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

}
