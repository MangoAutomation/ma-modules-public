/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class MaintenanceEventDao extends AbstractDao<MaintenanceEventVO> {
    
    public static final MaintenanceEventDao instance = new MaintenanceEventDao();
    
    private MaintenanceEventDao() {
        super(ModuleRegistry.getWebSocketHandlerDefinition(MaintenanceEventWebSocketDefinition.TYPE_NAME),
                AuditEvent.TYPE_NAME, "m",
                new String[] {},
                false, new TranslatableMessage("header.maintenanceEvents"));
    }
    
    private static final String SELECT_POINT_IDS = "SELECT dataPointId FROM maintenanceEventPoints WHERE maintenanceEventId=?";
    private static final String SELECT_DATA_SOURCE_IDS = "SELECT dataSourceId FROM maintenanceEventDataSources WHERE maintenanceEventId=?";
    
    private static final String SELECT_POINTS = DataPointDao.instance.getSelectAllSql() + " JOIN maintenanceEventPoints mep ON mep.dataPointId = dp.id WHERE mep.maintenanceEventId=?";
    private static final String SELECT_DATA_SOURCES = DataSourceDao.instance.getSelectAllSql() + " JOIN maintenanceEventDataSources med ON mep.dataSourceId = ds.id WHERE med.maintenanceEventId=?";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#delete(int, java.lang.String)
     */
    @Override
    public void delete(MaintenanceEventVO vo, String initiatorId) {
        if (vo != null) {
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ejt.update("delete from eventHandlersMapping where eventTypeName=? and eventTypeRef1=?", new Object[] {
                            MaintenanceEventType.TYPE_NAME, vo.getId() });
                    MaintenanceEventDao.super.delete(vo, initiatorId);
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#loadRelationalData(com.serotonin.m2m2.vo.AbstractBasicVO)
     */
    @Override
    public void loadRelationalData(MaintenanceEventVO vo) {
        vo.setDataPoints(queryForList(SELECT_POINT_IDS, new Object[] {vo.getId()}, Integer.class));
        vo.setDataSources(queryForList(SELECT_DATA_SOURCE_IDS, new Object[] {vo.getId()}, Integer.class));
    }
    
    /**
     * Get the points for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getPoints(int maintenanceEventId, final MappedRowCallback<DataPointVO> callback){
        RowMapper<DataPointVO> pointMapper = DataPointDao.instance.getRowMapper();
        this.ejt.query(SELECT_POINTS, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.row(pointMapper.mapRow(rs, row), row);
                row++;
            }
            
        });
    }
    
    /**
     * Get the points for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getDataSources(int maintenanceEventId, final MappedRowCallback<DataSourceVO<?>> callback){
        RowMapper<DataSourceVO<?>> mapper = DataSourceDao.instance.getRowMapper();
        this.ejt.query(SELECT_DATA_SOURCES, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.row(mapper.mapRow(rs, row), row);
                row++;
            }
            
        });
    }
    
    private static final String INSERT_DATA_SOURCE_IDS = "INSERT INTO maintenanceEventDataSources (maintenanceEventId, dataSourceId) VALUES (?,?)";
    private static final String DELETE_DATA_SOURCE_IDS = "DELETE FROM maintenanceEventDataSources WHERE maintenanceEventId=?";
    
    private static final String INSERT_DATA_POINT_IDS = "INSERT INTO maintenanceEventDataPoints (maintenanceEventId, dataPointId) VALUES (?,?)";
    private static final String DELETE_DATA_POINT_IDS = "DELETE FROM maintenanceEventDataPoints WHERE maintenanceEventId=?";
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#saveRelationalData(com.serotonin.m2m2.vo.AbstractBasicVO, boolean)
     */
    @Override
    public void saveRelationalData(MaintenanceEventVO vo, boolean insert) {
        if(vo.getDataSources().size() > 0) {
            if(insert) {
                ejt.batchUpdate(INSERT_DATA_SOURCE_IDS, new InsertDataSources(vo));
            }else {
                //Delete and insert
                ejt.update(DELETE_DATA_SOURCE_IDS, new Object[] {vo.getId()});
                ejt.batchUpdate(INSERT_DATA_SOURCE_IDS, new InsertDataSources(vo));
            }
        }
        
        if(vo.getDataPoints().size() > 0) {
            if(insert) {
                ejt.batchUpdate(INSERT_DATA_POINT_IDS, new InsertDataPoints(vo));
            }else {
                //Delete and insert
                ejt.update(DELETE_DATA_POINT_IDS, new Object[] {vo.getId()});
                ejt.batchUpdate(INSERT_DATA_POINT_IDS, new InsertDataPoints(vo));
            }
        }
    }
    
    private static class InsertDataSources implements BatchPreparedStatementSetter {
        MaintenanceEventVO vo;
        
        InsertDataSources(MaintenanceEventVO vo) {
            this.vo = vo;
        }
        
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.BatchPreparedStatementSetter#getBatchSize()
         */
        @Override
        public int getBatchSize() {
            return vo.getDataSources().size();
        }
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.BatchPreparedStatementSetter#setValues(java.sql.PreparedStatement, int)
         */
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getDataSources().get(i));
        }
    }
    
    private static class InsertDataPoints implements BatchPreparedStatementSetter {
        MaintenanceEventVO vo;
        
        InsertDataPoints(MaintenanceEventVO vo) {
            this.vo = vo;
        }
        
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.BatchPreparedStatementSetter#getBatchSize()
         */
        @Override
        public int getBatchSize() {
            return vo.getDataPoints().size();
        }
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.BatchPreparedStatementSetter#setValues(java.sql.PreparedStatement, int)
         */
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getDataPoints().get(i));
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractDao#getXidPrefix()
     */
    @Override
    protected String getXidPrefix() {
        return MaintenanceEventVO.XID_PREFIX;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractDao#getNewVo()
     */
    @Override
    public MaintenanceEventVO getNewVo() {
        return new MaintenanceEventVO();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getTableName()
     */
    @Override
    protected String getTableName() {
        return SchemaDefinition.TABLE_NAME;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#voToObjectArray(com.serotonin.m2m2.vo.AbstractBasicVO)
     */
    @Override
    protected Object[] voToObjectArray(MaintenanceEventVO me) {
        return new Object[] {
                me.getXid(), 
                me.getName(),
                me.getAlarmLevel(),
                me.getScheduleType(),
                boolToChar(me.isDisabled()),
                me.getActiveYear(),
                me.getActiveMonth(),
                me.getActiveDay(),
                me.getActiveHour(),
                me.getActiveMinute(),
                me.getActiveSecond(),
                me.getActiveCron(),
                me.getInactiveYear(),
                me.getInactiveMonth(),
                me.getInactiveDay(),
                me.getInactiveHour(),
                me.getInactiveMinute(),
                me.getInactiveSecond(),
                me.getInactiveCron()   
        };
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertyTypeMap()
     */
    @Override
    protected LinkedHashMap<String, Integer> getPropertyTypeMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("id", Types.INTEGER);
        map.put("xid", Types.VARCHAR);
        map.put("alias", Types.VARCHAR);
        map.put("alarmLevel", Types.INTEGER);
        map.put("scheduleType", Types.INTEGER);
        map.put("disabled", Types.CHAR);
        map.put("activeYear", Types.INTEGER);
        map.put("activeMonth", Types.INTEGER);
        map.put("activeDay", Types.INTEGER);
        map.put("activeHour", Types.INTEGER);
        map.put("activeMinute", Types.INTEGER);
        map.put("activeSecond", Types.INTEGER);
        map.put("activeCron", Types.VARCHAR);
        map.put("inactiveYear", Types.INTEGER);
        map.put("inactiveMonth", Types.INTEGER);
        map.put("inactiveDay", Types.INTEGER);
        map.put("inactiveHour", Types.INTEGER);
        map.put("inactiveMinute", Types.INTEGER);
        map.put("inactiveSecond", Types.INTEGER);
        map.put("inactiveCron", Types.VARCHAR);
        return map;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertiesMap()
     */
    @Override
    protected Map<String, IntStringPair> getPropertiesMap() {
        HashMap<String, IntStringPair> map = new HashMap<String, IntStringPair>();
        return map;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getRowMapper()
     */
    @Override
    public RowMapper<MaintenanceEventVO> getRowMapper() {
        return new MaintenanceEventRowMapper();
    }
    
    class MaintenanceEventRowMapper implements RowMapper<MaintenanceEventVO> {
        @Override
        public MaintenanceEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MaintenanceEventVO me = new MaintenanceEventVO();
            int i = 0;
            me.setId(rs.getInt(++i));
            me.setXid(rs.getString(++i));
            me.setName(rs.getString(++i));
            me.setAlarmLevel(rs.getInt(++i));
            me.setScheduleType(rs.getInt(++i));
            me.setDisabled(charToBool(rs.getString(++i)));
            me.setActiveYear(rs.getInt(++i));
            me.setActiveMonth(rs.getInt(++i));
            me.setActiveDay(rs.getInt(++i));
            me.setActiveHour(rs.getInt(++i));
            me.setActiveMinute(rs.getInt(++i));
            me.setActiveSecond(rs.getInt(++i));
            me.setActiveCron(rs.getString(++i));
            me.setInactiveYear(rs.getInt(++i));
            me.setInactiveMonth(rs.getInt(++i));
            me.setInactiveDay(rs.getInt(++i));
            me.setInactiveHour(rs.getInt(++i));
            me.setInactiveMinute(rs.getInt(++i));
            me.setInactiveSecond(rs.getInt(++i));
            me.setInactiveCron(rs.getString(++i));

            return me;
        }
    }
}
