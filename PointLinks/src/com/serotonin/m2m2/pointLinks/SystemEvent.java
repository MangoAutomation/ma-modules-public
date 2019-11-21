/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.SystemEventTypeDefinition;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.web.taglib.Functions;

public class SystemEvent extends SystemEventTypeDefinition {
    public static final String TYPE_NAME = "POINT_LINK_FAILURE";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.system.pointLink";
    }

    @Override
    public String getEventListLink(int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("events.editPointLink"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='point_links.shtm?plid=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append("/" + Constants.DIR_MODULES + "/" + getModule().getName()).append("/web/link.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public boolean supportsReferenceId1() {
        return true;
    }

    @Override
    public boolean supportsReferenceId2() {
        return false;
    }
    
    @Override
    public List<EventTypeVO> generatePossibleEventTypesWithReferenceId1(PermissionHolder user, String subtype) {
        if(!StringUtils.equals(TYPE_NAME, subtype) || !user.hasAdminPermission())
            return Collections.emptyList();
        
        List<PointLinkVO> links = PointLinkDao.getInstance().getAll();
        List<EventTypeVO> types = new ArrayList<>(links.size());
        AlarmLevels level = AlarmLevels.fromValue(SystemSettingsDao.instance.getIntValue(SystemEventType.SYSTEM_SETTINGS_PREFIX + TYPE_NAME));

        for(PointLinkVO link : links)
            types.add(new EventTypeVO(new SystemEventType(TYPE_NAME, link.getId()), new TranslatableMessage("event.system.pointLinkSpecific", link.getName()), level));
        
        return types;
    }

}
