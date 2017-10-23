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
import com.infiniteautomation.mango.db.query.JoinClause;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.SchemaDefinition;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 */
public class WatchListDao extends AbstractDao<WatchListVO> {
    
	public static String TABLE_NAME = "watchLists";
	public static WatchListDao instance = new WatchListDao();
	DaoNotificationWebSocketHandler<WatchListVO> wsHandler;
	
    /**
     * Pass null through to super constructor for websocket handler so we have full control over where it is notified
     */
    @SuppressWarnings("unchecked")
    private WatchListDao() {
		super((DaoNotificationWebSocketHandler<WatchListVO>) null,
				AuditEvent.TYPE_NAME, "w",
		        new String[] {"u.username"}, //to allow filtering on username
		        false,
		        new TranslatableMessage("internal.monitor.WATCHLIST_COUNT")
		        );
        wsHandler = (DaoNotificationWebSocketHandler<WatchListVO>) ModuleRegistry.getWebSocketHandlerDefinition(WatchListWebSocketDefinition.TYPE_NAME).getHandlerInstance();
	}

	
    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchListVO> getWatchLists(final User user) {
        final List<WatchListVO> result = new ArrayList<>();
        query(SELECT_ALL_FIXED_SORT, new Object[0], rowMapper, new MappedRowCallback<WatchListVO>() {
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
        return getAll();
    }

    public WatchListVO getWatchList(int watchListId) {
        WatchListVO watchList = get(watchListId);
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
        DataPointDao dataPointDao = DataPointDao.instance;
        for (Integer pointId : pointIds)
            points.add(dataPointDao.getDataPoint(pointId));
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public WatchListVO getWatchList(String xid) {
        return getByXid(xid);
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
        save(wl);
        return wl;
    }

    public void saveWatchList(final WatchListVO wl) {
        save(wl);
    }

    public void deleteWatchList(final int watchListId) {
        delete(watchListId);
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

    private final String SELECT_POINTS = DataPointDao.instance.getSelectAllSql() + " JOIN watchListPoints wlp ON wlp.dataPointId = dp.id WHERE wlp.watchListId=? order by wlp.sortOrder";
    
    /**
     * Get the Data Points for a Given Watchlist
     * @param watchListId
     * @param callback
     */
    public void getPoints(int watchListId, final MappedRowCallback<DataPointVO> callback){

    	RowMapper<DataPointVO> pointMapper = DataPointDao.instance.getRowMapper();

    	this.ejt.query(SELECT_POINTS, new Object[]{watchListId}, new RowCallbackHandler(){
    		private int row = 0;
    		
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				callback.row(pointMapper.mapRow(rs, row), row);
				row++;
			}
    		
    	});
    }
    
    @Override
    public void delete(WatchListVO vo, String initiatorId) {
        super.delete(vo, initiatorId);
        wsHandler.notify("delete", vo, initiatorId);
    }

    @Override
    protected void insert(WatchListVO vo, String initiatorId) {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                WatchListDao.super.insert(vo, initiatorId);
                
                ejt.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new InsertPoints(vo));
                
                // manually trigger websocket after saving points
                wsHandler.notify("add", vo, initiatorId);
            }
        });
    }
    
    @Override
    protected void update(WatchListVO vo, String initiatorId, String originalXid) {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                String oldXid = originalXid;
                if (oldXid == null) {
                    oldXid = getXid(vo.getId());
                }

                WatchListDao.super.update(vo, initiatorId, oldXid);

                ejt.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { vo.getId() });
                ejt.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new InsertPoints(vo));

                // manually trigger websocket after saving points
                wsHandler.notify("update", vo, initiatorId, oldXid);
            }
        });
    }

    protected String getXid(int id) {
        return ejt.queryForObject("SELECT xid FROM " + tableName + " WHERE id=?", String.class, id);
    }
    
    private static class InsertPoints implements BatchPreparedStatementSetter {
        WatchListVO vo;
        
        InsertPoints(WatchListVO vo) {
            this.vo = vo;
        }
        
        @Override
        public int getBatchSize() {
            return vo.getPointList().size();
        }
        
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getPointList().get(i).getId());
            ps.setInt(3, i);
        }
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
            jsonData =  this.getObjectWriter(WatchListDbDataModel1.class).writeValueAsString(data);
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
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getJoins()
	 */
	@Override
	protected List<JoinClause> getJoins() {
    	List<JoinClause> joins = new ArrayList<JoinClause>();
    	joins.add(new JoinClause(JOIN, SchemaDefinition.USERS_TABLE, "u", "w.userId = u.id"));
    	return joins;
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
				    WatchListDbDataModel data = getObjectReader(WatchListDbDataModel.class).readValue(c.getCharacterStream());
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

    @JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="version", defaultImpl=WatchListDbDataModel1.class)
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
