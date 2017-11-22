/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All
 *            rights reserved.
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
import com.infiniteautomation.mango.util.datetime.TruncateTimePeriodAdjuster;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 *
 * @author Terry Packer
 */
public class ZonedDateTimeRangeQueryInfo {

    private ZonedDateTime from;
    private ZonedDateTime to;
    private ZoneId zoneId;

    private RollupEnum rollup;
    private TimePeriod timePeriod;
    private Integer limit;

    private final boolean bookend; //Do we want virtual values at the to/from time if they don't already exist?
    private final boolean useRendered;
    private final boolean useXidAsFieldName;
    private final boolean ascending;
    private final boolean singleArray;

    protected final String noDataMessage;
    protected final UriComponentsBuilder imageServletBuilder;
    protected final DateTimeFormatter dateTimeFormatter; // Write a timestamp or string date

    /**
     * This class with use an optional timzone to ensure that the to/from dates are correct and
     * attempt to determine the timezone to use for rendering and rollup edges using the following
     * rules:
     * 
     * if 'timezone' is supplied use that for all timezones if 'timezone' is not supplied the rules
     * are applied in this order: use timezone of from if not null use timezone of to if not null
     * use server timezone 
     * 
     * @param host
     * @param port
     * @param from
     * @param to
     * @param dateTimeFormat
     * @param timezone
     * @param rollup
     * @param timePeriod
     * @param limit
     * @param ascending
     * @param useRendered
     * @param useXidAsFieldName
     * @param singleArray
     */
    public ZonedDateTimeRangeQueryInfo(String host, int port, ZonedDateTime from, ZonedDateTime to,
            String dateTimeFormat, String timezone, RollupEnum rollup, TimePeriod timePeriod,
            Integer limit, boolean ascending, boolean bookend, boolean useRendered, 
            boolean useXidAsFieldName, boolean singleArray) {

        // Quick validation
        validateTimezone(timezone);
        validateDateTimeFormat(dateTimeFormat);

        // Determine the timezone to use
        if (timezone == null) {
            if (from != null) {
                this.zoneId = from.getZone();
            } else if (to != null)
                this.zoneId = to.getZone();
            else
                this.zoneId = TimeZone.getDefault().toZoneId();
        } else {
            this.zoneId = ZoneId.of(timezone);
        }
        this.rollup = rollup;
        this.timePeriod = timePeriod;
        this.limit = limit;

        // Set the timezone on the from and to dates
        long current = Common.timer.currentTimeMillis();
        if (from != null)
            this.from = from.withZoneSameInstant(zoneId);
        else
            this.from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);
        if (to != null)
            this.to = to.withZoneSameInstant(zoneId);
        else
            this.to = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);

        // Validate time
        if (!this.to.isAfter(this.from)) {
            RestValidationResult vr = new RestValidationResult();
            vr.addError("validate.invalidValue", "from");
            vr.addError("validate.invalidValue", "to");
            throw new ValidationFailedRestException(vr);
        }

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
        this.useXidAsFieldName = useXidAsFieldName;
        this.ascending = ascending;
        this.bookend = bookend;
        this.singleArray = singleArray;

    }

    /**
     * Round off the period for rollups
     */
    public void setupDates() {
        // Round off the period if we are using periodic rollup
        if (this.timePeriod != null) {
            TruncateTimePeriodAdjuster adj = new TruncateTimePeriodAdjuster(
                    TimePeriodType.convertFrom(this.timePeriod.getType()),
                    this.timePeriod.getPeriods());
            from = from.with(adj);
            to = to.with(adj);
        }
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
    
    public long getFromMillis() {
        return from.toInstant().toEpochMilli();
    }

    public long getToMillis() {
        return to.toInstant().toEpochMilli();
    }
    
    public ZonedDateTime getFrom() {
        return from;
    }

    public void setFrom(ZonedDateTime from) {
        this.from = from;
    }

    public ZonedDateTime getTo() {
        return to;
    }

    public void setTo(ZonedDateTime to) {
        this.to = to;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public RollupEnum getRollup() {
        return rollup;
    }

    public void setRollup(RollupEnum rollup) {
        this.rollup = rollup;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean isUseRendered() {
        return useRendered;
    }

    public boolean isUseXidAsFieldName() {
        return useXidAsFieldName;
    }
    
    public boolean isAscending() {
        return ascending;
    }
    
    public boolean isSingleArray() {
        return singleArray;
    }
    
    public boolean isBookend() {
        return bookend;
    }
}
