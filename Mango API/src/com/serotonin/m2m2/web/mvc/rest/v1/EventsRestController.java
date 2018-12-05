/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.ComparisonEnum;
import com.infiniteautomation.mango.db.query.SQLQueryColumn;
import com.infiniteautomation.mango.db.query.appender.GenericSQLColumnQueryAppender;
import com.infiniteautomation.mango.db.query.appender.ReverseEnumColumnQueryAppender;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.EventInstanceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleQueryDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.type.EventType.EventTypeNames;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ModuleQueryExplainModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ModuleQueryModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.TranslatableMessageModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventLevelSummaryModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 *
 * @author Terry Packer
 *
 */
@Api(value="Events", description="Events")
@RestController()
@RequestMapping("/events")
public class EventsRestController extends MangoVoRestController<EventInstanceVO, EventInstanceModel, EventInstanceDao>{

    private static Log LOG = LogFactory.getLog(EventsRestController.class);

    public EventsRestController(){
        super(EventInstanceDao.getInstance());

        //Add in our mappings
        this.modelMap.put("eventType", "typeName");
        this.modelMap.put("referenceId1", "typeRef1");
        this.modelMap.put("referenceId2", "typeRef2");
        this.modelMap.put("dataPointId", "typeRef1");
        this.modelMap.put("active", "rtnTs");
        this.modelMap.put("acknowledged", "ackTs");

        this.appenders.put("alarmLevel", new ReverseEnumColumnQueryAppender<>(AlarmLevels.class));

        //If we query on the member active
        this.appenders.put("active", new GenericSQLColumnQueryAppender(){

            @Override
            public void appendSQL(SQLQueryColumn column,
                    StringBuilder selectSql, StringBuilder countSql,
                    List<Object> selectArgs, List<Object> columnArgs,
                    ComparisonEnum comparison) {

                if (columnArgs.size() == 0 || !(columnArgs.get(0) instanceof Boolean)) {
                    super.appendSQL(column, selectSql, countSql, selectArgs, columnArgs, comparison);
                    return;
                }

                Boolean condition = (Boolean)columnArgs.get(0);
                if(condition){
                    appendSQL(column.getName(), " IS ? ", selectSql, countSql);
                    selectArgs.add(null);
                }else{
                    appendSQL(column.getName(), " IS NOT ? ", selectSql, countSql);
                    selectArgs.add(null);
                }

                appendSQL("AND evt.rtnApplicable", EQUAL_TO_SQL, selectSql, countSql);
                selectArgs.add("Y");
            }

        });
        //If we query on the member acknowledged
        this.appenders.put("acknowledged", new GenericSQLColumnQueryAppender(){

            @Override
            public void appendSQL(SQLQueryColumn column,
                    StringBuilder selectSql, StringBuilder countSql,
                    List<Object> selectArgs, List<Object> columnArgs,
                    ComparisonEnum comparison) {

                if (columnArgs.size() == 0 || !(columnArgs.get(0) instanceof Boolean)) {
                    super.appendSQL(column, selectSql, countSql, selectArgs, columnArgs, comparison);
                    return;
                }

                Boolean condition = (Boolean)columnArgs.get(0);
                if(condition){
                    appendSQL(column.getName(), " IS NOT ? ", selectSql, countSql);
                    selectArgs.add(null);
                }else{
                    appendSQL(column.getName(), " IS ? ", selectSql, countSql);
                    selectArgs.add(null);
                }
            }

        });

        //Ensure we are querying on Type of Data Point for dataPointId queries
        this.appenders.put("dataPointId", new GenericSQLColumnQueryAppender(){

            @Override
            public void appendSQL(SQLQueryColumn column,
                    StringBuilder selectSql, StringBuilder countSql,
                    List<Object> selectArgs, List<Object> columnArgs,
                    ComparisonEnum comparison) {

                selectSql.append(" typeName = ? AND typeRef1 = ? ");
                countSql.append(" typeName = ? AND typeRef1 = ? ");
                selectArgs.add(EventTypeNames.DATA_POINT);
                selectArgs.add(columnArgs.get(0));
            }
        });
    }


    @ApiOperation(
            value = "Get all events",
            notes = "",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET, value="/list")
    public ResponseEntity<QueryArrayStream<EventInstanceVO>> getAll(HttpServletRequest request,
            @RequestParam(value="limit", required=false, defaultValue="100")Integer limit) {

        RestProcessResult<QueryArrayStream<EventInstanceVO>> result = new RestProcessResult<QueryArrayStream<EventInstanceVO>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            ASTNode root = new ASTNode("and", new ASTNode("eq", "userId", user.getId()), new ASTNode("limit", limit));
            return result.createResponseEntity(getPageStream(root));
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get event by ID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<EventInstanceModel> getById(
            @ApiParam(value = "Valid Event ID", required = true, allowMultiple = false)
            @PathVariable Integer id, HttpServletRequest request) {

        RestProcessResult<EventInstanceModel> result = new RestProcessResult<EventInstanceModel>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            EventInstanceVO vo = EventInstanceDao.getInstance().get(id);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            if(!Permissions.hasEventTypePermission(user, vo.getEventType())) {
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            EventInstanceModel model = new EventInstanceModel(vo);
            return result.createResponseEntity(model);
        }
        return result.createResponseEntity();
    }


    @ApiOperation(
            value = "Query Events",
            notes = "Query by posting AST Model",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.POST, value = "/query")
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> query(
            @ApiParam(value="Query", required=true)
            @RequestBody(required=true) ASTNode query,

            HttpServletRequest request) {

        RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            query = RQLUtils.addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
            return result.createResponseEntity(getPageStream(query));
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Query Events",
            notes = "Query via rql in url",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> queryRQL(HttpServletRequest request) {

        RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            try{
                //Parse the RQL Query
                ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
                query = RQLUtils.addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
                return result.createResponseEntity(getPageStream(query));
            }catch(InvalidRQLRestException e){
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    /**
     * Update an event
     * @param vo
     * @param xid
     * @param builder
     * @param request
     * @return
     */
    @ApiOperation(
            value = "Acknowledge an existing event",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/acknowledge/{id}")
    public ResponseEntity<EventInstanceModel> acknowledgeEvent(
            @PathVariable Integer id,
            @RequestBody(required=false) TranslatableMessageModel message,
            UriComponentsBuilder builder, HttpServletRequest request) {

        RestProcessResult<EventInstanceModel> result = new RestProcessResult<EventInstanceModel>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if (result.isOk()) {
            TranslatableMessage tlm = null;
            if (message != null)
                tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());

            EventInstance event = EventDao.getInstance().get(id);
            if (event == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            } else if(!Permissions.hasEventTypePermission(user, event.getEventType())) {
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            Common.eventManager.acknowledgeEventById(id, System.currentTimeMillis(), user, tlm);

            // if event has a different ack timestamp, user or message it was already acked, we could return a different message

            EventInstanceModel model = new EventInstanceModel(event);

            //Put a link to the updated data in the header?
            URI location = builder.path("/v1/events/{id}").buildAndExpand(id).toUri();
            result.addRestMessage(getResourceUpdatedMessage(location));
            return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }


    @ApiOperation(
            value = "Acknowledge many existing events",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.POST, value = "/acknowledge")
    public ResponseEntity<EventAcknowledgeQueryStream> acknowledgeManyEvents(
            @RequestBody(required=false) TranslatableMessageModel message,
            UriComponentsBuilder builder, HttpServletRequest request) {

        RestProcessResult<EventAcknowledgeQueryStream> result = new RestProcessResult<EventAcknowledgeQueryStream>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){

            //Parse the RQL Query
            ASTNode query;
            try {
                query = RQLUtils.parseRQLtoAST(request.getQueryString());
                query = RQLUtils.addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));

                TranslatableMessage tlm = null;
                if(message != null)
                    tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());

                //Perform the query and stream through acknowledger
                EventAcknowledgeQueryStreamCallback callback = new EventAcknowledgeQueryStreamCallback(user, tlm);
                EventAcknowledgeQueryStream stream = new EventAcknowledgeQueryStream(dao, this, query, callback);
                //Ensure its ready
                stream.setupQuery();

                return result.createResponseEntity(stream);
            } catch (InvalidRQLRestException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }

        }
        //Not logged in
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get the active events summary",
            notes = "List of counts for all active events by type and the most recent active alarm for each."
            )
    @RequestMapping(method = RequestMethod.GET, value = "/active-summary")
    public ResponseEntity<List<EventLevelSummaryModel>> getActiveSummary(
            HttpServletRequest request) {

        RestProcessResult<List<EventLevelSummaryModel>> result =
                new RestProcessResult<List<EventLevelSummaryModel>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if (result.isOk()) {
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
                model = new EventInstanceModel(lifeSafetyEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.LIFE_SAFETY,
                    lifeSafetyTotal, model));
            // Critical Events
            if (criticalEvent != null)
                model = new EventInstanceModel(criticalEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CRITICAL,
                    criticalTotal, model));
            // Urgent Events
            if (urgentEvent != null)
                model = new EventInstanceModel(urgentEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.URGENT,
                    urgentTotal, model));
            // Warning Events
            if (warningEvent != null)
                model = new EventInstanceModel(warningEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.WARNING,
                    warningTotal, model));
            // Important Events
            if (importantEvent != null)
                model = new EventInstanceModel(importantEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.IMPORTANT,
                    importantTotal, model));
            // Information Events
            if (informationEvent != null)
                model = new EventInstanceModel(informationEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.INFORMATION,
                    informationTotal, model));
            // None Events
            if (noneEvent != null)
                model = new EventInstanceModel(noneEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.NONE,
                    noneTotal, model));
            // Do Not Log Events
            if (doNotLogEvent != null)
                model = new EventInstanceModel(doNotLogEvent);
            else
                model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.DO_NOT_LOG,
                    doNotLogTotal, model));

            return result.createResponseEntity(list);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Query Events By Custom Module Defined Query",
            notes = "See explain-module-defined-queries for all options",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.POST, value = "/module-defined-query")
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> moduleDefinedQuery(
            @ApiParam(value="Query Payload", required=true)
            @RequestBody(required=true) ModuleQueryModel model,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException {
        model.ensureValid(user, this.dao.tableName);
        ASTNode query = model.createQuery(user);
        if(query == null)
            return ResponseEntity.ok(new QueryDataPageStream<EventInstanceVO>() {

                @Override
                public void streamData(JsonGenerator jgen) throws IOException { }

                @Override
                public void streamData(CSVPojoWriter<EventInstanceVO> writer) throws IOException { }

                @Override
                public void streamCount(JsonGenerator jgen) throws IOException {
                    jgen.writeNumber(0);
                }

                @Override
                public void streamCount(CSVPojoWriter<Long> writer) throws IOException {
                    writer.writeNext(0);
                }

            });
        else
            return ResponseEntity.ok(getPageStream(query));
    }

    @ApiOperation(
            value = "Explain all module defined queries for this controller",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value = "/explain-module-defined-queries")
    public ResponseEntity<List<ModuleQueryExplainModel>> explainModuleDefinedQueries(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        List<ModuleQueryExplainModel> models = new ArrayList<>();
        Map<String, ModuleQueryDefinition> defs = ModuleRegistry.getModuleQueryDefinitions();
        defs.forEach((k,v) -> {
            if(v.getTableName().equals(this.dao.tableName))
                models.add(new ModuleQueryExplainModel(v.getQueryTypeName(), v.getExplainInfo()));
        });
        return ResponseEntity.ok(models);
    }


    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    public EventInstanceModel createModel(EventInstanceVO vo) {
        return new EventInstanceModel(vo);
    }

    class EventAcknowledgeQueryStream extends QueryObjectStream<EventInstanceVO, EventInstanceModel, EventInstanceDao>{

        /**
         * @param dao
         * @param controller
         * @param root
         * @param queryCallback
         */
        public EventAcknowledgeQueryStream(EventInstanceDao dao,
                MangoVoRestController<EventInstanceVO, EventInstanceModel, EventInstanceDao> controller, ASTNode root,
                QueryStreamCallback<EventInstanceVO> queryCallback) {
            super(dao, controller, root, queryCallback);
        }

        /* (non-Javadoc)
         * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
         */
        @Override
        public void streamData(JsonGenerator jgen) throws IOException {
            this.queryCallback.setJsonGenerator(jgen);
            this.results.query();
            ((EventAcknowledgeQueryStreamCallback)this.queryCallback).finish();
        }

    }

    class EventAcknowledgeQueryStreamCallback extends QueryStreamCallback<EventInstanceVO>{

        private int count;
        private User user;
        private TranslatableMessage message;
        private long ackTimestamp;


        /**
         * @param user2
         * @param tlm
         */
        public EventAcknowledgeQueryStreamCallback(User user, TranslatableMessage message) {
            this.user = user;
            this.message = message;
            this.ackTimestamp = System.currentTimeMillis();
        }

        /* (non-Javadoc)
         * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
         */
        @Override
        public void row(EventInstanceVO vo, int index) {
            EventInstance event = Common.eventManager.acknowledgeEventById(vo.getId(), ackTimestamp, user, message);
            if (event != null && event.isAcknowledged()) {
                this.count++;
            }
        }

        public void finish() throws IOException{
            this.jgen.writeNumberField("count", this.count);
        }
    }
}
