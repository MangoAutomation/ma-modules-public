/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;

/**
 *
 * TODO This model needs a cleanup
 * @author Terry Packer
 *
 */
public class PointValueTimeModel {


    private DataTypeEnum dataType;
    private Object value;
    private long timestamp;
    private TranslatableMessage annotation;

    public PointValueTimeModel(){

    }
    /**
     *
     *
     * @param data - PointValueTime object
     */
    public PointValueTimeModel(PointValueTime data) {

        this.dataType = DataTypeEnum.convertTo(data.getValue().getDataType());
        if(dataType != DataTypeEnum.IMAGE){
            this.value = data.getValue().getObjectValue();
        }
        this.timestamp = data.getTime();

        if(data instanceof IAnnotated)
            this.annotation = ((IAnnotated) data).getSourceMessage();

    }


    public DataTypeEnum getDataType() {
        return dataType;
    }
    public void setDataType(DataTypeEnum type) {
        this.dataType = type;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public TranslatableMessage getAnnotation() {
        return annotation;
    }
    public void setAnnotation(TranslatableMessage annotation) {
        this.annotation = annotation;
    }

    public PointValueTime toVO(){
        DataValue dataValue = null;
        switch(this.dataType){
            case ALPHANUMERIC:
                dataValue = new AlphanumericValue((String) this.value);
                break;
            case BINARY:
                dataValue = new BinaryValue((Boolean)this.value);
                break;
            case MULTISTATE:
                dataValue = new MultistateValue(((Number)this.value).intValue());
                break;
            case NUMERIC:
                dataValue = new NumericValue(((Number)this.value).doubleValue());
                break;
            case IMAGE:
                throw new ShouldNeverHappenException("Importing Image values not supported");
        }

        if(this.annotation != null)
            return new AnnotatedPointValueTime(dataValue, this.timestamp, this.annotation);
        else
            return new PointValueTime(dataValue, this.timestamp);

    }
}
