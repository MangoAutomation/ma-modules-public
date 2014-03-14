/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Matthew Lohbihler
 */
public class WatchListDao extends BaseDao {
    public String generateUniqueXid() {
        return generateUniqueXid(WatchList.XID_PREFIX, "watchLists");
    }

    public boolean isXidUnique(String xid, int watchListId) {
        return isXidUnique(xid, watchListId, "watchLists");
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists(final int userId) {
        return query("select id, xid, userId, name from watchLists " //
                + "where userId=? or id in (select watchListId from watchListUsers where userId=?)" //
                + "order by name", new Object[] { userId, userId }, new WatchListRowMapper());
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public List<WatchList> getWatchLists() {
        return query("select id, xid, userId, name from watchLists", new WatchListRowMapper());
    }

    public WatchList getWatchList(int watchListId) {
        // Get the watch lists.
        WatchList watchList = queryForObject("select id, xid, userId, name from watchLists where id=?",
                new Object[] { watchListId }, new WatchListRowMapper());
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
                "select dataPointId from watchListPoints where watchListId=? order by sortOrder",
                new Object[] { watchList.getId() }, Integer.class);
        List<DataPointVO> points = watchList.getPointList();
        DataPointDao dataPointDao = new DataPointDao();
        for (Integer pointId : pointIds)
            points.add(dataPointDao.getDataPoint(pointId));

        setWatchListUsers(watchList);
    }

    /**
     * Note: this method only returns basic watchlist information. No data points or share users.
     */
    public WatchList getWatchList(String xid) {
        return queryForObject("select id, xid, userId, name from watchLists where xid=?", new Object[] { xid },
                new WatchListRowMapper(), null);
    }

    public WatchList getSelectedWatchList(int userId) {
        WatchList watchList = queryForObject("select w.id, w.xid, w.userId, w.name "
                + "from watchLists w join selectedWatchList s on s.watchListId=w.id where s.userId=?",
                new Object[] { userId }, new WatchListRowMapper(), null);
        populateWatchlistData(watchList);
        return watchList;
    }

    class WatchListRowMapper implements RowMapper<WatchList> {
        @Override
        public WatchList mapRow(ResultSet rs, int rowNum) throws SQLException {
            WatchList wl = new WatchList();
            wl.setId(rs.getInt(1));
            wl.setXid(rs.getString(2));
            wl.setUserId(rs.getInt(3));
            wl.setName(rs.getString(4));
            return wl;
        }
    }

    public void saveSelectedWatchList(int userId, int watchListId) {
        int count = ejt.update("update selectedWatchList set watchListId=? where userId=?", new Object[] { watchListId,
                userId });
        if (count == 0)
            ejt.update("insert into selectedWatchList (userId, watchListId) values (?,?)", new Object[] { userId,
                    watchListId });
    }

    public WatchList createNewWatchList(WatchList watchList, int userId) {
        watchList.setUserId(userId);
        watchList.setXid(generateUniqueXid());
        watchList.setId(doInsert("insert into watchLists (xid, userId, name) values (?,?,?)",
                new Object[] { watchList.getXid(), userId, watchList.getName() }));
        return watchList;
    }

    public void saveWatchList(final WatchList watchList) {
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("synthetic-access")
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (watchList.getId() == Common.NEW_ID)
                    watchList.setId(doInsert("insert into watchLists (xid, name, userId) values (?,?,?)", new Object[] {
                            watchList.getXid(), watchList.getName(), watchList.getUserId() }));
                else
                    ejt2.update("update watchLists set xid=?, name=? where id=?", new Object[] { watchList.getXid(),
                            watchList.getName(), watchList.getId() });
                ejt2.update("delete from watchListPoints where watchListId=?", new Object[] { watchList.getId() });
                ejt2.batchUpdate("insert into watchListPoints values (?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public int getBatchSize() {
                        return watchList.getPointList().size();
                    }

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, watchList.getId());
                        ps.setInt(2, watchList.getPointList().get(i).getId());
                        ps.setInt(3, i);
                    }
                });

                saveWatchListUsers(watchList);
            }
        });
    }

    public void deleteWatchList(final int watchListId) {
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ejt2.update("delete from watchListPoints where watchListId=?", new Object[] { watchListId });
                ejt2.update("delete from watchListUsers where watchListId=?", new Object[] { watchListId });
                ejt2.update("delete from selectedWatchList where watchListId=?", new Object[] { watchListId });
                ejt2.update("delete from watchLists where id=?", new Object[] { watchListId });
            }
        });
    }

    //
    //
    // Watch list users
    //
    private void setWatchListUsers(WatchList watchList) {
        watchList.setWatchListUsers(query("select userId, accessType from watchListUsers where watchListId=?",
                new Object[] { watchList.getId() }, new WatchListUserRowMapper()));
    }

    class WatchListUserRowMapper implements RowMapper<ShareUser> {
        @Override
        public ShareUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareUser wlu = new ShareUser();
            wlu.setUserId(rs.getInt(1));
            wlu.setAccessType(rs.getInt(2));
            return wlu;
        }
    }

    private void deleteWatchListUsers(int watchListId) {
        ejt.update("delete from watchListUsers where watchListId=?", new Object[] { watchListId });
    }

    void saveWatchListUsers(final WatchList watchList) {
        // Delete anything that is currently there.
        deleteWatchListUsers(watchList.getId());

        // Add in all of the entries.
        ejt.batchUpdate("insert into watchListUsers values (?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return watchList.getWatchListUsers().size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ShareUser wlu = watchList.getWatchListUsers().get(i);
                ps.setInt(1, watchList.getId());
                ps.setInt(2, wlu.getUserId());
                ps.setInt(3, wlu.getAccessType());
            }
        });
    }

    public void removeUserFromWatchList(int watchListId, int userId) {
        ejt.update("delete from watchListUsers where watchListId=? and userId=?", new Object[] { watchListId, userId });
    }
}
