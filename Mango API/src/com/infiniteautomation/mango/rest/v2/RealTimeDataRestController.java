/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import com.serotonin.m2m2.i18n.Translations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.realtime.RealTimeDataPointValueModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.User;
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
    private final UriComponentsBuilder imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/{ts}_{id}.jpg");

    private final PermissionService permissionService;
    private final BiFunction<DataPointRT, PermissionHolder, RealTimeDataPointValueModel> map;


    @Autowired
    public RealTimeDataRestController(PermissionService permissionService, RestModelMapper modelMapper){
        this.permissionService = permissionService;
        this.map = (rt, user) -> {
            RealTimeDataPointValueModel model = new RealTimeDataPointValueModel();

            model.setXid(rt.getVO().getXid());
            model.setDeviceName(rt.getVO().getDeviceName());
            model.setName(rt.getVO().getName());

            PointValueTime pvt = rt.getPointValue();
            if(pvt != null) {
                if(pvt.getValue() instanceof ImageValue) {
                    model.setValue(imageServletBuilder.buildAndExpand(pvt.getTime(), rt.getId()).toUri());
                }else {
                    model.setValue(pvt.getValue().getObjectValue());
                }
                model.setTimestamp(pvt.getTime());
                model.setRenderedValue(rt.getVO().getTextRenderer().getText(pvt, TextRenderer.HINT_FULL));
            }

            if(user instanceof User) {
                model.setType(rt.getVO().getPointLocator().getDataTypeMessage().translate(((User)user).getTranslations()));
            }else {
                model.setType(rt.getVO().getPointLocator().getDataTypeMessage().translate(Common.getTranslations()));
            }

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
     * @return
     */
    @ApiOperation(value = "Query realtime values",
            notes = "Check the status member to ensure the point is OK not DISABLED or UNRELIABLE")
    @RequestMapping(method = RequestMethod.GET)
    public StreamWithTotal<RealTimeDataPointValueModel> query(
            @AuthenticationPrincipal User user,
            ASTNode query,
            Translations translations) {
        //First build all the models
        List<DataPointRT> points = Common.runtimeManager.getRunningDataPoints();
        List<RealTimeDataPointValueModel> models = new ArrayList<>();
        for(DataPointRT rt : points) {
            if(permissionService.hasDataPointReadPermission(user, rt.getVO())) {
                models.add(map.apply(rt, user));
            }
        }

        //Query the results
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

}
