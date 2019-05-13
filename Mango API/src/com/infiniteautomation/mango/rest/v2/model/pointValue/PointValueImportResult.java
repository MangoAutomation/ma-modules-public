/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.m2m2.Common;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.DataTypeEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeModel;

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
    private final User user;
    @JsonIgnore
    private DataPointRT rt;
    @JsonIgnore
    private DataPointVO vo;
    
    public PointValueImportResult(String xid, PointValueDao dao, User user) {
        this.xid = xid;
        this.result = new ProcessResult();
        this.dao = dao;
        this.user = user;
        vo = DataPointDao.getInstance().getByXid(xid);
        if(vo == null) {
            valid = false;
            result.addContextualMessage("xid", "emport.error.missingPoint", xid);
        }else {
            if (Permissions.hasDataPointSetPermission(user, vo)){
                valid = true;
                rt = Common.runtimeManager.getDataPoint(vo.getId());
            }else {
                valid = false;
                result.addContextualMessage("xid", "permission.exception.setDataPoint", user.getUsername());
            }
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
    
    public void saveValue(XidPointValueTimeModel model) {
        if(valid) {
            
            //Validate the model against our point
            long timestamp = model.getTimestamp();
            if(timestamp == 0)
                timestamp = Common.timer.currentTimeMillis();
            if ((model.getType() == null ) || (DataTypeEnum.convertFrom(model.getType()) != vo.getPointLocator().getDataTypeId())) {
                result.addContextualMessage("dataType", "event.ds.dataType");
                return;
            }
            
            DataValue value;
            switch(model.getType()) {
                case ALPHANUMERIC:
                    value = new AlphanumericValue((String) model.getValue());
                    break;
                case BINARY:
                    value = new BinaryValue((Boolean)model.getValue());
                    break;
                case MULTISTATE:
                    if(model.getValue() instanceof String) {
                        try {
                            int dataTypeId = DataTypeEnum.convertFrom(model.getType());
                            value = vo.getTextRenderer().parseText((String) model.getValue(), dataTypeId);
                        } catch (Exception e) {
                            // Lots can go wrong here so let the user know
                            result.addContextualMessage("value", "event.valueParse.textParse", e.getMessage());
                            return;
                        }
                    }else {
                        value = new MultistateValue(((Number)model.getValue()).intValue());
                    }
                    break;
                case NUMERIC:
                    value = new NumericValue(((Number)model.getValue()).doubleValue());
                    break;
                case IMAGE:
                default:
                    result.addContextualMessage("dataType", "common.default", model.getType() + " data type not supported yet");
                    return;
            }
            
            PointValueTime pvt;
            if(model.getAnnotation() == null) {
                pvt = new PointValueTime(value, timestamp);
            }else {
                pvt = new AnnotatedPointValueTime(value, timestamp, new TranslatableMessage("common.default", model.getAnnotation()));
            }
            if(rt == null) {
                dao.savePointValueAsync(vo.getId(), pvt, null, null);
            }else {
                rt.savePointValueDirectToCache(pvt, null, true, true);
            }
            total++;
        }
    }
}
