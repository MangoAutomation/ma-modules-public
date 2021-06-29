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
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
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
 *
 * Import point values in bulk for data uploads
 *
 * @author Terry Packer
 *
 */
@Api(value = "Point Value Import/Delete")
@RestController()
@RequestMapping("/point-value-modification")
public class PointValueModificationRestController {

    private final DataPointDao dataPointDao;
    private final PermissionService permissionService;

    @Autowired
    public PointValueModificationRestController(DataPointDao dataPointDao, PermissionService permissionService) {
        this.dataPointDao = dataPointDao;
        this.permissionService = permissionService;
    }

    @ApiOperation(
            value = "Import Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
            )
    @RequestMapping(method = RequestMethod.POST, value="/import")
    @Async
    public CompletableFuture<List<PointValueTimeImportResult>> importPointValues(
            @ApiParam(value = "Shall data point listeners be notifified, default is NEVER", required = false, allowMultiple = false)
            @RequestParam(defaultValue="NEVER") FireEvents fireEvents,
            @RequestBody Stream<XidPointValueTimeModel> stream,
            @AuthenticationPrincipal PermissionHolder user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
                Map<String, PointValueTimeImport> results = new LinkedHashMap<>();
                stream.forEach((pvt) -> {

                    results.compute(pvt.getXid(), (xidKey, entry) ->{
                        if(entry == null) {
                            entry = new PointValueTimeImport(pvt.getXid(), pointValueDao, dataPointDao, fireEvents, user);
                        }
                        entry.saveValue(pvt.getValue(), pvt.getTimestamp(), pvt.getAnnotation());
                        return entry;
                    });

                });

                return results.values().stream().map((v) -> {
                    return new PointValueTimeImportResult(v.xid, v.totalProcessed, v.totalSkipped, v.result);
                }).collect(Collectors.toList());

            }catch(Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    class PointValueTimeImport {

        protected String xid;
        protected int totalProcessed;
        protected int totalSkipped;
        protected ProcessResult result;

        protected final boolean valid;
        protected final PointValueDao dao;
        protected final FireEvents fireEvents;
        protected DataPointRT rt;
        protected final DataPointVO vo;
        protected final int dataTypeId;

        public PointValueTimeImport(String xid, PointValueDao dao, DataPointDao dataPointDao, FireEvents fireEvents, PermissionHolder user) {
            this.xid = xid;
            this.result = new ProcessResult();
            this.dao = dao;
            this.fireEvents = fireEvents;
            this.vo = dataPointDao.getByXid(xid);
            if(vo == null) {
                valid = false;
                dataTypeId = DataTypes.UNKNOWN;
                result.addContextualMessage("xid", "emport.error.missingPoint", xid);
            }else {
                if (permissionService.hasPermission(user, vo.getSetPermission())){
                    valid = true;
                    rt = Common.runtimeManager.getDataPoint(vo.getId());
                    dataTypeId = vo.getPointLocator().getDataTypeId();
                }else {
                    valid = false;
                    dataTypeId = DataTypes.UNKNOWN;
                    result.addContextualMessage("xid", "permission.exception.setDataPoint", user.getPermissionHolderName());
                }
            }
        }

        /**
         *
         * @param value
         * @param date
         * @param annotation
         */
        private void saveValue(Object value, ZonedDateTime date, String annotation) {

            if(valid && value != null) {

                //Validate the model against our point
                long timestamp;
                if(date == null || date.toInstant().toEpochMilli() == 0) {
                    totalSkipped++;
                    return;
                }else {
                    timestamp = date.toInstant().toEpochMilli();
                }

                DataValue dataValue = null;
                try {
                    switch(dataTypeId) {
                        case DataTypes.ALPHANUMERIC:
                            dataValue = new AlphanumericValue(value.toString());
                            break;
                        case DataTypes.BINARY:
                            if(value instanceof String) {
                                dataValue = new BinaryValue(Boolean.valueOf((String)value));
                            }else {
                                dataValue = new BinaryValue((Boolean)value);
                            }
                            break;
                        case DataTypes.MULTISTATE:
                            if(value instanceof String) {
                                try {
                                    dataValue = new MultistateValue(Integer.parseInt((String)value));
                                }catch(NumberFormatException ex) {
                                    try {
                                        dataValue = vo.getTextRenderer().parseText((String) value, dataTypeId);
                                    } catch (Exception e) {
                                        // Lots can go wrong here so let the user know
                                        result.addContextualMessage("value", "event.valueParse.textParse", e.getMessage());
                                        totalSkipped++;
                                        return;
                                    }
                                }
                            }else {
                                dataValue = new MultistateValue(((Number)value).intValue());
                            }
                            break;
                        case DataTypes.NUMERIC:
                            if(value instanceof String) {
                                dataValue =  new NumericValue(Double.valueOf((String)value));
                            }else {
                                dataValue = new NumericValue(((Number)value).doubleValue());
                            }
                            break;
                        case DataTypes.IMAGE:
                        default:
                            result.addContextualMessage("dataType", "rest.validate.imageNotSupported");
                            return;
                    }
                }catch(Exception e) {
                    result.addContextualMessage("value", "rest.error.serverError", e.getMessage());
                    totalSkipped++;
                    return;
                }

                PointValueTime pvt;
                if(StringUtils.isEmpty(annotation)) {
                    pvt = new PointValueTime(dataValue, timestamp);
                }else {
                    pvt = new AnnotatedPointValueTime(dataValue, timestamp, new TranslatableMessage("common.default", annotation));
                }
                if(rt == null) {
                    dao.savePointValueAsync(vo, pvt, null);
                    //Try for next value to see if the point is enabled now
                    rt = Common.runtimeManager.getDataPoint(vo.getId());
                }else {
                    rt.savePointValueDirectToCache(pvt, null, true, true, fireEvents);
                }
                totalProcessed++;
            }else {
                totalSkipped++;
            }
        }
    }

    @ApiOperation(
            value = "Delete Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/delete")
    @Async
    public CompletableFuture<List<PointValueTimeDeleteResult>> deletePointValues(
            @RequestBody  Stream<XidPointValueTimeModel> stream,
            @AuthenticationPrincipal PermissionHolder user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
                Map<String, PointValueTimeDelete> results = new HashMap<>();

                stream.forEach((pvt) ->{

                    results.compute(pvt.getXid(), (xidKey, entry) ->{
                        if(entry == null) {
                            entry = new PointValueTimeDelete(pvt.getXid(), pointValueDao, dataPointDao, user);
                        }
                        entry.deleteValue(pvt.getTimestamp());
                        return entry;
                    });

                });

                return results.values().stream().map((v) -> {
                    return new PointValueTimeDeleteResult(v.xid, v.totalProcessed, v.totalSkipped, v.result);
                }).collect(Collectors.toList());
            } catch(Exception e) {
                throw new CompletionException(e);
            }
        });

    }

    class PointValueTimeDelete extends PointValueTimeImport {

        public PointValueTimeDelete(String xid, PointValueDao dao, DataPointDao dataPointDao, PermissionHolder user) {
            super(xid, dao, dataPointDao, null, user);
        }

        public void deleteValue(ZonedDateTime timestamp) {
            if(valid && timestamp != null) {
                totalProcessed += Common.runtimeManager.purgeDataPointValue(vo, timestamp.toInstant().toEpochMilli(), dao);
            }else {
                totalSkipped++;
            }
        }
    }
}
