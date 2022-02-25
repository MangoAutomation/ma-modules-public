/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.converter.UnitConverter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.goebl.simplify.SimplifyUtility;
import com.infiniteautomation.mango.db.iterators.MergingIterator;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.LegacyPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.LegacyXidPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueImportResult;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PurgeDataPointValuesModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PurgePointValuesResponseModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.PointValueTimeCacheControl;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.XidLatestQueryInfoModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.XidRollupTimeRangeQueryModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.XidTimeRangeQueryModel;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriodType;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AggregateValueMapper;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.DefaultStreamMapper;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.StreamMapperBuilder;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.TimestampGrouper;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingMultiPointModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.rest.pointextractor.PointValueTimePointExtractor;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.datetime.ExpandTimePeriodAdjuster;
import com.infiniteautomation.mango.util.datetime.TruncateTimePeriodAdjuster;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.db.dao.pointvalue.TimeOrder;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RTException;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.DataPointRT.FireEvents;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.view.stats.ITime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 */
@Api(value = "Point Values")
@RestController("PointValueV2RestController")
@RequestMapping("/point-values")
public class PointValueRestController extends AbstractMangoRestController {

    private final Logger log = LoggerFactory.getLogger(PointValueRestController.class);

    private final PointValueDao dao;
    private final MangoTaskTemporaryResourceManager<PurgePointValuesResponseModel> resourceManager;
    private final DataPointService dataPointService;
    private final DataSourceService dataSourceService;
    private final RuntimeManager runtimeManager;

    private final Comparator<StreamingPointValueTimeModel> modelComparator = Comparator.comparingLong(StreamingPointValueTimeModel::getExactTimestamp);

    @Autowired
    public PointValueRestController(PointValueDao dao, TemporaryResourceWebSocketHandler websocket,
                                    PermissionService permissionService, DataPointService dataPointService,
                                    DataSourceService dataSourceService, Environment environment,
                                    RuntimeManager runtimeManager) {
        this.dao = dao;
        this.dataSourceService = dataSourceService;
        this.runtimeManager = runtimeManager;
        this.resourceManager = new MangoTaskTemporaryResourceManager<>(permissionService, websocket, environment);
        this.dataPointService = dataPointService;
    }

    private Stream<IdPointValueTime> latestStream(Collection<? extends DataPointVO> points,
                                                  @Nullable ZonedDateTime before,
                                                  @Nullable Integer limit,
                                                 PointValueTimeCacheControl useCache) {

        var beforeTimestamp = before == null ? null : before.toInstant().toEpochMilli();

        if (useCache == PointValueTimeCacheControl.NONE) {
            return dao.streamPointValuesCombined(points, null, beforeTimestamp, limit, TimeOrder.DESCENDING);
        } else if (useCache == PointValueTimeCacheControl.CACHE_ONLY) {
            var streams = points.stream()
                    .map(point -> streamCache(point, beforeTimestamp, limit))
                    .collect(Collectors.toList());
            return MergingIterator.mergeStreams(streams, TimeOrder.DESCENDING.getComparator());
        } else {
            throw new UnsupportedOperationException("Cache option not supported: " + useCache);
        }
    }

    private Stream<IdPointValueTime> streamCache(DataPointVO point, @Nullable Long before, @Nullable Integer limit) {
        DataPointRT rt = runtimeManager.getDataPoint(point.getId());
        Stream<IdPointValueTime> stream = rt == null ? Stream.empty() : rt.getLatestPointValues().stream()
                .filter(v -> before == null || v.getTime() < before)
                .map(v -> v.withSeriesId(point.getSeriesId()).withFromCache());
        return limit == null ? stream : stream.limit(limit);
    }

    private Stream<IdPointValueTime> simplifyStream(Stream<IdPointValueTime> stream, @Nullable Double simplifyTolerance, @Nullable Integer simplifyTarget) {
        if (simplifyTolerance != null || simplifyTarget != null) {
            var list = stream.collect(Collectors.toList());
            return SimplifyUtility.simplify(simplifyTolerance, simplifyTarget,
                    true, true, list, PointValueTimePointExtractor.INSTANCE, ITime.COMPARATOR)
                    .stream();
        }
        return stream;
    }

    private Function<DataPointVO, Stream<StreamingPointValueTimeModel>> timeRangeStream(
            ZonedDateTime from, ZonedDateTime to, @Nullable Integer limit,
            boolean bookend, @Nullable Double simplifyTolerance, @Nullable Integer simplifyTarget,
            DefaultStreamMapper mapper) {

        return point -> {
            Stream<IdPointValueTime> stream;
            if (bookend) {
                stream = dao.bookendStream(point, from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), limit);
            } else {
                stream = dao.streamPointValues(point, from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), limit, TimeOrder.ASCENDING);
            }
            stream = simplifyStream(stream, simplifyTolerance, simplifyTarget);
            return stream.map(mapper);
        };
    }

    private Function<DataPointVO, Stream<StreamingPointValueTimeModel>> rollupStream(
            ZonedDateTime from, ZonedDateTime to, @Nullable Integer limit,
            RollupEnum rollup, TemporalAmount rollupPeriod,
            DefaultStreamMapper defaultMapper, AggregateValueMapper aggregateMapper) {

        return point -> {
            boolean simplify = false;
            RollupEnum pointRollup = rollup;
            if (pointRollup == RollupEnum.POINT_DEFAULT) {
                pointRollup = RollupEnum.convertTo(point.getRollup());
                simplify = point.isSimplifyDataSets();
            }

            if (pointRollup == RollupEnum.NONE) {
                Stream<IdPointValueTime> stream = dao.bookendStream(point, from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), limit);
                if (simplify) {
                    stream = simplifyStream(stream, point.getSimplifyTolerance(), point.getSimplifyTarget());
                }
                return stream.map(defaultMapper);
            } else {
                var stream = dao.getAggregateDao(rollupPeriod).query(point, from, to, limit);
                return stream.map(aggregateMapper);
            }
        };
    }

    @ApiOperation(
            value = "Get latest values For 1 Data Point in time descending order",
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/latest/{xid}")
    public Stream<StreamingPointValueTimeModel> getLatestPointValues(
            @ApiParam(value = "Point xid", required = true)
            @PathVariable String xid,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Return values before this time")
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime before,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Use cached/intra-interval logging data, for best performance set the data point's cache size >= the the requested limit")
            @RequestParam(value = "useCache", required = false, defaultValue = "NONE") PointValueTimeCacheControl useCache,

            @ApiParam(value = "Tolerance for use in Simplify algorithm")
            @RequestParam(required = false) Double simplifyTolerance,

            @ApiParam(value = "Target number of values to return for use in Simplify algorithm")
            @RequestParam(required = false) Integer simplifyTarget,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale
    ) {

        DataPointVO point = dataPointService.get(xid);

        var stream = latestStream(List.of(point), before, limit, useCache);
        stream = simplifyStream(stream, simplifyTolerance, simplifyTarget);

        DefaultStreamMapper mapper = new StreamMapperBuilder()
                .withDataPoint(point)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, before)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        return stream.map(mapper);
    }

    @ApiOperation(
            value = "Get latest values For 1 or more Data Points in time descending order in a single array",
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/latest/{xids}")
    public Stream<StreamingMultiPointModel> getLatestPointValuesAsSingleArray(
            @ApiParam(value = "Point xids", required = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Return values before this time")
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime before,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Use cached/intra-interval logging data")
            @RequestParam(value = "useCache", required = false, defaultValue = "NONE") PointValueTimeCacheControl useCache,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mergedStream = latestStream(points, before, limit, useCache);
        var mapper = new StreamMapperBuilder()
                .withDataPoints(points)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, before)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        return TimestampGrouper.groupByTimestamp(mergedStream.map(mapper));
    }

    @ApiOperation(
            value = "POST for latest values For 1 or more Data Points in time descending order in a single array",
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class,
            responseContainer = "Array"
    )
    @RequestMapping(method = RequestMethod.POST, value = "/single-array/latest")
    public Stream<StreamingMultiPointModel> postLatestPointValuesAsSingleArray(
            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidLatestQueryInfoModel info,

            Locale locale) {

        return getLatestPointValuesAsSingleArray(info.getXids(), info.getDateTimeFormat(), info.getBefore(),
                info.getTimezone(), info.getLimit(), info.getUseCache(), info.getFields(), locale);
    }

    @ApiOperation(
            value = "GET latest values For 1 or more Data Points in time descending order in multiple arrays",
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class,
            responseContainer = "Object"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/latest/{xids}")
    public Map<String, Stream<StreamingPointValueTimeModel>> getLatestPointValuesAsMultipleArrays(
            @ApiParam(value = "Point xids", required = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Return values before this time")
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime before,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Use cached/intra-interval logging data")
            @RequestParam(value = "useCache", required = false, defaultValue = "NONE") PointValueTimeCacheControl useCache,

            @ApiParam(value = "Tolerance for use in Simplify algorithm")
            @RequestParam(required = false) Double simplifyTolerance,

            @ApiParam(value = "Target number of values to return for use in Simplify algorithm")
            @RequestParam(required = false) Integer simplifyTarget,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapper = new StreamMapperBuilder()
                .withDataPoints(points)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, before)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        return points.stream()
                .collect(Collectors.toUnmodifiableMap(DataPointVO::getXid, point -> {
                    var stream = latestStream(List.of(point), before, limit, useCache);
                    stream = simplifyStream(stream, simplifyTolerance, simplifyTarget);
                    return stream.map(mapper);
                }));
    }

    @ApiOperation(
            value = "Get latest values For 1 or more Data Points in time descending order in multiple arrays",
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class,
            responseContainer = "Object"
    )
    @RequestMapping(method = RequestMethod.POST, value = "/multiple-arrays/latest")
    public Map<String, Stream<StreamingPointValueTimeModel>> postLatestPointValuesAsMultipleArrays(
            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidLatestQueryInfoModel info,

            Locale locale) {

        return getLatestPointValuesAsMultipleArrays(info.getXids(), info.getDateTimeFormat(), info.getBefore(),
                info.getTimezone(), info.getLimit(), info.getUseCache(),
                info.getSimplifyTolerance(), info.getSimplifyTarget(), info.getFields(), locale);
    }

    @ApiOperation(
            value = "Query Time Range For 1 Data Point, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  With a bookend value at from and to if possible/necessary.",
            response = PointValueTimeModel.class,
            responseContainer = "Array"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/time-period/{xid}")
    public Stream<StreamingPointValueTimeModel> getPointValues(
            @ApiParam(value = "Point xid", required = true)
            @PathVariable String xid,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "From time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit (not including bookend values)")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Bookend")
            @RequestParam(value = "bookend", required = false, defaultValue = "false") boolean bookend,

            @ApiParam(value = "Tolerance for use in Simplify algorithm")
            @RequestParam(required = false) Double simplifyTolerance,

            @ApiParam(value = "Target number of values to return for use in Simplify algorithm")
            @RequestParam(required = false) Integer simplifyTarget,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        DataPointVO point = dataPointService.get(xid);

        var mapper = new StreamMapperBuilder()
                .withDataPoint(point)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        return timeRangeStream(from, to, limit, bookend, simplifyTolerance, simplifyTarget, mapper).apply(point);
    }

    @ApiOperation(
            value = "Rollup values For 1 Data Point, return in time ascending order",
            notes = "From time inclusive, To time exclusive.",
            response = PointValueTimeModel.class,
            responseContainer = "Array"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/time-period/{xid}/{rollup}")
    public Stream<StreamingPointValueTimeModel> getRollupPointValues(
            @ApiParam(value = "Point xid", required = true)
            @PathVariable String xid,

            @ApiParam(value = "Rollup type")
            @PathVariable(value = "rollup") RollupEnum rollup,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "From time")
            @RequestParam(value = "from")
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to")
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Time Period Type")
            @RequestParam(value = "timePeriodType", required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods")
            @RequestParam(value = "timePeriods", required = false) Integer timePeriods,

            @ApiParam(value = "Truncate the from time and expand to time based on the time period settings")
            @RequestParam(value = "truncate", required = false, defaultValue = "false") boolean truncate,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        DataPointVO point = dataPointService.get(xid);

        var mapperBuilder = new StreamMapperBuilder()
                .withDataPoint(point)
                .withRollup(rollup)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale);

        var defaultMapper = mapperBuilder.build(DefaultStreamMapper::new);
        var aggregateMapper = mapperBuilder.build(AggregateValueMapper::new);

        if (truncate) {
            from = from.with(new TruncateTimePeriodAdjuster(timePeriodType.getChronoUnit(), timePeriods));
            to = to.with(new ExpandTimePeriodAdjuster(from, timePeriodType.getChronoUnit(), timePeriods));
        }

        var rollupPeriod = timePeriodType.toTemporalAmount(timePeriods);
        return rollupStream(from, to, limit, rollup, rollupPeriod, defaultMapper, aggregateMapper).apply(point);
    }

    @ApiOperation(value = "Query Time Range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array with bookends, use limit if provided.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/time-period/{xids}")
    public Stream<StreamingMultiPointModel> getPointValuesAsSingleArray(
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "From time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit (not including bookend values)")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Bookend")
            @RequestParam(value = "bookend", required = false, defaultValue = "false") boolean bookend,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapper = new StreamMapperBuilder()
                .withDataPoints(points)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        var streamGenerator = timeRangeStream(from, to, limit, bookend, null, null, mapper);
        var streams = points.stream().map(streamGenerator).collect(Collectors.toList());

        // merge the streams and group by timestamp
        var mergedStream = MergingIterator.mergeStreams(streams, modelComparator);
        return TimestampGrouper.groupByTimestamp(mergedStream);
    }

    @ApiOperation(value = "POST to query a time range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array with bookends, use limit if provided.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.POST, value = "/single-array/time-period")
    public Stream<StreamingMultiPointModel> postPointValuesAsSingleArray(
            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidTimeRangeQueryModel model,

            Locale locale) {

        return getPointValuesAsSingleArray(model.getXids(), model.getDateTimeFormat(), model.getFrom(), model.getTo(),
                model.getTimezone(), model.getLimit(), model.isBookend(), model.getFields(), locale);
    }

    @ApiOperation(value = "Rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/time-period/{xids}/{rollup}")
    public Stream<StreamingMultiPointModel> getRollupPointValuesAsSingleArray(
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Rollup type")
            @PathVariable(value = "rollup") RollupEnum rollup,

            @ApiParam(value = "From time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time Period Type")
            @RequestParam(value = "timePeriodType", required = false)
                    TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods")
            @RequestParam(value = "timePeriods", required = false) Integer timePeriods,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Truncate the from time and expand to time based on the time period settings")
            @RequestParam(value = "truncate", required = false, defaultValue = "false") boolean truncate,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapperBuilder = new StreamMapperBuilder()
                .withDataPoints(points)
                .withRollup(rollup)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale);

        var defaultMapper = mapperBuilder.build(DefaultStreamMapper::new);
        var aggregateMapper = mapperBuilder.build(AggregateValueMapper::new);

        if (truncate) {
            from = from.with(new TruncateTimePeriodAdjuster(timePeriodType.getChronoUnit(), timePeriods));
            to = to.with(new ExpandTimePeriodAdjuster(from, timePeriodType.getChronoUnit(), timePeriods));
        }

        var rollupPeriod = timePeriodType.toTemporalAmount(timePeriods);
        var streamGenerator = rollupStream(from, to, limit, rollup, rollupPeriod, defaultMapper, aggregateMapper);
        var streams = points.stream()
                .map(streamGenerator)
                .collect(Collectors.toUnmodifiableList());

        // merge the streams and group by timestamp
        var mergedStream = MergingIterator.mergeStreams(streams, Comparator.comparingLong(StreamingPointValueTimeModel::getExactTimestamp));
        return TimestampGrouper.groupByTimestamp(mergedStream);
    }

    @ApiOperation(value = "POST to get rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.POST, value = "/single-array/time-period/{rollup}")
    public Stream<StreamingMultiPointModel> postRollupPointValuesAsSingleArray(
            @ApiParam(value = "Rollup type")
            @PathVariable(value = "rollup") RollupEnum rollup,

            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidRollupTimeRangeQueryModel model,

            Locale locale) {

        return getRollupPointValuesAsSingleArray(model.getXids(), rollup, model.getFrom(), model.getTo(),
                model.getTimePeriod().getType(), model.getTimePeriod().getPeriods(), model.getTimezone(),
                model.getLimit(), model.getDateTimeFormat(), model.isTruncate(), model.getFields(), locale);
    }

    @ApiOperation(value = "Query time range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to values with optionally limited value arrays with bookends.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/time-period/{xids}")
    public Map<String, Stream<StreamingPointValueTimeModel>> getPointValuesForMultiplePointsAsMultipleArrays(
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "From time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit (per series not including bookend values)")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Bookend")
            @RequestParam(value = "bookend", required = false, defaultValue = "false") boolean bookend,

            @ApiParam(value = "Tolerance for use in Simplify algorithm")
            @RequestParam(required = false) Double simplifyTolerance,

            @ApiParam(value = "Target number of values to return for use in Simplify algorithm")
            @RequestParam(required = false) Integer simplifyTarget,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapper = new StreamMapperBuilder()
                .withDataPoints(points)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale)
                .build(DefaultStreamMapper::new);

        var streamGenerator = timeRangeStream(from, to, limit,
                bookend, simplifyTolerance, simplifyTarget, mapper);
        return points.stream()
                .collect(Collectors.toMap(DataPointVO::getXid, streamGenerator));
    }

    @ApiOperation(value = "POST to query time range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to values with optionally limited value arrays with bookends.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.POST, value = "/multiple-arrays/time-period")
    public Map<String, Stream<StreamingPointValueTimeModel>> postPointValuesForMultiplePointsAsMultipleArrays(
            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidTimeRangeQueryModel model,

            Locale locale) {

        return getPointValuesForMultiplePointsAsMultipleArrays(model.getXids(), model.getDateTimeFormat(),
                model.getFrom(), model.getTo(), model.getTimezone(), model.getLimit(), model.isBookend(),
                model.getSimplifyTolerance(), model.getSimplifyTarget(), model.getFields(), locale);
    }

    @ApiOperation(value = "Rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to point value time arrays.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/time-period/{xids}/{rollup}")
    public Map<String, Stream<StreamingPointValueTimeModel>> getRollupPointValuesAsMultipleArrays(
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,

            @ApiParam(value = "Rollup type")
            @PathVariable(value = "rollup") RollupEnum rollup,

            @ApiParam(value = "From time") @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time") @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time Period Type")
            @RequestParam(value = "timePeriodType", required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods")
            @RequestParam(value = "timePeriods", required = false) Integer timePeriods,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit (per series)")
            @RequestParam(value = "limit", required = false) Integer limit,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Truncate the from time and expand to time based on the time period settings")
            @RequestParam(value = "truncate", required = false, defaultValue = "false") boolean truncate,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapperBuilder = new StreamMapperBuilder()
                .withDataPoints(points)
                .withRollup(rollup)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale);

        var defaultMapper = mapperBuilder.build(DefaultStreamMapper::new);
        var aggregateMapper = mapperBuilder.build(AggregateValueMapper::new);

        if (truncate) {
            from = from.with(new TruncateTimePeriodAdjuster(timePeriodType.getChronoUnit(), timePeriods));
            to = to.with(new ExpandTimePeriodAdjuster(from, timePeriodType.getChronoUnit(), timePeriods));
        }

        var rollupPeriod = timePeriodType.toTemporalAmount(timePeriods);
        var streamGenerator = rollupStream(from, to, limit, rollup, rollupPeriod, defaultMapper, aggregateMapper);
        return points.stream()
                .collect(Collectors.toUnmodifiableMap(DataPointVO::getXid, streamGenerator));
    }

    @ApiOperation(value = "POST to rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to point value time arrays.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.POST, value = "/multiple-arrays/time-period/{rollup}")
    public Map<String, Stream<StreamingPointValueTimeModel>> postRollupPointValuesAsMultipleArrays(
            @ApiParam(value = "Rollup type")
            @PathVariable(value = "rollup") RollupEnum rollup,

            @ApiParam(value = "Query Information", required = true)
            @RequestBody XidRollupTimeRangeQueryModel model,

            Locale locale) {

        return getRollupPointValuesAsMultipleArrays(model.getXids(), rollup, model.getFrom(), model.getTo(),
                model.getTimePeriod().getType(), model.getTimePeriod().getPeriods(), model.getTimezone(),
                model.getLimit(), model.getDateTimeFormat(), model.isTruncate(), model.getFields(), locale);
    }

    @ApiOperation(value = "GET statistics for data point(s) over the given time range",
            notes = "From time inclusive, To time exclusive. Returns map of xid to Statistics object",
            response = PointValueTimeModel.class, responseContainer = "Map")
    @RequestMapping(method = RequestMethod.GET, value = "/statistics/{xids}")
    public Map<String, StreamingPointValueTimeModel> getStatistics(
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,

            @ApiParam(value = "From time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false) String timezone,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used")
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,

            @ApiParam(value = "Fields to be included in the returned data, default is TIMESTAMP,VALUE")
            @RequestParam(required = false) PointValueField[] fields,

            Locale locale) {

        var points = Arrays.stream(xids).distinct()
                .map(dataPointService::get)
                .collect(Collectors.toUnmodifiableSet());

        var mapperBuilder = new StreamMapperBuilder()
                .withDataPoints(points)
                .withRollup(RollupEnum.ALL)
                .withFields(fields)
                .withDateTimeFormat(dateTimeFormat)
                .withTimezone(timezone, from, to)
                .withLocale(locale);

        var aggregateMapper = mapperBuilder.build(AggregateValueMapper::new);
        var rollupPeriod = Duration.between(from, to);

        return points.stream().collect(Collectors.toUnmodifiableMap(DataPointVO::getXid,
                point -> dao.getAggregateDao(rollupPeriod)
                        .query(point, from, to, null)
                        .map(aggregateMapper)
                        .findAny().orElseThrow()));
    }

    /**
     * Update a point value in the system
     */
    @ApiOperation(value = "Update an existing data point's value",
            notes = "Data point must exist and be enabled")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<LegacyPointValueTimeModel> putPointValue(@RequestBody() LegacyPointValueTimeModel model, @PathVariable String xid,

                                                                   @ApiParam(value = "Return converted value using displayed unit",
                                                                           defaultValue = "false") @RequestParam(required = false,
                                                                           defaultValue = "false") boolean unitConversion,
                                                                   @AuthenticationPrincipal PermissionHolder user,
                                                                   UriComponentsBuilder builder) {


        DataPointVO vo = this.dataPointService.get(xid);
        this.dataPointService.ensureSetPermission(user, vo);
        // Set the time to now if it is not present
        if (model.getTimestamp() == 0) {
            model.setTimestamp(Common.timer.currentTimeMillis());
        }

        // Validate the model's data type for compatibility
        if (model.getDataType() != vo.getPointLocator().getDataType()) {
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE,
                    new TranslatableMessage("event.ds.dataType"));
        }

        // Validate the timestamp for future dated
        if (model.getTimestamp() > Common.timer.currentTimeMillis()
                + SystemSettingsDao.getInstance().getFutureDateLimit()) {
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                    "common.default", "Future dated points not acceptable."));
        }

        // TODO Backdate validation?
        // boolean backdated = pointValue != null && newValue.getTime() <
        // pointValue.getTime();

        // Are we converting from the rendered Unit?
        if (unitConversion) {
            if (model.getDataType() == DataType.NUMERIC && model.getValue() instanceof Number) {
                double convertedValue = ((Number) model.getValue()).doubleValue();
                UnitConverter inverseConverter = vo.getRenderedUnitConverter().inverse();
                double rawValue = inverseConverter.convert(convertedValue);
                model.setValue(rawValue);
            } else {
                throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE,
                        new TranslatableMessage("common.default", "[" + xid
                                + "]Cannot perform unit conversion on Non Numeric data types."));
            }
        }

        // If we are a multistate point and our value is in string format then we should try
        // to convert it
        if ((model.getDataType() == DataType.MULTISTATE || model.getDataType() == DataType.NUMERIC)
                && (model.getValue() instanceof String)) {
            try {
                DataValue value =
                        vo.getTextRenderer().parseText((String) model.getValue(),
                                vo.getPointLocator().getDataType());
                model.setValue(value.getObjectValue());
            } catch (Exception e) {
                // Lots can go wrong here so let the user know
                throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE,
                        new TranslatableMessage("common.default", "[" + xid
                                + "]Unable to convert String representation to any known value."));
            }
        }

        final PointValueTime pvt;
        try {
            DataValue dataValue;
            switch (model.getDataType()) {
                case ALPHANUMERIC:
                    dataValue = new AlphanumericValue((String) model.getValue());
                    break;
                case BINARY:
                    dataValue = new BinaryValue((Boolean) model.getValue());
                    break;
                case MULTISTATE:
                    dataValue = new MultistateValue(((Number) model.getValue()).intValue());
                    break;
                case NUMERIC:
                    dataValue = new NumericValue(((Number) model.getValue()).doubleValue());
                    break;
                default:
                    throw new UnsupportedOperationException("Setting image values not supported");

            }

            if (model.getAnnotation() != null)
                pvt = new AnnotatedPointValueTime(dataValue, model.getTimestamp(), new TranslatableMessage("common.default", model.getAnnotation()));
            else
                pvt = new PointValueTime(dataValue, model.getTimestamp());
        } catch (Exception e) {
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                    "common.default", "[" + xid + "]Invalid Format"));
        }

        // one last check to ensure we are inserting the correct data type
        if (pvt.getValue().getDataType() != vo.getPointLocator()
                .getDataType()) {
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE,
                    new TranslatableMessage("event.ds.dataType"));
        }

        final int dataSourceId = vo.getDataSourceId();
        SetPointSource source = null;
        if (model.getAnnotation() != null) {
            source = new SetPointSource() {

                @Override
                public String getSetPointSourceType() {
                    return "REST";
                }

                @Override
                public int getSetPointSourceId() {
                    return dataSourceId;
                }

                @Override
                public TranslatableMessage getSetPointSourceMessage() {
                    return ((AnnotatedPointValueTime) pvt).getSourceMessage();
                }

                @Override
                public void raiseRecursionFailureEvent() {
                    log.error("Recursive failure while setting point via REST");
                }

            };
        }
        try {
            Common.runtimeManager.setDataPointValue(vo.getId(), pvt, source);
            // This URI may not always be accurate if the Data Source doesn't use the
            // provided time...
            URI location = builder.path("/point-values/{xid}/{time}")
                    .buildAndExpand(xid, pvt.getTime()).toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(location);
            return new ResponseEntity<>(model, headers, HttpStatus.CREATED);

        } catch (RTException e) {
            // Ok its probably not enabled or settable
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                    "common.default", "[" + xid + "]" + e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorException(e);
        }
    }

    @ApiOperation(
            value = "Import Point Values for one or many Data Points, this is deprecated and it is recommended to use the /point-value-modification endpoints",
            notes = "Data Point must exist and user must have write access"
    )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Collection<PointValueImportResult>> savePointsValues(@ApiParam(value = "Shall data point listeners be notifified, default is NEVER")
                                                                               @RequestParam(defaultValue = "NEVER") FireEvents fireEvents,
                                                                               @RequestBody() List<LegacyXidPointValueTimeModel> models,
                                                                               @AuthenticationPrincipal User user
    ) {

        //Map of XIDs to results
        Map<String, PointValueImportResult> results = new HashMap<>();

        for (LegacyXidPointValueTimeModel model : models) {
            PointValueImportResult result = results.get(model.getXid());
            if (result == null) {
                result = new PointValueImportResult(model.getXid(), dao, dataPointService, fireEvents, user);
                results.put(model.getXid(), result);
            }
            //Attempt to save it
            result.saveValue(model);
        }

        return ResponseEntity.ok(results.values());
    }

    @ApiOperation(
            value = "Delete point values >= from  and < to",
            notes = "The user must have set permission to the data point. If date is not supplied it defaults to now."
    )
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ResponseEntity<Long> deletePointValues(
            @ApiParam(value = "Point xids", required = true)
            @PathVariable
                    String xid,

            @ApiParam(value = "From time"
            )
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
                    ZonedDateTime from,

            @ApiParam(value = "To time"
            )
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
                    ZonedDateTime to,

            @ApiParam(value = "Time zone")
            @RequestParam(value = "timezone", required = false)
                    String timezone,

            @AuthenticationPrincipal PermissionHolder user) {


        DataPointVO vo = dataPointService.get(xid);
        dataPointService.ensureSetPermission(user, vo);

        ZoneId zoneId;
        if (timezone == null) {
            if (from != null) {
                zoneId = from.getZone();
            } else if (to != null)
                zoneId = to.getZone();
            else
                zoneId = TimeZone.getDefault().toZoneId();
        } else {
            zoneId = ZoneId.of(timezone);
        }

        // Set the timezone on the from and to dates
        long current = Common.timer.currentTimeMillis();
        if (from != null)
            from = from.withZoneSameInstant(zoneId);
        else
            from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);
        if (to != null)
            to = to.withZoneSameInstant(zoneId);
        else
            to = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);

        Optional<Long> result = Common.runtimeManager.purgeDataPointValuesBetween(vo,
                from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli());
        return ResponseEntity.ok().body(result.orElse(null));
    }

    @ApiOperation(
            value = "Update point attributes, return all attributes after change",
            notes = "Data Point must be running and user must have write access"
    )
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}/attributes")
    public ResponseEntity<Map<String, Object>> updatePointAttributes(
            @ApiParam(value = "Point xids", required = true)
            @PathVariable String xid,
            @RequestBody() Map<String, Object> attributes,
            @AuthenticationPrincipal PermissionHolder user
    ) {
        DataPointVO vo = dataPointService.get(xid);
        dataPointService.ensureSetPermission(user, vo);
        DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getId());
        if (rt == null)
            throw new NotFoundRestException();
        for (Entry<String, Object> entry : attributes.entrySet()) {
            rt.setAttribute(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(rt.getAttributes());
    }

    @ApiOperation(
            value = "Purge Point Values for one or many data points, or a single data source",
            notes = "User must have edit access to data source and its points, use created header to track progress/cancel"
    )
    @RequestMapping(method = RequestMethod.POST, value = "/purge")
    public ResponseEntity<TemporaryResource<PurgePointValuesResponseModel, AbstractRestException>> purgePointValues(@RequestBody() PurgeDataPointValuesModel model,
                                                                                                                    UriComponentsBuilder builder) {

        model.ensureValid();
        TemporaryResource<PurgePointValuesResponseModel, AbstractRestException> response = resourceManager.newTemporaryResource(
                "DATA_POINT_PURGE", null, model.getExpiry(), model.getTimeout(),
                (resource) -> {
                    PurgePointValuesResponseModel result = new PurgePointValuesResponseModel();

                    Map<Integer, DataSourceVO> dataSourceMap = new HashMap<>();
                    Map<String, DataPointVO> dataPointsMap = new HashMap<>();

                    //Build the list of data point Xids
                    List<String> xids = model.getXids();
                    if (xids != null && !xids.isEmpty()) {
                        for (String xid : xids) {
                            DataPointVO vo = dataPointService.get(xid);
                            dataPointsMap.put(xid, vo);
                            if (vo != null) {
                                dataSourceMap.computeIfAbsent(vo.getDataSourceId(), (key) -> dataSourceService.get(vo.getDataSourceId()));
                            }
                        }
                    } else {
                        DataSourceVO ds = dataSourceService.get(model.getDataSourceXid());
                        xids = new ArrayList<>();
                        if (ds != null) {
                            dataSourceMap.put(ds.getId(), ds);
                            List<DataPointVO> points = dataPointService.getDataPoints(ds.getId());
                            for (DataPointVO point : points) {
                                xids.add(point.getXid());
                                dataPointsMap.put(point.getXid(), point);
                            }
                        }
                    }
                    int maximum = xids.size();
                    int position = 0;

                    //Initial status
                    resource.progressOrSuccess(result, position, maximum);


                    for (String xid : xids) {
                        try {
                            //Get the point and its data source XID
                            DataPointVO dp = dataPointsMap.get(xid);
                            if (dp == null)
                                throw new NotFoundException();

                            DataSourceVO ds = dataSourceMap.get(dp.getDataSourceId());
                            if (ds == null)
                                throw new NotFoundException();

                            //Do purge based on settings
                            if (model.isPurgeAll())
                                Common.runtimeManager.purgeDataPointValues(dp);
                            else if (model.isUseTimeRange())
                                Common.runtimeManager.purgeDataPointValuesBetween(dp, model.getTimeRange().getFrom().getTime(), model.getTimeRange().getTo().getTime());
                            else {
                                long before = DateUtils.minus(Common.timer.currentTimeMillis(), TimePeriodType.convertFrom(model.getDuration().getType()), model.getDuration().getPeriods());
                                Common.runtimeManager.purgeDataPointValues(dp, before);
                            }
                            result.getSuccessfullyPurged().add(xid);
                        } catch (NotFoundException e) {
                            result.getNotFound().add(xid);
                        } catch (PermissionException e) {
                            result.getNoEditPermission().add(xid);
                        }
                        position++;
                        resource.progressOrSuccess(result, position, maximum);
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/point-values/purge/{id}").buildAndExpand(response.getId()).toUri());
        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }


    @ApiOperation(value = "Update a purge task using its id",
            notes = "Only allowed operation is to change the status to CANCELLED. " +
                    "User can only update their own purge task unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value = "/purge/{id}")
    public TemporaryResource<PurgePointValuesResponseModel, AbstractRestException> updateDataPointPurge(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id,

            @RequestBody
                    TemporaryResourceStatusUpdate body) {

        TemporaryResource<PurgePointValuesResponseModel, AbstractRestException> resource = resourceManager.get(id);

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            if (!resource.isComplete()) {
                resource.cancel();
            }
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a purge operation using its id",
            notes = "User can only get their own status unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value = "/purge/{id}")
    public TemporaryResource<PurgePointValuesResponseModel, AbstractRestException> getDataPointPurgeStatus(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id) {

        return resourceManager.get(id);
    }

    @ApiOperation(value = "Remove a purge task using its id",
            notes = "Will only remove a task if it is complete. " +
                    "User can only remove their own purge task unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value = "/purge/data-points/{id}")
    public void removeDataPointPurgeTask(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id) {

        TemporaryResource<PurgePointValuesResponseModel, AbstractRestException> resource = resourceManager.get(id);
        resource.remove();
    }

}
