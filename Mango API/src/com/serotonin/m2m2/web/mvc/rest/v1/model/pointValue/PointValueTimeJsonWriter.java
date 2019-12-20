/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.util.Functions;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonWriter extends PointValueTimeWriter {

    protected final String TIMESTAMP = "timestamp";
    protected final String ROLLUP = "rollup";
    protected final String VALUE = "value";
    protected final String ANNOTATION = "annotation";

    protected JsonGenerator jgen;

    public PointValueTimeJsonWriter(JsonGenerator jgen, boolean useRendered,
            boolean unitConversion, String dateTimeFormat, String timezone) {
        super(useRendered, unitConversion, dateTimeFormat, timezone);
        this.jgen = jgen;
    }

    @Override
    public void writePointValueTime(double value, long timestamp, String annotation, DataPointVO vo)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(ANNOTATION, annotation);
        jgen.writeNumberField(VALUE, value);
        writeTimestamp(timestamp);
        jgen.writeEndObject();
    }

    @Override
    public void writePointValueTime(int value, long timestamp, String annotation, DataPointVO vo)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(ANNOTATION, annotation);
        jgen.writeNumberField(VALUE, value);
        writeTimestamp(timestamp);
        jgen.writeEndObject();
    }

    @Override
    public void writePointValueTime(String string, long timestamp, String annotation,
            DataPointVO vo) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(ANNOTATION, annotation);
        jgen.writeStringField(VALUE, string);
        writeTimestamp(timestamp);
        jgen.writeEndObject();
    }

    @Override
    public void writePointValueTime(DataValue value, long timestamp, String annotation,
            DataPointVO vo) throws IOException {

        jgen.writeStartObject();

        if (value == null) {
            jgen.writeNullField(VALUE);
        } else {
            switch (value.getDataType()) {
                case DataTypes.ALPHANUMERIC:
                    jgen.writeStringField(VALUE, value.getStringValue());
                    break;
                case DataTypes.BINARY:
                    jgen.writeBooleanField(VALUE, value.getBooleanValue());
                    break;
                case DataTypes.MULTISTATE:
                    jgen.writeNumberField(VALUE, value.getIntegerValue());
                    break;
                case DataTypes.NUMERIC:
                    jgen.writeNumberField(VALUE, value.getDoubleValue());
                    break;
                case DataTypes.IMAGE:
                    jgen.writeStringField(VALUE, imageServletBuilder
                            .buildAndExpand(timestamp, vo.getId()).toUri().toString());
                    break;
            }
        }

        writeTimestamp(timestamp);
        jgen.writeStringField(ANNOTATION, annotation);

        jgen.writeEndObject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeAllStatistics(
     * com.serotonin.m2m2.view.stats.ValueChangeCounter, com.serotonin.m2m2.vo.DataPointVO)
     */
    @Override
    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo)
            throws IOException {

        this.jgen.writeStartObject();
        writeTimestamp(statisticsGenerator.getPeriodStartTime());

        if (statisticsGenerator instanceof ValueChangeCounter) {
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;

            if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                this.writeImageValue(stats.getStartValue(), stats.getPeriodStartTime(),
                        stats.getPeriodStartTime(), vo, RollupEnum.START.name());
            else
                this.writeDataValue(stats.getPeriodStartTime(), stats.getStartValue(), vo,
                        RollupEnum.START.name());

            if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                this.writeImageValue(stats.getFirstValue(), stats.getFirstTime(),
                        stats.getPeriodStartTime(), vo, RollupEnum.FIRST.name());
            else
                this.writeDataValue(stats.getPeriodStartTime(), stats.getFirstValue(), vo,
                        RollupEnum.FIRST.name());

            if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                this.writeImageValue(stats.getLastValue(), stats.getLastTime(),
                        stats.getPeriodStartTime(), vo, RollupEnum.LAST.name());
            else
                this.writeDataValue(stats.getPeriodStartTime(), stats.getLastValue(), vo,
                        RollupEnum.LAST.name());
            this.jgen.writeNumberField(RollupEnum.COUNT.name(), stats.getCount());
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            this.writeDouble(stats.getAverage(), vo, RollupEnum.AVERAGE.name());
            this.writeDouble(stats.getDelta(), vo, RollupEnum.DELTA.name());
            this.writeDouble(stats.getMinimumValue(), vo, RollupEnum.MINIMUM.name());
            this.writeDouble(stats.getMaximumValue(), vo, RollupEnum.MAXIMUM.name());
            Double acc = stats.getLastValue();
            if (acc == null) {
                acc = stats.getMaximumValue();
            }
            this.writeDouble(acc, vo, RollupEnum.ACCUMULATOR.name());
            this.writeDouble(stats.getSum(), vo, RollupEnum.SUM.name());
            this.writeDouble(stats.getStartValue(), vo, RollupEnum.START.name());
            this.writeDouble(stats.getFirstValue(), vo, RollupEnum.FIRST.name());
            this.writeDouble(stats.getLastValue(), vo, RollupEnum.LAST.name());
            this.writeIntegral(stats.getIntegral(), vo, RollupEnum.INTEGRAL.name());
            this.jgen.writeNumberField(RollupEnum.COUNT.name(), stats.getCount());
        }
        this.jgen.writeEndObject();
    }

    protected void writeImageValue(DataValue value, Long imageTimestamp, long periodTimestamp,
            DataPointVO vo, String name) throws IOException {
        if (value == null) {
            this.jgen.writeNullField(name);
        } else {
            writeDataValue(name, value, imageTimestamp, vo);
        }
    }

    /**
     * @param xid
     * @param firstValue
     * @throws IOException
     */
    protected void writeDataValue(long timestamp, DataValue value, DataPointVO vo, String name)
            throws IOException {

        if (value == null) {
            this.jgen.writeNullField(name);
        } else {
            if (useRendered) {
                this.jgen.writeStringField(name,
                        vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL));
            } else if (unitConversion) {
                // Convert Value, must be numeric
                if (value instanceof NumericValue)
                    this.jgen.writeNumberField(name, vo.getUnit()
                            .getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()));
                else
                    this.writeDataValue(name, value, timestamp, vo);
            } else {
                this.writeDataValue(name, value, timestamp, vo);
            }
        }
    }

    /**
     * Only to be called via the other write methods
     * 
     * @param name
     * @param value
     * @throws IOException
     */
    private void writeDataValue(String name, DataValue value, long timestamp, DataPointVO vo)
            throws IOException {
        switch (value.getDataType()) {
            case DataTypes.ALPHANUMERIC:
                this.jgen.writeStringField(name, value.getStringValue());
                break;
            case DataTypes.BINARY:
                this.jgen.writeBooleanField(name, value.getBooleanValue());
                break;
            case DataTypes.MULTISTATE:
                this.jgen.writeNumberField(name, value.getIntegerValue());
                break;
            case DataTypes.NUMERIC:
                this.jgen.writeNumberField(name, value.getDoubleValue());
                break;
            case DataTypes.IMAGE:
                jgen.writeStringField(name, imageServletBuilder
                        .buildAndExpand(timestamp, vo.getId()).toUri().toString());
                break;
        }
    }

    protected void writeDouble(Double value, DataPointVO vo, String name) throws IOException {
        if (value == null) {
            this.jgen.writeNullField(name);
        } else {
            if (useRendered) {
                this.jgen.writeStringField(name,
                        vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL));
            } else if (unitConversion) {
                this.jgen.writeNumberField(name,
                        vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value));
            } else {
                this.jgen.writeNumberField(name, value);
            }
        }
    }

    /**
     * @param integral
     * @param vo
     * @throws IOException
     */
    protected void writeIntegral(Double integral, DataPointVO vo, String name) throws IOException {
        if (integral == null) {
            this.jgen.writeNullField(name);
        } else {
            if (useRendered) {
                this.jgen.writeStringField(name, Functions.getIntegralText(vo, integral));
            } else {
                this.jgen.writeNumberField(name, integral);
            }
        }
    }

    protected void writeTimestamp(long timestamp) throws IOException {
        if (dateFormatter == null)
            jgen.writeNumberField(TIMESTAMP, timestamp);
        else
            jgen.writeStringField(TIMESTAMP, writeTimestampString(timestamp));
    }
}
