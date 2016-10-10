/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.WatchListWebSocketConfiguration;
import com.serotonin.m2m2.web.mvc.rest.v1.WatchListWebSocketHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 */
public class WatchListDao extends AbstractDao<WatchListVO> {
    
	public static String TABLE_NAME = "watchLists";
	public static WatchListDao instance = new WatchListDao();
    WatchListWebSocketHandler handler;
	private ObjectMapper mapper;
	
	private WatchListDao() {
		super(AuditEvent.TYPE_NAME, "w",
		        new String[] {"u.username"}, //to allow filtering on username
				"join users u on u.id = w.userId ");
		this.handler = WatchListWebSocketConfiguration.handler;
		mapper = new ObjectMapper();
	}

	
    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchListVO> getWatchLists(final User user) {
        final List<WatchListVO> result = new ArrayList<>();
        query(SELECT_ALL + "ORDER BY w.name", new Object[0], rowMapper, new MappedRowCallback<WatchListVO>() {
            @Override
            public void row(WatchListVO wl, int index) {
                if (wl.isReader(user))
                    result.add(wl);
            }
        });
        return result;
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchListVO> getWatchLists() {
        return query(SELECT_ALL + "ORDER BY w.name", rowMapper);
    }

    public WatchListVO getWatchList(int watchListId) {
        // Get the watch lists.
        WatchListVO watchList = queryForObject(SELECT_ALL + "WHERE w.id=?", new Object[] { watchListId }, rowMapper);
        populateWatchlistData(watchList);
        return watchList;
    }

    public void populateWatchlistData(List<WatchListVO> watchLists) {
        for (WatchListVO watchList : watchLists)
            populateWatchlistData(watchList);
    }

    public void populateWatchlistData(WatchListVO watchList) {
        if (watchList == null)
            return;

        // Get the points for each of the watch lists.
        List<Integer> pointIds = queryForList(
                "SELECT dataPointId FROM watchListPoints WHERE watchListId=? ORDER BY sortOrder",
                new Object[] { watchList.getId() }, Integer.class);
        List<DataPointVO> points = watchList.getPointList();
        DataPointDao dataPointDao = new DataPointDao();
        for (Integer pointId : pointIds)
            points.add(dataPointDao.getDataPoint(pointId));
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public WatchListVO getWatchList(String xid) {
        return queryForObject(SELECT_ALL + " WHERE w.xid=?", new Object[] { xid }, rowMapper, null);
    }

    public WatchListVO getSelectedWatchList(int userId) {
        WatchListVO watchList = queryForObject(
                SELECT_ALL + "JOIN selectedWatchList s ON s.watchListId=w.id WHERE s.userId=?", new Object[] { userId },
                rowMapper, null);
        populateWatchlistData(watchList);
        return watchList;
    }

    public void saveSelectedWatchList(int userId, int watchListId) {
        int count = ejt.update("UPDATE selectedWatchList SET watchListId=? WHERE userId=?", new Object[] { watchListId,
                userId });
        if (count == 0)
            ejt.update("INSERT INTO selectedWatchList (userId, watchListId) VALUES (?,?)", new Object[] { userId,
                    watchListId });
    }

    public WatchListVO createNewWatchList(WatchListVO wl, int userId) {
        wl.setUserId(userId);
        wl.setXid(generateUniqueXid());
        wl.setId(ejt.doInsert(
                "INSERT INTO watchLists (xid, userId, name, readPermission, editPermission, type) VALUES (?,?,?,?,?, 'static')",
                new Object[] { wl.getXid(), userId, wl.getName(), wl.getReadPermission(), wl.getEditPermission() }));
        return wl;
    }

    public void saveWatchList(final WatchListVO wl) {
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("synthetic-access")
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (wl.getId() == Common.NEW_ID)
                    wl.setId(ejt.doInsert(
                            "INSERT INTO watchLists (xid, name, userId, readPermission, editPermission, type) " //
                                    + "values (?,?,?,?,?, 'static')",
                            new Object[] { wl.getXid(), wl.getName(), wl.getUserId(), wl.getReadPermission(),
                                    wl.getEditPermission() }));
                else
                    ejt2.update("UPDATE watchLists SET xid=?, name=?, readPermission=?, editPermission=? WHERE id=?",
                            new Object[] { wl.getXid(), wl.getName(), wl.getReadPermission(), wl.getEditPermission(),
                                    wl.getId() });
                ejt2.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { wl.getId() });
                ejt2.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public int getBatchSize() {
                        return wl.getPointList().size();
                    }

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, wl.getId());
                        ps.setInt(2, wl.getPointList().get(i).getId());
                        ps.setInt(3, i);
                    }
                });
            }
        });
    }

    public void deleteWatchList(final int watchListId) {
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ejt2.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { watchListId });
                ejt2.update("DELETE FROM selectedWatchList WHERE watchListId=?", new Object[] { watchListId });
                ejt2.update("DELETE FROM watchLists WHERE id=?", new Object[] { watchListId });
            }
        });
    }
	
    /**
     * Stream all results back
     * @param callback
     */
    public void getAll(MappedRowCallback<WatchListVO> callback){
    	query(SELECT_ALL, new Object[]{}, rowMapper, callback);
    }
    
    private final String SELECT_POINT_SUMMARIES = "SELECT dp.xid,dp.name,dp.deviceName,dp.pointFolderId,dp.readPermission,dp.setPermission from dataPoints as dp JOIN watchListPoints wlp ON wlp.dataPointId = dp.id WHERE wlp.watchListId=? order by wlp.sortOrder";
    public List<WatchListDataPointModel> getPointSummaries(int watchListId){
    	return query(SELECT_POINT_SUMMARIES, new Object[]{watchListId}, new RowMapper<WatchListDataPointModel>(){

			@Override
			public WatchListDataPointModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				int i=0;
				WatchListDataPointModel model = new WatchListDataPointModel();
				model.setXid(rs.getString(++i));
				model.setName(rs.getString(++i));
				model.setDeviceName(rs.getString(++i));
				model.setPointFolderId(rs.getInt(++i));
				model.setReadPermission(rs.getString(++i));
				model.setSetPermission(rs.getString(++i));
				return model;
			}
    		
    	});
    }
    
    //TODO Clean this up when issue #824 is fixed so we don't have to hard code all this business
    private final String SELECT_POINTS = 
    "select dp.data, dp.id, dp.xid, dp.dataSourceId, dp.name, dp.deviceName, dp.enabled, dp.pointFolderId, " //
            + "  dp.loggingType, dp.intervalLoggingPeriodType, dp.intervalLoggingPeriod, dp.intervalLoggingType, " //
            + "  dp.tolerance, dp.purgeOverride, dp.purgeType, dp.purgePeriod, dp.defaultCacheSize, " //
            + "  dp.discardExtremeValues, dp.engineeringUnits, dp.readPermission, dp.setPermission, dp.templateId, ds.name, " //
            + "  ds.xid, ds.dataSourceType " //
            + "from dataPoints dp join dataSources ds on ds.id = dp.dataSourceId JOIN watchListPoints wlp ON wlp.dataPointId = dp.id WHERE wlp.watchListId=? order by wlp.sortOrder";
    public void getPoints(int watchListId, final MappedRowCallback<DataPointVO> callback){
    	
    	//Create a row mapper
    	final RowMapper<DataPointVO> pointMapper = new RowMapper<DataPointVO>(){
			@SuppressWarnings("deprecation")
			@Override
			public DataPointVO mapRow(ResultSet rs, int rowNum) throws SQLException {
				int i=0;
	            DataPointVO dp = (DataPointVO) SerializationHelper.readObjectInContext(rs.getBinaryStream(++i));
	            dp.setId(rs.getInt(++i));
	            dp.setXid(rs.getString(++i));
	            dp.setDataSourceId(rs.getInt(++i));
	            dp.setName(rs.getString(++i));
	            dp.setDeviceName(rs.getString(++i));
	            dp.setEnabled(charToBool(rs.getString(++i)));
	            dp.setPointFolderId(rs.getInt(++i));
	            dp.setLoggingType(rs.getInt(++i));
	            dp.setIntervalLoggingPeriodType(rs.getInt(++i));
	            dp.setIntervalLoggingPeriod(rs.getInt(++i));
	            dp.setIntervalLoggingType(rs.getInt(++i));
	            dp.setTolerance(rs.getDouble(++i));
	            dp.setPurgeOverride(charToBool(rs.getString(++i)));
	            dp.setPurgeType(rs.getInt(++i));
	            dp.setPurgePeriod(rs.getInt(++i));
	            dp.setDefaultCacheSize(rs.getInt(++i));
	            dp.setDiscardExtremeValues(charToBool(rs.getString(++i)));
	            dp.setEngineeringUnits(rs.getInt(++i));
	            dp.setReadPermission(rs.getString(++i));
	            dp.setSetPermission(rs.getString(++i));
	            //Because we read 0 for null
	            dp.setTemplateId(rs.getInt(++i));
	            if(rs.wasNull())
	            	dp.setTemplateId(null);

	            // Data source information.
	            dp.setDataSourceName(rs.getString(++i));
	            dp.setDataSourceXid(rs.getString(++i));
	            dp.setDataSourceTypeName(rs.getString(++i));

	            dp.ensureUnitsCorrect();
				return dp;
			}
    		
    	};
    	
    	this.ejt.query(SELECT_POINTS, new Object[]{watchListId}, new RowCallbackHandler(){
    		private int row = 0;
    		
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				callback.row(pointMapper.mapRow(rs, row), row);
				row++;
			}
    		
    	});
    }

    public void delete(int id, String initiatorId) {
        WatchListVO vo = get(id);
        delete(vo, initiatorId);
    }

    @Override
    public void delete(WatchListVO vo) {
        delete(vo, null);
    }

    public void delete(WatchListVO vo, String initiatorId) {
        super.delete(vo);
        handler.notify("delete", vo, initiatorId);
    }

    @Override
    protected void insert(WatchListVO vo) {
        insert(vo, null);
    }

    protected void insert(WatchListVO vo, String initiatorId) {
        super.insert(vo);
        handler.notify("add", vo, initiatorId);
    }

    @Override
    protected void update(WatchListVO vo) {
        update(vo, null);
    }

    protected void update(WatchListVO vo, String initiatorId) {
        super.update(vo);
        handler.notify("update", vo, initiatorId);
    }
    
    @Override
    public void save(WatchListVO vo) {
        save(vo, null);
    }
   
    public void save(final WatchListVO wl, final String initiatorId) {
    	final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("synthetic-access")
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // manually call super insert or update methods so websocket is not notified
                boolean isNew = wl.getId() == Common.NEW_ID;
            	if (isNew) {
            	    WatchListDao.super.insert(wl);
                } else {
                    WatchListDao.super.update(wl);
                }
            	
                ejt2.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { wl.getId() });
                ejt2.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public int getBatchSize() {
                        return wl.getPointList().size();
                    }
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, wl.getId());
                        ps.setInt(2, wl.getPointList().get(i).getId());
                        ps.setInt(3, i);
                    }
                });
                
                // manually trigger websocket after saving points
                if (isNew) {
                    handler.notify("add", wl);
                } else {
                    handler.notify("update", wl);
                }
            }
        });
    }
    
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#getXidPrefix()
	 */
	@Override
	protected String getXidPrefix() {
		return WatchListVO.XID_PREFIX;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#getNewVo()
	 */
	@Override
	public WatchListVO getNewVo() {
		return new WatchListVO();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getTableName()
	 */
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#voToObjectArray(java.lang.Object)
	 */
	@Override
	protected Object[] voToObjectArray(WatchListVO vo) {
		String jsonData = null;
		try{
		    WatchListDbDataModel1 data = new WatchListDbDataModel1();
		    data.query = vo.getQuery();
            data.folderIds = vo.getFolderIds();
            data.params = vo.getParams();
            data.data = vo.getData();
            jsonData =  this.mapper.writeValueAsString(data);
		}catch(JsonProcessingException e){
			LOG.error(e.getMessage(), e);
		}
		
		return new Object[]{
			vo.getXid(),
            vo.getUserId(),
			vo.getName(),
            vo.getReadPermission(),
			vo.getEditPermission(),
			vo.getType(),
			jsonData
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
        map.put("userId", Types.INTEGER);
		map.put("name", Types.VARCHAR);
        map.put("readPermission", Types.VARCHAR);
		map.put("editPermission", Types.VARCHAR);
		map.put("type", Types.VARCHAR);
		map.put("data", Types.CLOB);
		return map;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertiesMap()
	 */
	@Override
	protected Map<String, IntStringPair> getPropertiesMap() {
		Map<String, IntStringPair> map = new HashMap<String, IntStringPair>();
		//So we can filter/sort on username in the models
		map.put("username", new IntStringPair(Types.VARCHAR, "u.username"));
		return map;

	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getRowMapper()
	 */
	@Override
	public RowMapper<WatchListVO> getRowMapper() {
		return rowMapper;
	}
	
    private final WatchListRowMapper rowMapper = new WatchListRowMapper();

    class WatchListRowMapper implements RowMapper<WatchListVO> {
		@Override
        public WatchListVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            WatchListVO wl = new WatchListVO();
            wl.setId(rs.getInt(++i));
            wl.setXid(rs.getString(++i));
            wl.setUserId(rs.getInt(++i));
            wl.setName(rs.getString(++i));
            wl.setReadPermission(rs.getString(++i));
            wl.setEditPermission(rs.getString(++i));
            wl.setType(rs.getString(++i));
            //Read the data
			try{
				Clob c = rs.getClob(++i);
				if(c != null) {
				    WatchListDbDataModel data = mapper.readValue(c.getCharacterStream(), WatchListDbDataModel.class);
				    if (data != null) {
    				    wl.setQuery(data.query);
    				    wl.setFolderIds(data.folderIds);
    				    wl.setParams(data.params);
                        wl.setData(data.data);
				    }
				}
			}catch(Exception e){
				LOG.error(e.getMessage(), e);
			}
            wl.setUsername(rs.getString(++i));
            return wl;
        }
    }

    @JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="version")
    @JsonSubTypes({
        @Type(name = "1", value = WatchListDbDataModel1.class)
    })
    private static abstract class WatchListDbDataModel {
        @JsonProperty
        String query;
        @JsonProperty
        List<Integer> folderIds;
        @JsonProperty
        List<WatchListParameter> params;
        @JsonProperty
        Map<String, Object> data;
    }

    private static class WatchListDbDataModel1 extends WatchListDbDataModel {
    }
}
