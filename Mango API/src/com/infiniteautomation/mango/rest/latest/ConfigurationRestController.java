/*
 * Copyright (C) 2023 Radix IoT LLC. All rights reserved.
 *
 *
 */

package com.infiniteautomation.mango.rest.latest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.ReadOnlyDataSourceModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

@Api(value="Configuration")
@RestController(value="Configuration")
@RequestMapping("/configuration")
public class ConfigurationRestController {

    private final DataPointService dataPointService;
    private final BiFunction<DataPointVO, PermissionHolder, DataPointModel> mapPoint;
    private final DataSourceService dataSourceService;
    private final BiFunction<DataSourceVO, PermissionHolder, AbstractDataSourceModel<?>> mapSource;

    public ConfigurationRestController(final RestModelMapper modelMapper, DataPointService dataPointService, DataSourceService dataSourceService) {
        this.dataPointService = dataPointService;
        this.mapPoint = (vo, user) -> {
            return modelMapper.map(vo, DataPointModel.class, user);
        };
        this.dataSourceService = dataSourceService;
        this.mapSource = (vo, user) -> {
            if(dataSourceService.hasEditPermission(user, vo)) {
                return modelMapper.map(vo, AbstractDataSourceModel.class, user);
            }else {
                return new ReadOnlyDataSourceModel(vo);
            }
        };
    }

    @ApiOperation(value = "Gets the mango configuration as CSV")
    @RequestMapping(method = RequestMethod.GET, value = "/query", produces= MediaTypes.CSV_VALUE)
    public List<DataSourcePointConfigModel> queryCsvPost(
            HttpServletRequest request,
            @AuthenticationPrincipal PermissionHolder user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        List<DataSourcePointConfigModel> configuration = new ArrayList<>();
        dataSourceService.list(ds -> {
            List<DataPointVO> points = dataPointService.getDataPoints(ds.getId());
            for(DataPointVO dp : points) {
                configuration.add(new DataSourcePointConfigModel(mapSource.apply(ds, user), mapPoint.apply(dp, user)));
            }
        });

        return configuration;
    }

    @ApiOperation(value = "Bulk insert/update data sources and points",
            notes = "",
            consumes=MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public void consumeConfiguration(@RequestBody List<DataSourcePointConfigModel> configuration) {
        Map<String, DataSourceVO> dsXidMap = new HashMap<>();
        for(DataSourcePointConfigModel m : configuration) {
            DataSourceVO ds = dsXidMap.compute(m.dataSource.getXid(), (k,v) -> {
                DataSourceVO vo = dataSourceService.get(k);
                if(vo == null) {
                    vo = dataSourceService.insert(m.dataSource.toVO());
                }else {
                    vo = dataSourceService.update(vo.getId(), m.dataSource.toVO());
                }
                return vo;
            });

            //Insert the point
            DataPointVO dp = dataPointService.get(m.dataPoint.getXid());
            if(dp == null) {
                dataPointService.insert(m.dataPoint.toVO());
            }else {
                dataPointService.update(dp.getId(), m.dataPoint.toVO());
            }
        }
    }


    public static class DataSourcePointConfigModel {
        private AbstractDataSourceModel<?> dataSource;
        private DataPointModel dataPoint;

        public DataSourcePointConfigModel() {}
        public DataSourcePointConfigModel(AbstractDataSourceModel<?> dataSource, DataPointModel dataPoint) {
            this.dataSource = dataSource;
            this.dataPoint = dataPoint;
        }

        public void setDataSource(AbstractDataSourceModel<?> dataSource) {
            this.dataSource = dataSource;
        }

        public void setDataPoint(DataPointModel dataPoint) {
            this.dataPoint = dataPoint;
        }

        public AbstractDataSourceModel<?> getDataSource() {
            return dataSource;
        }

        public DataPointModel getDataPoint() {
            return dataPoint;
        }
    }
}
