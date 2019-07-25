/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeExportQueryModel;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeExportStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeImportResult;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeImportStream;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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

    @Autowired
    public PointValueModificationRestController(DataPointDao dataPointDao) {
        this.dataPointDao = dataPointDao;
    }

    @ApiOperation(
            value = "Export Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have read access"
            )
    @RequestMapping(method = RequestMethod.POST, value="/export")
    @Async
    public PointValueTimeExportStream exportPointValues(
            @RequestBody  PointValueTimeExportQueryModel model,
            @AuthenticationPrincipal User user) {
        
        //TODO Permissions/Validation?
        return null;
    }

    @ApiOperation(
            value = "Import Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
            )
    @RequestMapping(method = RequestMethod.POST, value="/import")
    @Async
    public List<PointValueTimeImportResult> importPointValues(
            @RequestBody PointValueTimeImportStream stream,
            @AuthenticationPrincipal User user) {
        
        PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
        Map<String, PointValueTimeImport> results = new HashMap<>();
        
        stream.accept((pvt) ->{
            
            results.compute(pvt.getXid(), (xidKey, entry) ->{
                if(entry == null) {
                    entry = new PointValueTimeImport(pvt.getXid(), pointValueDao, dataPointDao, user);
                }
                entry.saveValue(pvt.getValue(), pvt.getTimestamp(), pvt.getAnnotation());
                return entry;
            });
            
        }, (error) ->{
            //TODO select best exception type based on what error is
            throw new ServerErrorException(error);
        });
        
        return results.values().stream().map((v) -> { 
            return new PointValueTimeImportResult(v.xid, v.total, v.result);
        }).collect(Collectors.toList());
    }
    
    class PointValueTimeImport {
        
        private String xid;
        private int total;
        private ProcessResult result; 
        
        private final boolean valid;
        private final PointValueDao dao;
        private DataPointRT rt;
        private final DataPointVO vo;
        private final int dataTypeId;
        
        public PointValueTimeImport(String xid, PointValueDao dao, DataPointDao dataPointDao, User user) {
            this.xid = xid;
            this.result = new ProcessResult();
            this.dao = dao;
            this.vo = dataPointDao.getByXid(xid);
            if(vo == null) {
                valid = false;
                dataTypeId = DataTypes.UNKNOWN;
                result.addContextualMessage("xid", "emport.error.missingPoint", xid);
            }else {
                if (Permissions.hasDataPointSetPermission(user, vo)){
                    valid = true;
                    rt = Common.runtimeManager.getDataPoint(vo.getId());
                    dataTypeId = vo.getPointLocator().getDataTypeId();
                }else {
                    valid = false;
                    dataTypeId = DataTypes.UNKNOWN;
                    result.addContextualMessage("xid", "permission.exception.setDataPoint", user.getUsername());
                }
            }
        }
        
        public void saveValue(Object value, ZonedDateTime date, String annotation) {
            if(valid && value != null) {
                
                //Validate the model against our point
                long timestamp;
                if(date == null || date.toInstant().toEpochMilli() == 0) {
                    timestamp = Common.timer.currentTimeMillis();
                }else {
                    timestamp = date.toInstant().toEpochMilli();
                }
                
                //TODO Better checking of types and error handling
                DataValue dataValue = null;
                try {
                    switch(dataTypeId) {
                        case DataTypes.ALPHANUMERIC:
                            value = new AlphanumericValue(value.toString());
                            break;
                        case DataTypes.BINARY:
                            if(value instanceof String) {
                                value = new BinaryValue(Boolean.valueOf((String)value));
                            }else {
                                value = new BinaryValue((Boolean)value);
                            }
                            break;
                        case DataTypes.MULTISTATE:
                            if(value instanceof String) {
                                try {
                                    value = new MultistateValue(Integer.parseInt((String)value));
                                }catch(NumberFormatException ex) {
                                    try {
                                        value = vo.getTextRenderer().parseText((String) value, dataTypeId);
                                    } catch (Exception e) {
                                        // Lots can go wrong here so let the user know
                                        result.addContextualMessage("value", "event.valueParse.textParse", e.getMessage());
                                        return;
                                    }
                                }
                            }else {
                                value = new MultistateValue(((Number)value).intValue());
                            }
                            break;
                        case DataTypes.NUMERIC:
                            if(value instanceof String) {
                                value =  new NumericValue(Double.valueOf((String)value));
                            }else {
                                value = new NumericValue(((Number)value).doubleValue());
                            }
                            break;
                        case DataTypes.IMAGE:
                        default:
                            //TODO Add translation
                            result.addContextualMessage("dataType", "common.default", "Image data type not supported yet");
                            return;
                    }
                }catch(Exception e) {
                    //TODO this could be dangerous maybe make invalid at this point?
                    //TODO Add translation
                    result.addContextualMessage("value", "common.default", e.getMessage());
                    return;
                }
                
                PointValueTime pvt;
                if(StringUtils.isEmpty(annotation)) {
                    pvt = new PointValueTime(dataValue, timestamp);
                }else {
                    pvt = new AnnotatedPointValueTime(dataValue, timestamp, new TranslatableMessage("common.default", annotation));
                }
                if(rt == null) {
                    dao.savePointValueAsync(vo.getId(), pvt, null);
                    //TODO Try for next value?
                    rt = Common.runtimeManager.getDataPoint(vo.getId());
                }else {
                    rt.savePointValueDirectToCache(pvt, null, true, true);
                }
                total++;
            }
        }
    }
}
