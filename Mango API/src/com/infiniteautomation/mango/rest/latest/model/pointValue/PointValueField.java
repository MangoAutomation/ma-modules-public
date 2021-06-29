/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.LatestQueryInfo;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;

/**
 * Information that can optionally be returned
 * in point value query results.
 * @author Terry Packer
 */
public enum PointValueField {

    VALUE("value"),
    TIMESTAMP("timestamp"),
    ANNOTATION("annotation"),
    CACHED("cached"),
    BOOKEND("bookend"),
    RENDERED("rendered"),
    RAW("raw"), //unconverted value if the point has a rendered unit

    XID("xid"),
    NAME("name"),
    DEVICE_NAME("deviceName"),
    DATA_SOURCE_NAME("dataSourceName");

    private final String fieldName;
    private PointValueField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    /**
     *
     * @param value
     * @param info
     * @param translations
     * @param useXid
     * @param writer
     * @throws IOException
     */
    public void writeValue(DataPointVOPointValueTimeBookend value, LatestQueryInfo info,
            Translations translations, boolean useXid, PointValueTimeWriter writer) throws IOException {
        switch(this) {
            case TIMESTAMP:
                writer.writeTimestamp(value.getPvt().getTime());
                break;
            case VALUE:
                if(useXid)
                    writer.writeDataValue(value.getVo().getXid(), value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), false, false);
                else
                    writer.writeDataValue(this.fieldName, value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), false, false);
                break;
            case RAW:
                if(useXid)
                    writer.writeDataValue(value.getVo().getXid(), value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), false, true);
                else
                    writer.writeDataValue(this.fieldName, value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), false, true);
                break;
            case ANNOTATION:
                if(value.getPvt() instanceof IAnnotated) {
                    if(useXid)
                        writer.writeStringField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, ((IAnnotated) value.getPvt()).getAnnotation(translations));
                    else
                        writer.writeStringField(this.fieldName, ((IAnnotated) value.getPvt()).getAnnotation(translations));
                }
                break;
            case BOOKEND:
                if(value.isBookend()) {
                    if(useXid)
                        writer.writeBooleanField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, true);
                    else
                        writer.writeBooleanField(this.fieldName, true);
                }
                break;
            case CACHED:
                if(value.isCached()) {
                    if(useXid)
                        writer.writeBooleanField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, true);
                    else
                        writer.writeBooleanField(this.fieldName, true);
                }
                break;
            case RENDERED:
                if(useXid)
                    writer.writeDataValue(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), true, false);
                else
                    writer.writeDataValue(this.fieldName, value.getVo(), value.getPvt().getValue(),value.getPvt().getTime(), true, false);
                break;
            case XID:
                if(useXid)
                    writer.writeStringField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, value.getVo().getXid());
                else
                    writer.writeStringField(this.fieldName, value.getVo().getXid());
                break;
            case NAME:
                if(useXid)
                    writer.writeStringField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, value.getVo().getName());
                else
                    writer.writeStringField(this.fieldName, value.getVo().getName());
                break;
            case DEVICE_NAME:
                if(useXid)
                    writer.writeStringField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, value.getVo().getDeviceName());
                else
                    writer.writeStringField(this.fieldName, value.getVo().getDeviceName());
                break;
            case DATA_SOURCE_NAME:
                if(useXid)
                    writer.writeStringField(value.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, value.getVo().getDataSourceName());
                else
                    writer.writeStringField(this.fieldName, value.getVo().getDataSourceName());
                break;
            default:
                throw new ShouldNeverHappenException("Unknown data point field.");
        }
    }

    /**
     * @param periodStats
     * @param info
     * @param translations
     * @param useXid
     * @param writer
     * @throws IOException
     */
    public void writeValue(DataPointStatisticsGenerator periodStats, LatestQueryInfo info,
            Translations translations, boolean useXid, PointValueTimeWriter writer) throws IOException {
        switch(this) {
            case TIMESTAMP:
                writer.writeTimestamp(periodStats.getGenerator().getPeriodStartTime());
                break;
            case VALUE:
                if(useXid)
                    writer.writeStatistic(periodStats.getVo().getXid(), periodStats.getGenerator(), periodStats.getVo(), false, false);
                else
                    writer.writeStatistic(this.fieldName, periodStats.getGenerator(), periodStats.getVo(), false, false);
                break;
            case RAW:
                if(useXid)
                    writer.writeStatistic(periodStats.getVo().getXid(), periodStats.getGenerator(), periodStats.getVo(), false, true);
                else
                    writer.writeStatistic(this.fieldName, periodStats.getGenerator(), periodStats.getVo(), false, true);
                break;
            case ANNOTATION:
                break;
            case BOOKEND:
                break;
            case CACHED:
                break;
            case RENDERED:
                if(useXid)
                    writer.writeStatistic(periodStats.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, periodStats.getGenerator(), periodStats.getVo(), true, false);
                else
                    writer.writeStatistic(this.fieldName, periodStats.getGenerator(), periodStats.getVo(), true, false);
                break;
            case XID:
                if(useXid)
                    writer.writeStringField(periodStats.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, periodStats.getVo().getXid());
                else
                    writer.writeStringField(this.fieldName, periodStats.getVo().getXid());
                break;
            case NAME:
                if(useXid)
                    writer.writeStringField(periodStats.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, periodStats.getVo().getName());
                else
                    writer.writeStringField(this.fieldName, periodStats.getVo().getName());
                break;
            case DEVICE_NAME:
                if(useXid)
                    writer.writeStringField(periodStats.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, periodStats.getVo().getDeviceName());
                else
                    writer.writeStringField(this.fieldName, periodStats.getVo().getDeviceName());
                break;
            case DATA_SOURCE_NAME:
                if(useXid)
                    writer.writeStringField(periodStats.getVo().getXid() + PointValueTimeWriter.DOT + this.fieldName, periodStats.getVo().getDataSourceName());
                else
                    writer.writeStringField(this.fieldName, periodStats.getVo().getDataSourceName());
                break;
            default:
                throw new ShouldNeverHappenException("Unknown data point field.");
        }
    }

    /**
     * @param builder
     */
    public void createColumn(Builder builder, String xid) {
        switch(this) {
            case TIMESTAMP:
                builder.addColumn(this.fieldName, ColumnType.NUMBER_OR_STRING);
                break;
            case VALUE:
                if(xid != null)
                    builder.addColumn(xid, ColumnType.NUMBER_OR_STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.NUMBER_OR_STRING);
                break;
            case RAW:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.NUMBER_OR_STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.NUMBER_OR_STRING);
                break;
            case ANNOTATION:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            case BOOKEND:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.BOOLEAN);
                else
                    builder.addColumn(this.fieldName, ColumnType.BOOLEAN);
                break;
            case CACHED:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.BOOLEAN);
                else
                    builder.addColumn(this.fieldName, ColumnType.BOOLEAN);
                break;
            case RENDERED:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            case XID:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            case NAME:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            case DEVICE_NAME:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            case DATA_SOURCE_NAME:
                if(xid != null)
                    builder.addColumn(xid + PointValueTimeWriter.DOT + this.fieldName, ColumnType.STRING);
                else
                    builder.addColumn(this.fieldName, ColumnType.STRING);
                break;
            default:
                throw new ShouldNeverHappenException("Unknown data point field.");
        }
    }
}
