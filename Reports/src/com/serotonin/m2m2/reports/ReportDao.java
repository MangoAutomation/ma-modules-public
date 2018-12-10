/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.infiniteautomation.mango.monitor.AtomicIntegerMonitor;
import com.infiniteautomation.mango.monitor.ValueMonitorOwner;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.events.DaoEventType;
import com.infiniteautomation.mango.util.LazyInitSupplier;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.nosql.NoSQLDao;
import com.serotonin.m2m2.db.dao.nosql.NoSQLQueryCallback;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportUserComment;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.view.stats.ITime;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.vo.export.ExportDataStreamHandler;
import com.serotonin.m2m2.vo.export.ExportDataValue;
import com.serotonin.m2m2.vo.export.ExportPointInfo;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.taglib.Functions;

/**
 *
 * @author Matthew Lohbihler
 */
@Repository()
public class ReportDao extends AbstractDao<ReportVO> {

    public static final String TABLE_NAME = "reports";

    private static final LazyInitSupplier<ReportDao> springInstance = new LazyInitSupplier<>(() -> {
        Object o = Common.getRuntimeContext().getBean(ReportDao.class);
        if(o == null)
            throw new ShouldNeverHappenException("DAO not initialized in Spring Runtime Context");
        return (ReportDao)o;
    });

    private ReportDao(){
        super(ReportAuditEvent.TYPE_NAME, "rpt", new String[0], false, new TranslatableMessage("internal.monitor.REPORT_COUNT"));

        this.instanceCountMonitor = new AtomicIntegerMonitor("com.serotonin.m2m2.reports.ReportInstanceDao.COUNT", new TranslatableMessage("internal.monitor.REPORT_INSTANCE_COUNT"), instanceCountMonitorOwner);
        this.instanceCountMonitor.setValue(this.countInstances());
        Common.MONITORED_VALUES.addIfMissingStatMonitor(this.instanceCountMonitor);
    }

    /**
     * Get cached instance from Spring Context
     * @return
     */
    public static ReportDao getInstance() {
        return springInstance.get();
    }

    //
    //
    // Report Templates
    //
    private static final String REPORT_SELECT = "select data, id, xid, userId, name from reports ";

    @Override
    public String generateUniqueXid() {
        return generateUniqueXid(ReportVO.XID_PREFIX, "reports");
    }

    public List<ReportVO> getReports() {
        return query(REPORT_SELECT, new ReportRowMapper());
    }

    public List<ReportVO> getReports(int userId) {
        return query(REPORT_SELECT + "where userId=? order by name", new Object[] { userId }, new ReportRowMapper());
    }

    /**
     * @param xid
     * @return
     */
    public ReportVO getReport(String xid) {
        return queryForObject(REPORT_SELECT + "where xid=?", new Object[] { xid }, new ReportRowMapper(), null);
    }

    public ReportVO getReport(int id) {
        return queryForObject(REPORT_SELECT + "where id=?", new Object[] { id }, new ReportRowMapper(), null);
    }

    class ReportRowMapper implements RowMapper<ReportVO> {
        @Override
        public ReportVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            ReportVO report = (ReportVO) SerializationHelper.readObjectInContext(rs.getBlob(++i).getBinaryStream());
            report.setId(rs.getInt(++i));
            report.setXid(rs.getString(++i));
            report.setUserId(rs.getInt(++i));
            report.setName(rs.getString(++i));
            return report;
        }
    }

    public void saveReport(ReportVO report) {
        if (report.getId() == Common.NEW_ID)
            insertReport(report);
        else
            updateReport(report);
    }

    private static final String REPORT_INSERT = "insert into reports (xid, userId, name, data) values (?,?,?,?)";

    private void insertReport(final ReportVO report) {
        report.setId(doInsert(REPORT_INSERT,
                new Object[] { report.getXid(), report.getUserId(), report.getName(), SerializationHelper.writeObject(report) },
                new int[] { Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.BLOB }));
        this.publishEvent(new DaoEvent<ReportVO>(this, DaoEventType.CREATE, report, null, null));
        AuditEventType.raiseAddedEvent(ReportAuditEvent.TYPE_NAME, report);
        this.countMonitor.increment();
    }

    private static final String REPORT_UPDATE = "update reports set xid=?, userId=?, name=?, data=? where id=?";

    private void updateReport(final ReportVO report) {
        ReportVO old = getReport(report.getId());
        ejt.update(
                REPORT_UPDATE,
                new Object[] { report.getXid(), report.getUserId(), report.getName(), SerializationHelper.writeObject(report),
                        report.getId() }, new int[] { Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.BLOB, Types.INTEGER });
        this.publishEvent(new DaoEvent<ReportVO>(this, DaoEventType.UPDATE, report, null, null));
        AuditEventType.raiseChangedEvent(ReportAuditEvent.TYPE_NAME, old, report);
    }

    public void deleteReport(int reportId) {
        ReportVO report = getReport(reportId);
        ejt.update("delete from reports where id=?", new Object[] { reportId });
        this.publishEvent(new DaoEvent<ReportVO>(this, DaoEventType.DELETE, report, null, null));
        AuditEventType.raiseDeletedEvent(ReportAuditEvent.TYPE_NAME, report);
        this.countMonitor.decrement();
    }

    //
    //
    // Report Instances
    //
    private final ValueMonitorOwner instanceCountMonitorOwner = new ValueMonitorOwner(){

        @Override
        public void reset(String monitorId) {
            instanceCountMonitor.setValue(countInstances());
        }

    };
    private final AtomicIntegerMonitor instanceCountMonitor;

    private static final String REPORT_INSTANCE_COUNT = "SELECT COUNT(DISTINCT id) FROM reportInstances";

    public int countInstances(){
        return ejt.queryForInt(REPORT_INSTANCE_COUNT, new Object[0], 0);
    }

    private static final String REPORT_INSTANCE_SELECT = "select id, userId, reportId, name, template, includeEvents, includeUserComments, reportStartTime, reportEndTime, runStartTime, "
            + "  runEndTime, recordCount, preventPurge, mapping " + "from reportInstances ";

    public List<ReportInstance> getReportInstances() {
        return query(REPORT_INSTANCE_SELECT + "order by runStartTime desc", new ReportInstanceRowMapper());
    }

    public List<ReportInstance> getReportInstances(int userId) {
        return query(REPORT_INSTANCE_SELECT + "where userId=? order by runStartTime desc", new Object[] { userId },
                new ReportInstanceRowMapper());
    }

    public ReportInstance getReportInstance(int id) {
        return queryForObject(REPORT_INSTANCE_SELECT + "where id=?", new Object[] { id },
                new ReportInstanceRowMapper(), null);
    }



    class ReportInstanceRowMapper implements RowMapper<ReportInstance> {
        @Override
        public ReportInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            ReportInstance ri = new ReportInstance();
            ri.setId(rs.getInt(++i));
            ri.setUserId(rs.getInt(++i));
            ri.setReportId(rs.getInt(++i));
            ri.setName(rs.getString(++i));
            ri.setTemplateFile(rs.getString(++i));
            ri.setIncludeEvents(rs.getInt(++i));
            ri.setIncludeUserComments(charToBool(rs.getString(++i)));
            ri.setReportStartTime(rs.getLong(++i));
            ri.setReportEndTime(rs.getLong(++i));
            ri.setRunStartTime(rs.getLong(++i));
            ri.setRunEndTime(rs.getLong(++i));
            ri.setRecordCount(rs.getInt(++i));
            ri.setPreventPurge(charToBool(rs.getString(++i)));
            ri.setXidMap((Map<String, String>)SerializationHelper.readObjectInContext(rs.getBlob(++i).getBinaryStream()));
            return ri;
        }
    }

    /**
     * Delete the instance from the system
     * @param id
     * @param userId
     */
    public void deleteReportInstance(int id, int userId) {

        if(Common.databaseProxy.getNoSQLProxy() != null){
            //Delete the NoSQL Data
            final NoSQLDao dao = Common.databaseProxy.getNoSQLProxy().createNoSQLDao(ReportPointValueTimeSerializer.get(), "reports");

            List<ExportPointInfo> points = this.getReportInstancePoints(id);
            //Drop the series for these
            for(ExportPointInfo point : points){
                dao.deleteStore(id + "_" + point.getReportPointId());
            }
        }

        int deleted = ejt.update("delete from reportInstances where id=? and userId=?", new Object[] { id, userId });
        instanceCountMonitor.addValue(-deleted);

    }

    public int purgeReportsBefore(final long time) {
        //Check to see if we are using NoSQL
        if(Common.databaseProxy.getNoSQLProxy() == null){
            return ejt.update("delete from reportInstances where runStartTime<? and preventPurge=?", new Object[] { time,
                    boolToChar(false) });
        }else{
            //We need to get the report instances to delete first
            List<ReportInstance> instances =  query(REPORT_INSTANCE_SELECT + "where runStartTime<? and preventPurge=?", new Object[] { time,
                    boolToChar(false) },new ReportInstanceRowMapper());
            final NoSQLDao dao = Common.databaseProxy.getNoSQLProxy().createNoSQLDao(ReportPointValueTimeSerializer.get(), "reports");

            for(ReportInstance instance :instances){
                List<ExportPointInfo> points = this.getReportInstancePoints(instance.getId());
                //Drop the series for these
                for(ExportPointInfo point : points){
                    dao.deleteStore(instance.getId() + "_" + point.getReportPointId());
                }
            }

            int deleted =  ejt.update("delete from reportInstances where runStartTime<? and preventPurge=?", new Object[] { time,
                    boolToChar(false) });

            instanceCountMonitor.addValue(-deleted);
            return deleted;
        }
    }

    /**
     * Set prevent purge on an instance, if User is Admin allow access to all report instances
     * if user is not admin restrict to only reports created by that user.
     * @param id
     * @param preventPurge
     * @param user
     */
    public void setReportInstancePreventPurge(int id, boolean preventPurge, User user) {

        if(Permissions.hasAdminPermission(user))
            ejt.update("update reportInstances set preventPurge=? where id=?", new Object[] {
                    boolToChar(preventPurge), id });
        else
            ejt.update("update reportInstances set preventPurge=? where id=? and userId=?", new Object[] {
                    boolToChar(preventPurge), id, user.getId() });
    }

    /**
     * Allow admin users to set prevent purge on an instance
     * @param id
     * @param preventPurge
     */
    public void setReportInstancePreventPurge(int id, boolean preventPurge) {

    }

    /**
     * This method should only be called by the ReportWorkItem.
     */
    private static final String REPORT_INSTANCE_INSERT = "insert into reportInstances "
            + "  (userId, reportId, name, template, includeEvents, includeUserComments, reportStartTime, reportEndTime, runStartTime, "
            + "     runEndTime, recordCount, preventPurge, mapping) " + "  values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String REPORT_INSTANCE_UPDATE = "update reportInstances set reportStartTime=?, reportEndTime=?, runStartTime=?, runEndTime=?, recordCount=? "
            + "where id=?";

    public void saveReportInstance(ReportInstance instance) {
        if (instance.getId() == Common.NEW_ID){
            instance.setId(doInsert(
                    REPORT_INSTANCE_INSERT,
                    new Object[] { instance.getUserId(), instance.getReportId(), instance.getName(), instance.getTemplateFile(), instance.getIncludeEvents(),
                            boolToChar(instance.isIncludeUserComments()), instance.getReportStartTime(),
                            instance.getReportEndTime(), instance.getRunStartTime(), instance.getRunEndTime(),
                            instance.getRecordCount(), boolToChar(instance.isPreventPurge()), SerializationHelper.writeObject(instance.getXidMap()) }));
            instanceCountMonitor.increment();
        }else
            ejt.update(
                    REPORT_INSTANCE_UPDATE,
                    new Object[] { instance.getReportStartTime(), instance.getReportEndTime(),
                            instance.getRunStartTime(), instance.getRunEndTime(), instance.getRecordCount(),
                            instance.getId() });
    }

    /**
     * This method should only be called by the ReportWorkItem.
     */
    private static final String REPORT_INSTANCE_POINTS_INSERT = "insert into reportInstancePoints " //
            + "(reportInstanceId, deviceName, pointName, xid, dataType, startValue, textRenderer, colour, weight,"
            + " consolidatedChart, plotType) " //
            + "values (?,?,?,?,?,?,?,?,?,?,?)";

    public static class PointInfo {
        private final DataPointVO point;
        private final String colour;
        private final float weight;
        private final boolean consolidatedChart;
        private final int plotType;

        public PointInfo(DataPointVO point, String colour, float weight, boolean consolidatedChart, int plotType) {
            this.point = point;
            this.colour = colour;
            this.weight = weight;
            this.consolidatedChart = consolidatedChart;
            this.plotType = plotType;
        }

        public DataPointVO getPoint() {
            return point;
        }

        public String getColour() {
            return colour;
        }

        public float getWeight() {
            return weight;
        }

        public boolean isConsolidatedChart() {
            return consolidatedChart;
        }

        public int getPlotType() {
            return plotType;
        }
    }

    /**
     * SQL Database Report
     * @param instance
     * @param points
     * @return
     */
    public int runReportSQL(final ReportInstance instance, List<PointInfo> points) {
        PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
        int count = 0;

        // The timestamp selection code is used multiple times for different tables
        String timestampSql;
        Object[] timestampParams;
        if (instance.isFromInception() && instance.isToNow()) {
            timestampSql = "";
            timestampParams = new Object[0];
        }
        else if (instance.isFromInception()) {
            timestampSql = "and ${field}<?";
            timestampParams = new Object[] { instance.getReportEndTime() };
        }
        else if (instance.isToNow()) {
            timestampSql = "and ${field}>=?";
            timestampParams = new Object[] { instance.getReportStartTime() };
        }
        else {
            timestampSql = "and ${field}>=? and ${field}<?";
            timestampParams = new Object[] { instance.getReportStartTime(), instance.getReportEndTime() };
        }

        // For each point.
        for (PointInfo pointInfo : points) {
            DataPointVO point = pointInfo.getPoint();
            int dataType = point.getPointLocator().getDataTypeId();

            DataValue startValue = null;
            if (!instance.isFromInception()) {
                // Get the value just before the start of the report
                PointValueTime pvt = pointValueDao.getPointValueBefore(point.getId(), instance.getReportStartTime());
                if (pvt != null)
                    startValue = pvt.getValue();

                // Make sure the data types match
                if (DataTypes.getDataType(startValue) != dataType)
                    startValue = null;
            }

            // Insert the reportInstancePoints record
            String name = Functions.truncate(point.getName(), 100);

            int reportPointId = doInsert(
                    REPORT_INSTANCE_POINTS_INSERT,
                    new Object[] { instance.getId(), point.getDeviceName(), name, pointInfo.getPoint().getXid(), dataType,
                            DataTypes.valueToString(startValue),
                            SerializationHelper.writeObject(point.getTextRenderer()), pointInfo.getColour(),
                            pointInfo.getWeight(), boolToChar(pointInfo.isConsolidatedChart()), pointInfo.getPlotType() },
                    new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.BLOB,
                            Types.VARCHAR, Types.FLOAT, Types.CHAR, Types.INTEGER });

            // Insert the reportInstanceData records
            String insertSQL = "insert into reportInstanceData " //
                    + "  select id, " + reportPointId + ", pointValue, ts from pointValues " //
                    + "    where dataPointId=? and dataType=? " //
                    + StringUtils.replaceMacro(timestampSql, "field", "ts");
            count += ejt.update(insertSQL, appendParameters(timestampParams, point.getId(), dataType));

            // Insert the reportInstanceDataAnnotations records
            ejt.update(
                    "insert into reportInstanceDataAnnotations " //
                    + "  (pointValueId, reportInstancePointId, textPointValueShort, textPointValueLong, sourceMessage) " //
                    + "  select rd.pointValueId, rd.reportInstancePointId, pva.textPointValueShort, " //
                    + "    pva.textPointValueLong, pva.sourceMessage " //
                    + "  from reportInstanceData rd " //
                    + "    join reportInstancePoints rp on rd.reportInstancePointId = rp.id " //
                    + "    join pointValueAnnotations pva on rd.pointValueId = pva.pointValueId " //
                    + "  where rp.id = ?", new Object[] { reportPointId });

            // Insert the reportInstanceEvents records for the point.
            if (instance.getIncludeEvents() != ReportVO.EVENTS_NONE) {
                String eventSQL = "insert into reportInstanceEvents " //
                        + "  (eventId, reportInstanceId, typeName, subtypeName, typeRef1, typeRef2, activeTs, " //
                        + "   rtnApplicable, rtnTs, rtnCause, alarmLevel, message, ackTs, ackUsername, " //
                        + "   alternateAckSource)" //
                        + "  select e.id, " + instance.getId() + ", e.typeName, e.subtypeName, e.typeRef1, " //
                        + "    e.typeRef2, e.activeTs, e.rtnApplicable, e.rtnTs, e.rtnCause, e.alarmLevel, " //
                        + "    e.message, e.ackTs, u.username, e.alternateAckSource " //
                        + "  from events e join userEvents ue on ue.eventId=e.id " //
                        + "    left join users u on e.ackUserId=u.id " //
                        + "  where ue.userId=? " //
                        + "    and e.typeName=? " //
                        + "    and e.typeRef1=? ";

                if (instance.getIncludeEvents() == ReportVO.EVENTS_ALARMS)
                    eventSQL += "and e.alarmLevel > 0 ";

                eventSQL += StringUtils.replaceMacro(timestampSql, "field", "e.activeTs");
                ejt.update(
                        eventSQL,
                        appendParameters(timestampParams, instance.getUserId(), EventType.EventTypeNames.DATA_POINT,
                                point.getId()));
            }

            // Insert the reportInstanceUserComments records for the point.
            if (instance.isIncludeUserComments()) {
                String commentSQL = "insert into reportInstanceUserComments " //
                        + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
                        + "  select " + instance.getId() + ", u.username, " + UserCommentVO.TYPE_POINT + ", " //
                        + reportPointId + ", uc.ts, uc.commentText " //
                        + "  from userComments uc " //
                        + "    left join users u on uc.userId=u.id " //
                        + "  where uc.commentType=" + UserCommentVO.TYPE_POINT //
                        + "    and uc.typeKey=? ";

                // Only include comments made in the duration of the report.
                commentSQL += StringUtils.replaceMacro(timestampSql, "field", "uc.ts");
                ejt.update(commentSQL, appendParameters(timestampParams, point.getId()));
            }
        }

        // Insert the reportInstanceUserComments records for the selected events
        if (instance.isIncludeUserComments()) {
            String commentSQL = "insert into reportInstanceUserComments " //
                    + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
                    + "  select " + instance.getId() + ", u.username, " + UserCommentVO.TYPE_EVENT + ", uc.typeKey, " //
                    + "    uc.ts, uc.commentText " //
                    + "  from userComments uc " //
                    + "    left join users u on uc.userId=u.id " //
                    + "    join reportInstanceEvents re on re.eventId=uc.typeKey " //
                    + "  where uc.commentType=" + UserCommentVO.TYPE_EVENT //
                    + "    and re.reportInstanceId=? ";
            ejt.update(commentSQL, new Object[] { instance.getId() });
        }

        // If the report had undefined start or end times, update them with values from the data.
        if (instance.isFromInception() || instance.isToNow()) {
            ejt.query(
                    "select min(rd.ts), max(rd.ts) " //
                    + "from reportInstancePoints rp "
                    + "  join reportInstanceData rd on rp.id=rd.reportInstancePointId "
                    + "where rp.reportInstanceId=?", new Object[] { instance.getId() },
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            if (instance.isFromInception())
                                instance.setReportStartTime(rs.getLong(1));
                            if (instance.isToNow())
                                instance.setReportEndTime(rs.getLong(2));
                        }
                    });
        }

        return count;
    }

    private Object[] appendParameters(Object[] toAppend, Object... params) {
        if (toAppend.length == 0)
            return params;
        if (params.length == 0)
            return toAppend;

        Object[] result = new Object[params.length + toAppend.length];
        System.arraycopy(params, 0, result, 0, params.length);
        System.arraycopy(toAppend, 0, result, params.length, toAppend.length);
        return result;
    }

    /**
     * This method guarantees that the data is provided to the setData handler method grouped by point (points are not
     * ordered), and sorted by time ascending.
     */
    private static final String REPORT_INSTANCE_POINT_SELECT = "select id, deviceName, pointName, xid, dataType, " //
            + "startValue, textRenderer, colour, weight, consolidatedChart, plotType " //
            + "from reportInstancePoints ";

    /**
     * Get the Points for a report
     * @param instanceId
     * @return
     */
    public List<ExportPointInfo> getReportInstancePoints(int instanceId){
        return query(REPORT_INSTANCE_POINT_SELECT + "where reportInstanceId=?",
                new Object[] { instanceId }, new RowMapper<ExportPointInfo>() {
            @Override
            public ExportPointInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                int i = 0;
                ExportPointInfo rp = new ExportPointInfo();
                rp.setReportPointId(rs.getInt(++i));
                rp.setDeviceName(rs.getString(++i));
                rp.setPointName(rs.getString(++i));
                rp.setXid(rs.getString(++i));
                rp.setDataType(rs.getInt(++i));
                String startValue = rs.getString(++i);
                if (startValue != null)
                    rp.setStartValue(DataValue.stringToValue(startValue, rp.getDataType()));
                rp.setTextRenderer((TextRenderer) SerializationHelper.readObjectInContext(rs.getBlob(++i)
                        .getBinaryStream()));
                rp.setColour(rs.getString(++i));
                rp.setWeight(rs.getFloat(++i));
                rp.setConsolidatedChart(charToBool(rs.getString(++i)));
                rp.setPlotType(rs.getInt(++i));
                return rp;
            }
        });
    }

    private static final String REPORT_INSTANCE_DATA_SELECT = "select rd.pointValue, rda.textPointValueShort, " //
            + "  rda.textPointValueLong, rd.ts, rda.sourceMessage "
            + "from reportInstanceData rd "
            + "  left join reportInstanceDataAnnotations rda on "
            + "      rd.pointValueId=rda.pointValueId and rd.reportInstancePointId=rda.reportInstancePointId ";

    public void reportInstanceDataSQL(int instanceId, final ExportDataStreamHandler handler) {
        // Retrieve point information.
        List<ExportPointInfo> pointInfos = query(REPORT_INSTANCE_POINT_SELECT + "where reportInstanceId=?",
                new Object[] { instanceId }, new RowMapper<ExportPointInfo>() {
            @Override
            public ExportPointInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                int i = 0;
                ExportPointInfo rp = new ExportPointInfo();
                rp.setReportPointId(rs.getInt(++i));
                rp.setDeviceName(rs.getString(++i));
                rp.setPointName(rs.getString(++i));
                rp.setXid(rs.getString(++i));
                rp.setDataType(rs.getInt(++i));
                String startValue = rs.getString(++i);
                if (startValue != null)
                    rp.setStartValue(DataValue.stringToValue(startValue, rp.getDataType()));
                rp.setTextRenderer((TextRenderer) SerializationHelper.readObjectInContext(rs.getBlob(++i)
                        .getBinaryStream()));
                rp.setColour(rs.getString(++i));
                rp.setWeight(rs.getFloat(++i));
                rp.setConsolidatedChart(charToBool(rs.getString(++i)));
                rp.setPlotType(rs.getInt(++i));
                return rp;
            }
        });

        final ExportDataValue edv = new ExportDataValue();
        for (final ExportPointInfo point : pointInfos) {
            if(point.getDataType() == DataTypes.IMAGE){
                DataPointVO vo = DataPointDao.getInstance().getByXid(point.getXid());
                if(vo != null)
                    point.setDataPointId(vo.getId());
                else
                    point.setDataPointId(-1);
            }
            handler.startPoint(point);

            edv.setReportPointId(point.getReportPointId());
            final int dataType = point.getDataType();
            ejt.query(REPORT_INSTANCE_DATA_SELECT + "where rd.reportInstancePointId=? order by rd.ts",
                    new Object[] { point.getReportPointId() }, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    switch (dataType) {
                        case (DataTypes.NUMERIC):
                            edv.setValue(new NumericValue(rs.getDouble(1)));
                        break;
                        case (DataTypes.BINARY):
                            edv.setValue(new BinaryValue(rs.getDouble(1) == 1));
                        break;
                        case (DataTypes.MULTISTATE):
                            edv.setValue(new MultistateValue(rs.getInt(1)));
                        break;
                        case (DataTypes.ALPHANUMERIC):
                            edv.setValue(new AlphanumericValue(rs.getString(2)));
                        if (rs.wasNull())
                            edv.setValue(new AlphanumericValue(rs.getString(3)));
                        break;
                        case (DataTypes.IMAGE):
                            edv.setValue(new ImageValue(Integer.parseInt(rs.getString(2)), rs.getInt(1)));
                        break;
                        default:
                            edv.setValue(null);
                    }

                    edv.setTime(rs.getLong(4));
                    edv.setAnnotation(BaseDao.readTranslatableMessage(rs, 5));
                    handler.pointData(edv);
                }
            });
        }
        handler.done();
    }


    public void reportInstanceDataNoSQL(int instanceId, final ExportDataStreamHandler handler) {
        // Retrieve point information.
        List<ExportPointInfo> pointInfos = query(REPORT_INSTANCE_POINT_SELECT + "where reportInstanceId=?",
                new Object[] { instanceId }, new RowMapper<ExportPointInfo>() {
            @Override
            public ExportPointInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                int i = 0;
                ExportPointInfo rp = new ExportPointInfo();
                rp.setReportPointId(rs.getInt(++i));
                rp.setDeviceName(rs.getString(++i));
                rp.setPointName(rs.getString(++i));
                rp.setXid(rs.getString(++i));
                rp.setDataType(rs.getInt(++i));
                String startValue = rs.getString(++i);
                if (startValue != null)
                    rp.setStartValue(DataValue.stringToValue(startValue, rp.getDataType()));
                rp.setTextRenderer((TextRenderer) SerializationHelper.readObjectInContext(rs.getBlob(++i)
                        .getBinaryStream()));
                rp.setColour(rs.getString(++i));
                rp.setWeight(rs.getFloat(++i));
                rp.setConsolidatedChart(charToBool(rs.getString(++i)));
                rp.setPlotType(rs.getInt(++i));
                return rp;
            }
        });

        final ExportDataValue edv = new ExportDataValue();
        for (final ExportPointInfo point : pointInfos) {
            if(point.getDataType() == DataTypes.IMAGE){
                DataPointVO vo = DataPointDao.getInstance().getByXid(point.getXid());
                if(vo != null)
                    point.setDataPointId(vo.getId());
                else
                    point.setDataPointId(-1);
            }
            handler.startPoint(point);

            edv.setReportPointId(point.getReportPointId());
            final NoSQLDao dao = Common.databaseProxy.getNoSQLProxy().createNoSQLDao(ReportPointValueTimeSerializer.get(), "reports");
            final String pointStore = instanceId + "_" + point.getReportPointId();
            dao.getData(pointStore, 0, Long.MAX_VALUE, -1, false, new NoSQLQueryCallback(){

                @Override
                public void entry(String storeName, long timestamp, ITime entry) {
                    PointValueTime pvt = (PointValueTime) entry;
                    edv.setValue(pvt.getValue());
                    edv.setTime(pvt.getTime());

                    if(pvt instanceof AnnotatedPointValueTime)
                        edv.setAnnotation(((AnnotatedPointValueTime)pvt).getSourceMessage());

                    handler.pointData(edv);
                }

            });
        }
        handler.done();
    }

    private static final String EVENT_SELECT = //
            "select eventId, typeName, subtypeName, typeRef1, typeRef2, activeTs, rtnApplicable, rtnTs, rtnCause, " //
            + "  alarmLevel, message, ackTs, 0, ackUsername, alternateAckSource, 0 " //
            + "from reportInstanceEvents " //
            + "where reportInstanceId=? " //
            + "order by activeTs";
    private static final String EVENT_COMMENT_SELECT = "select username, typeKey, ts, commentText " //
            + "from reportInstanceUserComments " //
            + "where reportInstanceId=? and commentType=? " //
            + "order by ts";

    public List<EventInstance> getReportInstanceEvents(int instanceId) {
        // Get the events.
        final List<EventInstance> events = query(EVENT_SELECT, new Object[] { instanceId },
                new EventDao.EventInstanceRowMapper());
        // Add in the comments.
        ejt.query(EVENT_COMMENT_SELECT, new Object[] { instanceId, UserCommentVO.TYPE_EVENT }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // Create the comment
                UserCommentVO c = new UserCommentVO();
                c.setUsername(rs.getString(1));
                c.setTs(rs.getLong(3));
                c.setComment(rs.getString(4));

                // Find the event and add the comment
                int eventId = rs.getInt(2);
                for (EventInstance event : events) {
                    if (event.getId() == eventId) {
                        if (event.getEventComments() == null)
                            event.setEventComments(new ArrayList<UserCommentVO>());
                        event.addEventComment(c);
                    }
                }
            }
        });
        // Done
        return events;
    }

    private static final String USER_COMMENT_SELECT = "select rc.username, rc.commentType, rc.typeKey, rp.pointName, " //
            + "  rc.ts, rc.commentText "
            + "from reportInstanceUserComments rc "
            + "  left join reportInstancePoints rp on rc.typeKey=rp.id and rc.commentType="
            + UserCommentVO.TYPE_POINT
            + " " + "where rc.reportInstanceId=? " + "order by rc.ts ";

    public List<ReportUserComment> getReportInstanceUserComments(int instanceId) {
        return query(USER_COMMENT_SELECT, new Object[] { instanceId }, new ReportCommentRowMapper());
    }

    class ReportCommentRowMapper implements RowMapper<ReportUserComment> {
        @Override
        public ReportUserComment mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportUserComment c = new ReportUserComment();
            c.setUsername(rs.getString(1));
            c.setCommentType(rs.getInt(2));
            c.setTypeKey(rs.getInt(3));
            c.setPointName(rs.getString(4));
            c.setTs(rs.getLong(5));
            c.setComment(rs.getString(6));
            return c;
        }
    }

    public List<Long> getFiledataIds() {
        //TODO add support for NoSQL and SQL when we actually copy the image files.  Currently we only
        // reference the existing point value files so there are no files to delete that are our own.

        /* return queryForList("select distinct d.pointValueId from reportInstanceData d " //
                + "  join reportInstancePoints p on d.reportInstancePointId=p.id " //
                + "where p.dataType=?", new Object[] { DataTypes.IMAGE }, Long.class); */
        return new ArrayList<Long>();
    }


    /**
     * Generate a report using the NoSQL DB for point value storage
     * @param instance
     * @param points
     * @return
     */
    public int runReportNoSQL(final ReportInstance instance, List<PointInfo> points) {
        PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
        final MappedCallbackCounter count = new MappedCallbackCounter();
        final NoSQLDao dao = Common.databaseProxy.getNoSQLProxy().createNoSQLDao(ReportPointValueTimeSerializer.get(), "reports");

        // The timestamp selection code is used multiple times for different tables
        long startTime, endTime;
        String timestampSql;
        Object[] timestampParams;
        if (instance.isFromInception() && instance.isToNow()) {
            timestampSql = "";
            timestampParams = new Object[0];
            startTime = 0l;
            endTime = Common.timer.currentTimeMillis();
        }
        else if (instance.isFromInception()) {
            timestampSql = "and ${field}<?";
            timestampParams = new Object[] { instance.getReportEndTime() };
            startTime = 0l;
            endTime = instance.getReportEndTime();
        }
        else if (instance.isToNow()) {
            timestampSql = "and ${field}>=?";
            timestampParams = new Object[] { instance.getReportStartTime() };
            startTime = instance.getReportStartTime();
            endTime = Common.timer.currentTimeMillis();
        }
        else {
            timestampSql = "and ${field}>=? and ${field}<?";
            timestampParams = new Object[] { instance.getReportStartTime(), instance.getReportEndTime() };
            startTime = instance.getReportStartTime();
            endTime = instance.getReportEndTime();
        }

        // For each point.
        List<Integer> pointIds = new ArrayList<Integer>();
        //Map the pointId to the Report PointId
        final Map<Integer,Integer> pointIdMap = new HashMap<Integer,Integer>();

        //Loop over all points, pre-process them and prepare to transfer the data to
        // the reports table/data store
        for (PointInfo pointInfo : points) {
            DataPointVO point = pointInfo.getPoint();
            pointIds.add(point.getId());
            int dataType = point.getPointLocator().getDataTypeId();


            DataValue startValue = null;
            if (!instance.isFromInception()) {
                // Get the value just before the start of the report
                PointValueTime pvt = pointValueDao.getPointValueBefore(point.getId(), instance.getReportStartTime());
                if (pvt != null)
                    startValue = pvt.getValue();

                // Make sure the data types match
                if (DataTypes.getDataType(startValue) != dataType)
                    startValue = null;
            }

            // Insert the reportInstancePoints record
            String name = Functions.truncate(point.getName(), 100);

            int reportPointId = doInsert(
                    REPORT_INSTANCE_POINTS_INSERT,
                    new Object[] { instance.getId(), point.getDeviceName(), name, pointInfo.getPoint().getXid(), dataType,
                            DataTypes.valueToString(startValue),
                            SerializationHelper.writeObject(point.getTextRenderer()), pointInfo.getColour(),
                            pointInfo.getWeight(), boolToChar(pointInfo.isConsolidatedChart()), pointInfo.getPlotType() },
                    new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.BLOB,
                            Types.VARCHAR, Types.FLOAT, Types.CHAR, Types.INTEGER });

            //Keep the info in the map
            pointIdMap.put(pointInfo.getPoint().getId(), reportPointId);

            // Insert the reportInstanceDataAnnotations records
            ejt.update(
                    "insert into reportInstanceDataAnnotations " //
                    + "  (pointValueId, reportInstancePointId, textPointValueShort, textPointValueLong, sourceMessage) " //
                    + "  select rd.pointValueId, rd.reportInstancePointId, pva.textPointValueShort, " //
                    + "    pva.textPointValueLong, pva.sourceMessage " //
                    + "  from reportInstanceData rd " //
                    + "    join reportInstancePoints rp on rd.reportInstancePointId = rp.id " //
                    + "    join pointValueAnnotations pva on rd.pointValueId = pva.pointValueId " //
                    + "  where rp.id = ?", new Object[] { reportPointId });

            // Insert the reportInstanceEvents records for the point.
            if (instance.getIncludeEvents() != ReportVO.EVENTS_NONE) {
                String eventSQL = "insert into reportInstanceEvents " //
                        + "  (eventId, reportInstanceId, typeName, subtypeName, typeRef1, typeRef2, activeTs, " //
                        + "   rtnApplicable, rtnTs, rtnCause, alarmLevel, message, ackTs, ackUsername, " //
                        + "   alternateAckSource)" //
                        + "  select e.id, " + instance.getId() + ", e.typeName, e.subtypeName, e.typeRef1, " //
                        + "    e.typeRef2, e.activeTs, e.rtnApplicable, e.rtnTs, e.rtnCause, e.alarmLevel, " //
                        + "    e.message, e.ackTs, u.username, e.alternateAckSource " //
                        + "  from events e join userEvents ue on ue.eventId=e.id " //
                        + "    left join users u on e.ackUserId=u.id " //
                        + "  where ue.userId=? " //
                        + "    and e.typeName=? " //
                        + "    and e.typeRef1=? ";

                if (instance.getIncludeEvents() == ReportVO.EVENTS_ALARMS)
                    eventSQL += "and e.alarmLevel > 0 ";

                eventSQL += StringUtils.replaceMacro(timestampSql, "field", "e.activeTs");
                ejt.update(
                        eventSQL,
                        appendParameters(timestampParams, instance.getUserId(), EventType.EventTypeNames.DATA_POINT,
                                point.getId()));
            }

            // Insert the reportInstanceUserComments records for the point.
            if (instance.isIncludeUserComments()) {
                String commentSQL = "insert into reportInstanceUserComments " //
                        + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
                        + "  select " + instance.getId() + ", u.username, " + UserCommentVO.TYPE_POINT + ", " //
                        + reportPointId + ", uc.ts, uc.commentText " //
                        + "  from userComments uc " //
                        + "    left join users u on uc.userId=u.id " //
                        + "  where uc.commentType=" + UserCommentVO.TYPE_POINT //
                        + "    and uc.typeKey=? ";

                // Only include comments made in the duration of the report.
                commentSQL += StringUtils.replaceMacro(timestampSql, "field", "uc.ts");
                ejt.update(commentSQL, appendParameters(timestampParams, point.getId()));
            }
        } //end for all points

        //Insert the data into the NoSQL DB and track first/last times
        //The series name is reportInstanceId_reportPointId
        final AtomicLong firstPointTime = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong lastPointTime = new AtomicLong(-1l);
        final String reportId = Integer.toString(instance.getId()) + "_";
        pointValueDao.getPointValuesBetween(pointIds, startTime, endTime, new MappedRowCallback<IdPointValueTime>(){
            @Override
            public void row(final IdPointValueTime ipvt, int rowId) {
                dao.storeData( reportId + Integer.toString(pointIdMap.get(ipvt.getId())),ipvt);
                count.increment();
                if(ipvt.getTime() < firstPointTime.get())
                    firstPointTime.set(ipvt.getTime());
                if(ipvt.getTime() > lastPointTime.get())
                    lastPointTime.set(ipvt.getTime());
            }
        });

        // Insert the reportInstanceUserComments records for the selected events
        if (instance.isIncludeUserComments()) {
            String commentSQL = "insert into reportInstanceUserComments " //
                    + "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
                    + "  select " + instance.getId() + ", u.username, " + UserCommentVO.TYPE_EVENT + ", uc.typeKey, " //
                    + "    uc.ts, uc.commentText " //
                    + "  from userComments uc " //
                    + "    left join users u on uc.userId=u.id " //
                    + "    join reportInstanceEvents re on re.eventId=uc.typeKey " //
                    + "  where uc.commentType=" + UserCommentVO.TYPE_EVENT //
                    + "    and re.reportInstanceId=? ";
            ejt.update(commentSQL, new Object[] { instance.getId() });
        }

        // If the report had undefined start or end times, update them with values from the data.
        if (instance.isFromInception() || instance.isToNow()) {
            if(instance.isFromInception()){
                if(firstPointTime.get() != Long.MAX_VALUE)
                    instance.setReportStartTime(firstPointTime.get());
            }
            if(instance.isToNow()){
                instance.setReportEndTime(lastPointTime.get());
            }
        }

        return count.getCount();
    }

    class MappedCallbackCounter {
        int count = 0;
        public int getCount(){
            return count;
        }
        public void increment(){
            this.count++;
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractDao#getXidPrefix()
     */
    @Override
    protected String getXidPrefix() {
        return ReportVO.XID_PREFIX;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractDao#getNewVo()
     */
    @Override
    public ReportVO getNewVo() {
        return new ReportVO();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#voToObjectArray(com.serotonin.m2m2.vo.AbstractBasicVO)
     */
    @Override
    protected Object[] voToObjectArray(ReportVO vo) {
        return new Object[] {
                SerializationHelper.writeObject(vo),
                vo.getXid(),
                vo.getUserId(),
                vo.getName()};
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertyTypeMap()
     */
    @Override
    protected LinkedHashMap<String, Integer> getPropertyTypeMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("id", Types.INTEGER);
        map.put("data", Types.BINARY);
        map.put("xid", Types.VARCHAR);
        map.put("userId", Types.INTEGER);
        map.put("name", Types.VARCHAR);
        return map;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertiesMap()
     */
    @Override
    protected Map<String, IntStringPair> getPropertiesMap() {
        return new HashMap<String, IntStringPair>();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getRowMapper()
     */
    @Override
    public RowMapper<ReportVO> getRowMapper() {
        return new ReportMapper();
    }

    class ReportMapper implements RowMapper<ReportVO> {
        @Override
        public ReportVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            int id = (rs.getInt(++i));
            ReportVO report = (ReportVO) SerializationHelper.readObjectInContext(rs.getBlob(++i).getBinaryStream());
            report.setId(id);
            report.setXid(rs.getString(++i));
            report.setUserId(rs.getInt(++i));
            report.setName(rs.getString(++i));
            return report;
        }
    }
    
}
