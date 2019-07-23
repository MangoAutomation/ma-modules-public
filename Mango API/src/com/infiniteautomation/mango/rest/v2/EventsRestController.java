/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventSummaryModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventLevelSummaryModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.EventInstanceDao;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;

import io.swagger.annotations.Api;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Events endpoints")
@RestController()
@RequestMapping("/events")
public class EventsRestController {

    private final RestModelMapper modelMapper;
    private final EventInstanceDao eventDao;
    private final DataPointDao dataPointDao;
    //TODO Build the mappings for model fields to table columns
    //TODO Build the mappings for model fields to SQL statements
    
    @Autowired
    public EventsRestController(RestModelMapper modelMapper, EventInstanceDao eventDao, DataPointDao dataPointDao) {
        this.modelMapper = modelMapper;
        this.eventDao = eventDao;
        this.dataPointDao = dataPointDao;
    }
    
    public StreamedArrayWithTotal queryRQL(ASTNode rql, User user) {
        if(user.hasAdminPermission()){
            //admin users don't need to filter the results
            return new StreamedVOQueryWithTotal<>(this.eventDao, rql, (vo)-> {return modelMapper.map(vo, EventInstanceModel.class, user);});
        }else{
            //Add some restrictions for permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "userId", user.getId()));
            ConditionSortLimit conditions = this.eventDao.rqlToCondition(rql);
            //TODO Mango 3.7 add method to Dao to generate a condition instead of the above RQL Utils
            //conditions.addCondition(this.eventDao.userHasPermission(user));
            
            return new StreamedVOQueryWithTotal<>(this.eventDao, conditions, item -> true, (vo)-> {return modelMapper.map(vo, EventInstanceModel.class, user);});
        } 
    }
    
    /**
     * TODO Mango 3.7 Move to Service in Core
     * Get the active summary of events for a user
     * @param user
     * @return
     */
    public List<EventLevelSummaryModel> getActiveSummary(User user){
        List<EventLevelSummaryModel> list = new ArrayList<EventLevelSummaryModel>();

        //This query is slow the first time as it must fill the UserEventCache
        List<EventInstance> events = Common.eventManager.getAllActiveUserEvents(user.getId());
        int lifeSafetyTotal = 0;
        EventInstance lifeSafetyEvent = null;
        int criticalTotal = 0;
        EventInstance criticalEvent = null;
        int urgentTotal = 0;
        EventInstance urgentEvent = null;
        int warningTotal = 0;
        EventInstance warningEvent = null;
        int importantTotal = 0;
        EventInstance importantEvent = null;
        int informationTotal = 0;
        EventInstance informationEvent = null;
        int noneTotal = 0;
        EventInstance noneEvent = null;
        int doNotLogTotal = 0;
        EventInstance doNotLogEvent = null;

        for (EventInstance event : events) {
            switch (event.getAlarmLevel()) {
                case LIFE_SAFETY:
                    lifeSafetyTotal++;
                    lifeSafetyEvent = event;
                    break;
                case CRITICAL:
                    criticalTotal++;
                    criticalEvent = event;
                    break;
                case URGENT:
                    urgentTotal++;
                    urgentEvent = event;
                    break;
                case WARNING:
                    warningTotal++;
                    warningEvent = event;
                    break;
                case IMPORTANT:
                    importantTotal++;
                    importantEvent = event;
                    break;
                case INFORMATION:
                    informationTotal++;
                    informationEvent = event;
                    break;
                case NONE:
                    noneTotal++;
                    noneEvent = event;
                    break;
                case DO_NOT_LOG:
                    doNotLogTotal++;
                    doNotLogEvent = event;
                    break;
                case IGNORE:
                    break;
                default:
                    break;
            }
        }
        EventInstanceModel model;
        // Life Safety
        if (lifeSafetyEvent != null)
            model = modelMapper.map(lifeSafetyEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.LIFE_SAFETY,
                lifeSafetyTotal, model));
        // Critical Events
        if (criticalEvent != null)
            model = modelMapper.map(criticalEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.CRITICAL,
                criticalTotal, model));
        // Urgent Events
        if (urgentEvent != null)
            model = modelMapper.map(urgentEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.URGENT,
                urgentTotal, model));
        // Warning Events
        if (warningEvent != null)
            model = modelMapper.map(warningEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.WARNING,
                warningTotal, model));
        // Important Events
        if (importantEvent != null)
            model = modelMapper.map(importantEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.IMPORTANT,
                importantTotal, model));
        // Information Events
        if (informationEvent != null)
            model = modelMapper.map(informationEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.INFORMATION,
                informationTotal, model));
        // None Events
        if (noneEvent != null)
            model = modelMapper.map(noneEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.NONE,
                noneTotal, model));
        // Do Not Log Events
        if (doNotLogEvent != null)
            model = modelMapper.map(doNotLogEvent, EventInstanceModel.class, user);
        else
            model = null;
        list.add(new EventLevelSummaryModel(AlarmLevels.DO_NOT_LOG,
                doNotLogTotal, model));

        return list;
    }

    /**
     * TODO Mango 3.7 Move to Service in Core
     * @param dataPointXids
     * @param user
     * @return
     */
    public Collection<DataPointEventSummaryModel> getDataPointEventSummaries(String[] dataPointXids, User user) {
        Map<Integer, DataPointEventSummaryModel> map = new HashMap<>();
        //TODO Do we really want/need these checks?
        for(String xid : dataPointXids) {
            DataPointVO point = dataPointDao.getByXid(xid);
            if(point == null) {
                throw new NotFoundException();
            }
            Permissions.ensureDataPointReadPermission(user, point);
            map.put(point.getId(), new DataPointEventSummaryModel(xid));
        }
        
        List<EventInstance> events = Common.eventManager.getAllActiveUserEvents(user.getId());
        for(EventInstance event : events) {
            if(EventType.EventTypeNames.DATA_POINT.equals(event.getEventType().getEventType())) {
                DataPointEventSummaryModel model = map.get(event.getEventType().getReferenceId1());
                if(model != null) {
                    model.update(event);
                }
            }
        }
        return map.values();
    }
}
