/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Matthew Lohbihler
 */
public class WatchListDao extends BaseDao {
    private static final String SELECT = "SELECT w.id, w.xid, w.userId, w.name, w.readPermission, w.editPermission " //
            + "FROM watchLists w ";

    public String generateUniqueXid() {
        return generateUniqueXid(WatchList.XID_PREFIX, "watchLists");
    }

    public boolean isXidUnique(String xid, int watchListId) {
        return isXidUnique(xid, watchListId, "watchLists");
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists(final User user) {
        final List<WatchList> result = new ArrayList<>();
        query(SELECT + "ORDER BY w.name", new Object[0], rowMapper, new MappedRowCallback<WatchList>() {
            @Override
            public void row(WatchList wl, int index) {
                if (wl.isReader(user))
                    result.add(wl);
            }
        });
        return result;
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists() {
        return query(SELECT + "ORDER BY w.name", rowMapper);
    }

    public WatchList getWatchList(int watchListId) {
        // Get the watch lists.
        WatchList watchList = queryForObject(SELECT + "WHERE w.id=?", new Object[] { watchListId }, rowMapper);
        populateWatchlistData(watchList);
        return watchList;
    }

    public void populateWatchlistData(List<WatchList> watchLists) {
        for (WatchList watchList : watchLists)
            populateWatchlistData(watchList);
    }

    public void populateWatchlistData(WatchList watchList) {
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
    public WatchList getWatchList(String xid) {
        return queryForObject(SELECT + " WHERE w.xid=?", new Object[] { xid }, rowMapper, null);
    }

    public WatchList getSelectedWatchList(int userId) {
        WatchList watchList = queryForObject(
                SELECT + "JOIN selectedWatchList s ON s.watchListId=w.id WHERE s.userId=?", new Object[] { userId },
                rowMapper, null);
        populateWatchlistData(watchList);
        return watchList;
    }

    private final WatchListRowMapper rowMapper = new WatchListRowMapper();

    class WatchListRowMapper implements RowMapper<WatchList> {
        @Override
        public WatchList mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            WatchList wl = new WatchList();
            wl.setId(rs.getInt(++i));
            wl.setXid(rs.getString(++i));
            wl.setUserId(rs.getInt(++i));
            wl.setName(rs.getString(++i));
            wl.setReadPermission(rs.getString(++i));
            wl.setEditPermission(rs.getString(++i));
            return wl;
        }
    }

    public void saveSelectedWatchList(int userId, int watchListId) {
        int count = ejt.update("UPDATE selectedWatchList SET watchListId=? WHERE userId=?", new Object[] { watchListId,
                userId });
        if (count == 0)
            ejt.update("INSERT INTO selectedWatchList (userId, watchListId) VALUES (?,?)", new Object[] { userId,
                    watchListId });
    }

    public WatchList createNewWatchList(WatchList wl, int userId) {
        wl.setUserId(userId);
        wl.setXid(generateUniqueXid());
        wl.setId(ejt.doInsert(
                "INSERT INTO watchLists (xid, userId, name, readPermission, editPermission) VALUES (?,?,?,?,?)",
                new Object[] { wl.getXid(), userId, wl.getName(), wl.getReadPermission(), wl.getEditPermission() }));
        return wl;
    }

    public void saveWatchList(final WatchList wl) {
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("synthetic-access")
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (wl.getId() == Common.NEW_ID)
                    wl.setId(ejt.doInsert(
                            "INSERT INTO watchLists (xid, name, userId, readPermission, editPermission) " //
                                    + "values (?,?,?,?,?)",
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

    //
    //
    // Watch list users
    //
    class WatchListUserRowMapper implements RowMapper<ShareUser> {
        @Override
        public ShareUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareUser wlu = new ShareUser();
            wlu.setUserId(rs.getInt(1));
            wlu.setAccessType(rs.getInt(2));
            return wlu;
        }
    }
}
