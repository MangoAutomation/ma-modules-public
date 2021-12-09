/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.util.Functions;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;


/**
 *
 * @author Terry Packer
 */
public class LatestQueryInfo {

    protected ZoneId zoneId;
    protected ZonedDateTime from;

    protected final Integer limit;

    protected final boolean multiplePointsPerArray;

    protected final boolean singleArray;
    protected final PointValueTimeCacheControl useCache;

    protected final String noDataMessage;
    protected final DateTimeFormatter dateTimeFormatter; // Write a timestamp or string date

    protected final Double simplifyTolerance;
    protected final Integer simplifyTarget;
    protected final boolean simplifyHighQuality = true; //Currently not in api
    protected final boolean simplifyPrePostProcess = true; //Not in api

    protected final PointValueField[] fields;

    public LatestQueryInfo(ZonedDateTime from, String dateTimeFormat, String timezone,
            Integer limit, boolean multiplePointsPerArray, boolean singleArray, PointValueTimeCacheControl useCache,
            Double simplifyTolerance, Integer simplifyTarget, PointValueField[] fields) {

        // Quick validation
        ensureTimezone(timezone);
        ensureDateTimeFormat(dateTimeFormat);

        // Determine the timezone to use
        if (timezone == null) {
            if (from != null)
                this.zoneId = from.getZone();
            else
                this.zoneId = TimeZone.getDefault().toZoneId();
        } else {
            this.zoneId = ZoneId.of(timezone);
        }

        if (from != null)
            this.from = from.withZoneSameInstant(zoneId);
        else {
            long current = Common.timer.currentTimeMillis() + SystemSettingsDao.getInstance().getFutureDateLimit();
            this.from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);
        }


        this.limit = limit;

        this.noDataMessage = new TranslatableMessage("common.stats.noDataForPeriod")
                .translate(Common.getTranslations());

        if (dateTimeFormat != null)
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        else
            this.dateTimeFormatter = null;

        this.multiplePointsPerArray = multiplePointsPerArray;
        this.singleArray = singleArray;
        this.useCache = useCache;

        this.simplifyTolerance = simplifyTolerance;
        this.simplifyTarget = simplifyTarget;

        if(fields != null)
            this.fields = fields;
        else {
            this.fields = new PointValueField[]{ PointValueField.TIMESTAMP, PointValueField.VALUE};
        }
    }


    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Integer getLimit() {
        return limit;
    }

    public boolean isMultiplePointsPerArray() {
        return multiplePointsPerArray;
    }

    public boolean isSingleArray() {
        return singleArray;
    }

    public PointValueTimeCacheControl isUseCache() {
        return useCache;
    }

    public long getFromMillis() {
        return from.toInstant().toEpochMilli();
    }

    public ZonedDateTime getFrom() {
        return from;
    }

    public RollupEnum getRollup() {
        return RollupEnum.NONE;
    }

    public PointValueField[] getFields() {
        return this.fields;
    }

    public String getNoDataMessage() {
        return noDataMessage;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public boolean isUseSimplify() {
        return simplifyTolerance != null || simplifyTarget != null;
    }

    /**
     * Return an rendered string representation of the value
     *
     */
    public String getRenderedString(DataPointVO vo, PointValueTime pvt) {
        return Functions.getRenderedText(vo, pvt);
    }

    /**
     * Render a double value using the data point's text renderer properties
     */
    public String getRenderedString(DataPointVO vo, Double value) {
        if (vo == null)
            return "-";
        if (value == null)
            return "-";
        return vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL);
    }

    /**
     */
    public String getRenderedString(DataPointVO vo, DataValue value) {
        if(value == null)
            return "-";
        else
            return vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL);
    }

    /**
     * Return an rendered string representation of the integral
     *
     */
    public String getIntegralString(DataPointVO vo, Double integral) {
        return Functions.getIntegralText(vo, integral);
    }

    /**
     * Generate a Date Time String using our time zone
     *
     */
    public String getDateTimeString(long timestamp) {
        return dateTimeFormatter
                .format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId));
    }

    public static void ensureTimezone(String timezone) throws ValidationFailedRestException {
        if (timezone != null) {
            try {
                ZoneId.of(timezone);
            } catch (Exception e) {
                throw new BadRequestException(new TranslatableMessage("validate.invalidValueForField", "timezone"));
            }
        }
    }

    public static void ensureDateTimeFormat(String dateTimeFormat)
            throws ValidationFailedRestException {
        if (dateTimeFormat != null) {
            try {
                DateTimeFormatter.ofPattern(dateTimeFormat);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(new TranslatableMessage("validate.invalidValueForField", "dateTimeFormat"));
            }
        }
    }


    public boolean fieldsContains(PointValueField toFind) {
        for(PointValueField field : fields)
            if(field == toFind)
                return true;
        return false;
    }
}
