/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
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
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;


/**
 *
 * @author Terry Packer
 */
public class PointValueImportResult {

    private String xid;
    private int total;
    private ProcessResult result;

    //Runtime members
    @JsonIgnore
    private boolean valid;
    @JsonIgnore
    private PointValueDao dao;
    @JsonIgnore
    private DataPointService service;
    @JsonIgnore
    private final FireEvents fireEvents;
    @JsonIgnore
    private final User user;
    @JsonIgnore
    private DataPointRT rt;
    @JsonIgnore
    private DataPointVO vo;

    public PointValueImportResult(String xid, PointValueDao dao, DataPointService service, FireEvents fireEvents, User user) {
        this.xid = xid;
        this.result = new ProcessResult();
        this.dao = dao;
        this.service = service;
        this.fireEvents = fireEvents;
        this.user = user;
        try {
            vo = service.get(xid);
            service.ensureSetPermission(user, vo);
            valid = true;
            rt = Common.runtimeManager.getDataPoint(vo.getId());
        }catch(NotFoundException e) {
            valid = false;
            result.addContextualMessage("xid", "emport.error.missingPoint", xid);
        }catch(PermissionException e) {
            valid = false;
            result.addContextualMessage("xid", "permission.exception.setDataPoint", user.getUsername());
        }
    }

    /**
     * @return the xid
     */
    public String getXid() {
        return xid;
    }

    /**
     * @param xid the xid to set
     */
    public void setXid(String xid) {
        this.xid = xid;
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * @return the result
     */
    public ProcessResult getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(ProcessResult result) {
        this.result = result;
    }

    public boolean isValid() {
        return valid;
    }

    public void saveValue(LegacyXidPointValueTimeModel model) {
        if(valid) {

            //Validate the model against our point
            long timestamp = model.getTimestamp();
            if(timestamp == 0)
                timestamp = Common.timer.currentTimeMillis();

            try {
                DataValue value;
                switch(vo.getPointLocator().getDataTypeId()) {
                    case DataTypes.ALPHANUMERIC:
                        value = new AlphanumericValue((String) model.getValue());
                        break;
                    case DataTypes.BINARY:
                        value = new BinaryValue((Boolean)model.getValue());
                        break;
                    case DataTypes.MULTISTATE:
                        if(model.getValue() instanceof String) {
                            try {
                                value = vo.getTextRenderer().parseText((String) model.getValue(), vo.getPointLocator().getDataTypeId());
                            } catch (Exception e) {
                                // Lots can go wrong here so let the user know
                                result.addContextualMessage("value", "event.valueParse.textParse", e.getMessage());
                                return;
                            }
                        }else {
                            value = new MultistateValue(((Number)model.getValue()).intValue());
                        }
                        break;
                    case DataTypes.NUMERIC:
                        value = new NumericValue(((Number)model.getValue()).doubleValue());
                        break;
                    case DataTypes.IMAGE:
                    default:
                        result.addContextualMessage("dataType", "common.default", vo.getPointLocator().getDataTypeId() + " data type not supported yet");
                        return;
                }

                PointValueTime pvt;
                if(model.getAnnotation() == null) {
                    pvt = new PointValueTime(value, timestamp);
                }else {
                    pvt = new AnnotatedPointValueTime(value, timestamp, new TranslatableMessage("common.default", model.getAnnotation()));
                }
                if(rt == null) {
                    dao.savePointValueAsync(vo, pvt, null);
                }else {
                    rt.savePointValueDirectToCache(pvt, null, true, true, fireEvents);
                }
                total++;
            }catch(Exception e) {
                if(e instanceof ClassCastException) {
                    result.addContextualMessage("dataType", "event.ds.dataType");
                }else {
                    result.addContextualMessage("value", "common.default", e.getMessage());
                }
            }
        }
    }
}
