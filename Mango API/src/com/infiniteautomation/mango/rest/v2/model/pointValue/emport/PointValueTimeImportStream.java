/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.emport;

import java.time.ZonedDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeImportStream.XidPointValueTime;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeImportStream implements BiConsumer<Consumer<XidPointValueTime>, Consumer<Throwable>>{

    /**
     * Consume a processed csv row and then supply each row for processing
     */
    private final BiConsumer<Consumer<XidPointValueTime>, Consumer<Throwable>> csvInputConsumer;
    
    public PointValueTimeImportStream(BiConsumer<Consumer<XidPointValueTime>, Consumer<Throwable>> csvInputConsumer) {
        this.csvInputConsumer = csvInputConsumer;
    }

    @Override
    public void accept(Consumer<XidPointValueTime> value, Consumer<Throwable> error) {
        this.csvInputConsumer.accept(value, error);
    }
    
    public static class XidPointValueTime {
        private String xid;
        private Object value;
        private ZonedDateTime timestamp;
        private String annotation;
        
        public String getXid() {
            return xid;
        }
        public void setXid(String xid) {
            this.xid = xid;
        }
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        public ZonedDateTime getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
        }
        public String getAnnotation() {
            return annotation;
        }
        public void setAnnotation(String annotation) {
            this.annotation = annotation;
        }
        @Override
        public String toString() {
            return xid + " - " + value + "@" + timestamp;
        }
    }
    
}
