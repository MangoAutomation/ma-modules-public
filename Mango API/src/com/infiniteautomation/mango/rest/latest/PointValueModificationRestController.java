/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.pointValue.XidPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.model.pointValue.emport.PointValueTimeDeleteResult;
import com.infiniteautomation.mango.rest.latest.model.pointValue.emport.PointValueTimeImportResult;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.DataPointRT.FireEvents;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Import point values in bulk for data uploads
 *
 * @author Terry Packer
 */
@Api(value = "Point Value Import/Delete")
@RestController()
@RequestMapping("/point-value-modification")
public class PointValueModificationRestController {

    private final PointValueDao pointValueDao;
    private final DataPointDao dataPointDao;
    private final PermissionService permissionService;
    private final RuntimeManager runtimeManager;
    private final ExecutorService executorService;

    @Autowired
    public PointValueModificationRestController(PointValueDao pointValueDao, DataPointDao dataPointDao,
                                                PermissionService permissionService, RuntimeManager runtimeManager,
                                                ExecutorService executorService) {
        this.pointValueDao = pointValueDao;
        this.dataPointDao = dataPointDao;
        this.permissionService = permissionService;
        this.runtimeManager = runtimeManager;
        this.executorService = executorService;
    }

    @ApiOperation(
            value = "Import Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
    )
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    @Async
    public CompletableFuture<List<PointValueTimeImportResult>> importPointValues(
            @ApiParam(value = "Shall data point listeners be notified, default is NEVER")
            @RequestParam(defaultValue = "NEVER") FireEvents fireEvents,
            @RequestBody Stream<XidPointValueTimeModel> stream,
            @AuthenticationPrincipal PermissionHolder user) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, PointValueTimeImport> results = new LinkedHashMap<>();
                stream.forEachOrdered((pvt) -> {
                    var entry = results.computeIfAbsent(pvt.getXid(),
                            (xidKey) -> new PointValueTimeImport(pvt.getXid(), fireEvents, user));
                    entry.saveValue(pvt.getValue(), pvt.getTimestamp(), pvt.getAnnotation());
                });

                return results.values().stream()
                        .map(v -> new PointValueTimeImportResult(v.xid, v.totalProcessed, v.totalSkipped, v.result))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    @ApiOperation(
            value = "Delete Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
    )
    @RequestMapping(method = RequestMethod.DELETE, value = "/delete")
    @Async
    public CompletableFuture<List<PointValueTimeDeleteResult>> deletePointValues(
            @ApiParam(value = "Shall data point listeners be notified, default is NEVER")
            @RequestParam(defaultValue = "NEVER") FireEvents fireEvents,

            @RequestBody Stream<XidPointValueTimeModel> stream,
            @AuthenticationPrincipal PermissionHolder user) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, PointValueTimeDelete> results = new HashMap<>();

                stream.forEachOrdered((pvt) -> {
                    var entry = results.computeIfAbsent(pvt.getXid(),
                            (xidKey) -> new PointValueTimeDelete(pvt.getXid(), fireEvents, user));
                    entry.deleteValue(pvt.getTimestamp());
                });

                return results.values().stream()
                        .map(v -> new PointValueTimeDeleteResult(v.xid, v.totalProcessed, v.totalSkipped, v.result))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);

    }

    class PointValueTimeImport {

        protected final boolean valid;
        protected final FireEvents fireEvents;
        protected final DataPointVO vo;
        protected final DataType dataType;
        protected String xid;
        protected int totalProcessed;
        protected int totalSkipped;
        protected ProcessResult result;
        protected DataPointRT rt;

        public PointValueTimeImport(String xid, FireEvents fireEvents, PermissionHolder user) {
            this.xid = xid;
            this.result = new ProcessResult();
            this.fireEvents = fireEvents;
            this.vo = dataPointDao.getByXid(xid);
            if (vo == null) {
                valid = false;
                dataType = null;
                result.addContextualMessage("xid", "emport.error.missingPoint", xid);
            } else {
                if (permissionService.hasPermission(user, vo.getSetPermission())) {
                    valid = true;
                    rt = runtimeManager.getDataPoint(vo.getId());
                    dataType = vo.getPointLocator().getDataType();
                } else {
                    valid = false;
                    dataType = null;
                    result.addContextualMessage("xid", "permission.exception.setDataPoint", user.getPermissionHolderName());
                }
            }
        }

        /**
         *
         */
        private void saveValue(Object value, ZonedDateTime date, String annotation) {

            if (valid && value != null) {

                //Validate the model against our point
                long timestamp;
                if (date == null || date.toInstant().toEpochMilli() == 0) {
                    totalSkipped++;
                    return;
                } else {
                    timestamp = date.toInstant().toEpochMilli();
                }

                DataValue dataValue;
                try {
                    switch (dataType) {
                        case ALPHANUMERIC:
                            dataValue = new AlphanumericValue(value.toString());
                            break;
                        case BINARY:
                            if (value instanceof String) {
                                dataValue = new BinaryValue(Boolean.parseBoolean((String) value));
                            } else {
                                dataValue = new BinaryValue((Boolean) value);
                            }
                            break;
                        case MULTISTATE:
                            if (value instanceof String) {
                                try {
                                    dataValue = new MultistateValue(Integer.parseInt((String) value));
                                } catch (NumberFormatException ex) {
                                    try {
                                        dataValue = vo.getTextRenderer().parseText((String) value, dataType);
                                    } catch (Exception e) {
                                        // Lots can go wrong here so let the user know
                                        result.addContextualMessage("value", "event.valueParse.textParse", e.getMessage());
                                        totalSkipped++;
                                        return;
                                    }
                                }
                            } else {
                                dataValue = new MultistateValue(((Number) value).intValue());
                            }
                            break;
                        case NUMERIC:
                            if (value instanceof String) {
                                dataValue = new NumericValue(Double.parseDouble((String) value));
                            } else {
                                dataValue = new NumericValue(((Number) value).doubleValue());
                            }
                            break;
                        default:
                            result.addContextualMessage("dataType", "rest.validate.imageNotSupported");
                            return;
                    }
                } catch (Exception e) {
                    result.addContextualMessage("value", "rest.error.serverError", e.getMessage());
                    totalSkipped++;
                    return;
                }

                PointValueTime pvt;
                if (StringUtils.isEmpty(annotation)) {
                    pvt = new PointValueTime(dataValue, timestamp);
                } else {
                    pvt = new AnnotatedPointValueTime(dataValue, timestamp, new TranslatableMessage("common.default", annotation));
                }
                if (rt == null) {
                    pointValueDao.savePointValueAsync(vo, pvt);
                    //Try for next value to see if the point is enabled now
                    rt = runtimeManager.getDataPoint(vo.getId());
                } else {
                    rt.savePointValueDirectToCache(pvt, null, true, true, fireEvents);
                }
                totalProcessed++;
            } else {
                totalSkipped++;
            }
        }
    }

    class PointValueTimeDelete extends PointValueTimeImport {

        public PointValueTimeDelete(String xid, FireEvents fireEvents, PermissionHolder user) {
            super(xid, fireEvents, user);
        }

        public void deleteValue(ZonedDateTime timestamp) {
            if (valid && timestamp != null) {
                runtimeManager.purgeDataPointValue(vo, timestamp.toInstant().toEpochMilli())
                        .ifPresent(aLong -> totalProcessed += aLong);
            } else {
                totalSkipped++;
            }
        }
    }
}
