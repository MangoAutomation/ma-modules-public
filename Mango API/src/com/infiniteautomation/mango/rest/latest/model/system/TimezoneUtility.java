/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * This class should not filter on continent and thus should not be used.
 *
 * @author Terry Packer
 *
 */
@Deprecated
public class TimezoneUtility {
    public static final String[] TZ_CONTINENTS = { "Africa", "America", "Antarctica", "Arctic", "Asia", "Atlantic",
            "Australia", "Europe", "Indian", "Pacific", };
    private static final String REGEX;
    static {
        StringBuilder sb = null;
        for (String continent : TZ_CONTINENTS) {
            if (sb == null)
                sb = new StringBuilder("^(");
            else
                sb.append("|");
            sb.append(continent);
        }
        sb.append(")/[^/]*");

        REGEX = sb.toString();
    }

    public static List<String> getTimeZoneIds() {
        List<String> result = new ArrayList<String>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            if (id.matches(REGEX))
                result.add(id);
        }

        Collections.sort(result);

        return result;
    }

    public static List<TimezoneModel> getTimeZoneIdsWithOffset() {

        List<TimezoneModel> models = new ArrayList<TimezoneModel>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            if (id.matches(REGEX)) {
                TimezoneModel model = new TimezoneModel(id, id, TimeZone.getTimeZone(id).getRawOffset());
                models.add(model);
            }
        }

        Collections.sort(models);
        return models;
    }

    public static String formatOffset(int offset) {
        if (offset == 0)
            return "(UTC)";

        String s = "";

        boolean minus = false;
        if (offset < 0) {
            minus = true;
            offset = -offset;
        }

        // Ignore millis and seconds
        offset /= 60000;
        s = ":" + StringUtils.leftPad(Integer.toString(offset % 60), 2, '0');

        offset /= 60;
        s = "(UTC" + (minus ? "-" : "+") + Integer.toString(offset) + s + ")";

        return s;
    }
}
