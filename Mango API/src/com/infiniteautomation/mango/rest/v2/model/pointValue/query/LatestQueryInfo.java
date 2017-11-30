/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.v2.model.RestValidationResult;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 *
 * @author Terry Packer
 */
public class LatestQueryInfo {
    
    protected ZoneId zoneId;
    protected ZonedDateTime from;

    protected final Integer limit;

    protected final boolean useRendered;
    protected final boolean multiplePointsPerArray;

    protected final boolean singleArray;
    protected final PointValueTimeCacheControl useCache;

    protected final String noDataMessage;
    protected final UriComponentsBuilder imageServletBuilder;
    protected final DateTimeFormatter dateTimeFormatter; // Write a timestamp or string date

    //TODO Remove when simplify is fully implemented
    public LatestQueryInfo(String host, int port, ZonedDateTime from, String dateTimeFormat, String timezone, 
            Integer limit, boolean useRendered, 
            boolean multiplePointsPerArray, boolean singleArray, PointValueTimeCacheControl useCache) {
        this(host, port, from, dateTimeFormat, timezone, limit, useRendered,
                multiplePointsPerArray, singleArray, useCache, null, null, false);
    }
    
    
    public LatestQueryInfo(String host, int port, ZonedDateTime from, String dateTimeFormat, String timezone, 
            Integer limit, boolean useRendered, 
            boolean multiplePointsPerArray, boolean singleArray, PointValueTimeCacheControl useCache, 
            Double simplifyTolerance, Integer simplifyTarget, boolean simplifyHighQuality) {
        
        // Quick validation
        validateTimezone(timezone);
        validateDateTimeFormat(dateTimeFormat);
        
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
            long current = Common.timer.currentTimeMillis();
            this.from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);
        }
        

        this.limit = limit;
        
        this.noDataMessage = new TranslatableMessage("common.stats.noDataForPeriod")
                .translate(Common.getTranslations());

        // If we are an image type we should build the URLS
        imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/hst{ts}_{id}.jpg");
        if (Common.envProps.getBoolean("ssl.on", false))
            imageServletBuilder.scheme("https");
        else
            imageServletBuilder.scheme("http");
        imageServletBuilder.host(host);
        imageServletBuilder.port(port);

        if (dateTimeFormat != null)
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        else
            this.dateTimeFormatter = null;

        this.useRendered = useRendered;
        this.multiplePointsPerArray = multiplePointsPerArray;
        this.singleArray = singleArray;
        this.useCache = useCache;
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

    public boolean isUseRendered() {
        return useRendered;
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
    
    /**
     * Write a link to an image based on data point id and timestamp
     * 
     * @param timestamp
     * @param id
     * @return
     */
    public String writeImageLink(long timestamp, int id) {
        return imageServletBuilder.buildAndExpand(timestamp, id).toUri().toString();
    }

    /**
     * Return an rendered string representation of the value
     * 
     * @param vo
     * @param pvt
     * @return
     */
    public String getRenderedString(DataPointVO vo, PointValueTime pvt) {
        return Functions.getRenderedText(vo, pvt);
    }

    public String getRenderedString(DataPointVO vo, Double value) {
        if (vo == null)
            return "-";
        if (value == null)
            return "-";
        return vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL);
    }

    /**
     * Return an rendered string representation of the integral
     * 
     * @param vo
     * @param integral
     * @return
     */
    public String getIntegralString(DataPointVO vo, Double integral) {
        return Functions.getIntegralText(vo, integral);
    }

    /**
     * Generate a Date Time String using our time zone
     * 
     * @param timestamp
     * @return
     */
    public String getDateTimeString(long timestamp) {
        return dateTimeFormatter
                .format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId));
    }
    
    public static void validateTimezone(String timezone) throws ValidationFailedRestException {
        if (timezone != null) {
            try {
                ZoneId.of(timezone);
            } catch (Exception e) {
                RestValidationResult vr = new RestValidationResult();
                vr.addError("validate.invalidValue", "timezone");
                throw new ValidationFailedRestException(vr);
            }
        }
    }

    public static void validateDateTimeFormat(String dateTimeFormat)
            throws ValidationFailedRestException {
        if (dateTimeFormat != null) {
            try {
                DateTimeFormatter.ofPattern(dateTimeFormat);
            } catch (IllegalArgumentException e) {
                RestValidationResult vr = new RestValidationResult();
                vr.addError("validate.invalid", "dateTimeFormat");
                throw new ValidationFailedRestException(vr);
            }
        }
    }
}
