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

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer.BaseTextRendererModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.ReadOnlyDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.AbstractEventDetectorModel;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.AbstractPointEventDetectorModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.spring.service.EventDetectorsService;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataPoint.DataPointWithEventDetectors;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.util.ILifecycleState;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

@Api(value="Configuration")
@RestController(value="Configuration")
@RequestMapping("/configuration")
public class ConfigurationRestController {

    private final RestModelMapper mapper;
    private final DataPointService dataPointService;
    private final BiFunction<DataPointVO, PermissionHolder, DataPointModel> mapPoint;
    private final DataSourceService dataSourceService;
    private final BiFunction<DataSourceVO, PermissionHolder, AbstractDataSourceModel<?>> mapSource;

    private final EventDetectorsService eventDetectorsService;
    private final BiFunction<AbstractEventDetectorVO, PermissionHolder, AbstractEventDetectorModel<?>> mapDetector;

    private final PublisherService publisherService;
    private final PublishedPointService publishedPointService;


    public ConfigurationRestController(final RestModelMapper modelMapper, DataPointService dataPointService,
                                       DataSourceService dataSourceService,
                                       EventDetectorsService eventDetectorsService,
                                       PublisherService publisherService,
                                       PublishedPointService publishedPointService) {
        this.mapper = modelMapper;

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
        this.eventDetectorsService = eventDetectorsService;
        this.mapDetector = (vo, user) -> {
            return modelMapper.map(vo, AbstractEventDetectorModel.class, user);
        };
        this.publisherService = publisherService;
        this.publishedPointService = publishedPointService;
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



    @ApiOperation(value = "Gets the mango data point configuration as CSV")
    @RequestMapping(method = RequestMethod.GET, value = "/query-data-points", produces= MediaTypes.CSV_VALUE)
    public List<DataPointWithEventDetectorsModel> queryDataPoints(
            HttpServletRequest request,
            @AuthenticationPrincipal PermissionHolder user) {

        return generateModels(dataPointService.list(), user);
    }

    @ApiOperation(value = "Gets the mango data point configuration as CSV")
    @RequestMapping(method = RequestMethod.GET, value = "/query-data-points/{dsXid}", produces= MediaTypes.CSV_VALUE)
    public List<DataPointWithEventDetectorsModel> queryDataPointsForSource(
            @PathVariable String dsXid,
            @AuthenticationPrincipal PermissionHolder user) {
        DataSourceVO ds = dataSourceService.get(dsXid);
        return generateModels(dataPointService.getDataPoints(ds.getId()), user);
    }

    protected List<DataPointWithEventDetectorsModel> generateModels(List<DataPointVO> dataPoints, PermissionHolder user) {
        List<DataPointWithEventDetectorsModel> configuration = new ArrayList<>();
        for(DataPointVO dp : dataPoints) {
            DataPointWithEventDetectors dpeds = dataPointService.getWithEventDetectors(dp.getXid());
            List<AbstractEventDetectorModel<?>> detectorModels = new ArrayList<>();
            for(AbstractEventDetectorVO edvo : dpeds.getEventDetectors()) {
                AbstractEventDetectorModel<?> model = mapDetector.apply(edvo, user);
                ((AbstractPointEventDetectorModel)model).setDataPoint(null);
                detectorModels.add(model);
            }
            DataPointWithEventDetectorsModel model = new DataPointWithEventDetectorsModel(dpeds.getDataPoint(), detectorModels);

            //TODO This currently happens in the DataPointModelMapper
            AbstractPointLocatorModel<?> pointLocatorModel = mapper.map(dpeds.getDataPoint().getPointLocator(), AbstractPointLocatorModel.class, user);
            model.setPointLocator(pointLocatorModel);
            BaseTextRendererModel<?> textRenderer = mapper.map(dpeds.getDataPoint().getTextRenderer(), BaseTextRendererModel.class, user);
            model.setTextRenderer(textRenderer);

            DataPointRT rt = Common.runtimeManager.getDataPoint(dpeds.getDataPoint().getId());
            ILifecycleState state = rt != null ? rt.getLifecycleState() : ILifecycleState.TERMINATED;
            model.setLifecycleState(state);

            configuration.add(model);
        }
        return configuration;
    }

    @ApiOperation(value = "Bulk insert/update data sources and points",
            notes = "",
            consumes=MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value="/bulk-data-points", consumes=MediaTypes.CSV_VALUE)
    public void consumeDataPoints(@RequestBody List<DataPointWithEventDetectorsModel> configuration) {
        Map<String, PublisherVO> pubXidMap = new HashMap<>();
        for(DataPointWithEventDetectorsModel m : configuration) {

            //Insert the point
            DataPointVO dp = dataPointService.get(m.getXid());
            if(dp == null) {
                dp = dataPointService.insert(m.toVO());
            }else {
                dataPointService.update(dp.getId(), m.toVO());
            }

            //Deal with the detectors
            for(AbstractEventDetectorModel<?> edm : m.getDetectors()) {
                AbstractEventDetectorVO ed = eventDetectorsService.get(edm.getXid());
                if(ed == null) {
                    eventDetectorsService.insert(edm.toVO());
                }else {
                    eventDetectorsService.update(ed.getId(), edm.toVO());
                }
            }

            //Update the publisher
            if(StringUtils.isNotEmpty(m.getPublisherXid())) {
                PublisherVO vo = pubXidMap.computeIfAbsent(m.publisherXid, (k) -> {
                   try {
                       return publisherService.get(m.publisherXid);
                   } catch(Exception e) {
                       //Munch and ignore,
                       //TODO validation error if the publisher DNE
                   }
                   return null;
                });

                if(vo != null) {
                    //TODO this will always just insert a new published point for this data point, we
                    // really want to only update an existing one or insert a new one.  To do that we need
                    // a way to lookup a published point by its data point source and publisher.  But some publishers allow
                    // publishing a data point multiple times.
                    PublishedPointVO pp = vo.getDefinition().createPublishedPointVO(vo, dp);
                    pp.setName(dp.getName());
                    publishedPointService.insert(pp);
                }
            }
        }
    }

    public static class DataPointWithEventDetectorsModel extends DataPointModel {

        private List<AbstractEventDetectorModel<?>> detectors;
        private String publisherXid; //What publisher to add this point to
        public DataPointWithEventDetectorsModel() {}

        public DataPointWithEventDetectorsModel(DataPointVO dp, List<AbstractEventDetectorModel<?>> detectors) {
            super(dp);
            this.detectors = detectors;
        }

        public List<AbstractEventDetectorModel<?>> getDetectors() {
            return detectors;
        }

        public void setDetectors(List<AbstractEventDetectorModel<?>> detectors) {
            this.detectors = detectors;
        }

        public String getPublisherXid() {
            return publisherXid;
        }

        public void setPublisherXid(String publisherXid) {
            this.publisherXid = publisherXid;
        }
    }
}
