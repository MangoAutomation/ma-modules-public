/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.db.pair.IntStringPairRowMapper;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.util.SerializationHelper;

public class GraphicalViewDao extends BaseDao {
    //
    //
    // Views
    //
    private static final String VIEW_SELECT = //
    "select data, id, xid, name, background, userId, anonymousAccess from graphicalViews";
    private static final String USER_ID_COND = //
    " where userId=? or id in (select graphicalViewId from graphicalViewUsers where userId=?)";

    public List<GraphicalView> getViews() {
        List<GraphicalView> views = query(VIEW_SELECT, new ViewRowMapper());
        setViewUsers(views);
        return views;
    }

    public List<GraphicalView> getViews(int userId) {
        List<GraphicalView> views = query(VIEW_SELECT + USER_ID_COND, new Object[] { userId, userId },
                new ViewRowMapper());
        setViewUsers(views);
        return views;
    }

    public List<IntStringPair> getViewNames(int userId) {
        return query("select id, name from graphicalViews" + USER_ID_COND, new Object[] { userId, userId },
                new IntStringPairRowMapper());
    }

    private void setViewUsers(List<GraphicalView> views) {
        for (GraphicalView view : views)
            setViewUsers(view);
    }

    public GraphicalView getView(int id) {
        return getSingleView(VIEW_SELECT + " where id=?", new Object[] { id });
    }

    public GraphicalView getViewByXid(String xid) {
        return getSingleView(VIEW_SELECT + " where xid=?", new Object[] { xid });
    }

    public GraphicalView getView(String name) {
        return getSingleView(VIEW_SELECT + " where name=?", new Object[] { name });
    }

    private GraphicalView getSingleView(String sql, Object[] params) {
        GraphicalView view = queryForObject(sql, params, new ViewRowMapper(), null);
        if (view == null)
            return null;

        setViewUsers(view);
        return view;
    }

    class ViewRowMapper implements RowMapper<GraphicalView> {
        @Override
        public GraphicalView mapRow(ResultSet rs, int rowNum) throws SQLException {
            GraphicalView v;
            Blob blob = rs.getBlob(1);
            if (blob == null)
                // This can happen during upgrade
                v = new GraphicalView();
            else
                v = (GraphicalView) SerializationHelper.readObjectInContext(blob.getBinaryStream());

            v.setId(rs.getInt(2));
            v.setXid(rs.getString(3));
            v.setName(rs.getString(4));
            v.setBackgroundFilename(rs.getString(5));
            v.setUserId(rs.getInt(6));
            v.setAnonymousAccess(rs.getInt(7));

            return v;
        }
    }

    class ViewNameRowMapper implements RowMapper<GraphicalView> {
        @Override
        public GraphicalView mapRow(ResultSet rs, int rowNum) throws SQLException {
            GraphicalView v = new GraphicalView();
            v.setId(rs.getInt(1));
            v.setName(rs.getString(2));
            v.setUserId(rs.getInt(3));

            return v;
        }
    }

    public String generateUniqueXid() {
        return generateUniqueXid(GraphicalView.XID_PREFIX, "graphicalViews");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "graphicalViews");
    }

    public void saveView(final GraphicalView view) {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // Decide whether to insert or update.
                if (view.getId() == Common.NEW_ID)
                    insertView(view);
                else
                    updateView(view);

                saveViewUsers(view);
            }
        });
    }

    void insertView(GraphicalView view) {
        view.setId(doInsert(
                "insert into graphicalViews (xid, name, background, userId, anonymousAccess, data) values (?,?,?,?,?,?)",
                new Object[] { view.getXid(), view.getName(), view.getBackgroundFilename(), view.getUserId(),
                        view.getAnonymousAccess(), SerializationHelper.writeObject(view) }, new int[] { Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.BLOB }));
    }

    void updateView(GraphicalView view) {
        ejt.update("update graphicalViews set xid=?, name=?, background=?, anonymousAccess=?, data=? where id=?",
                new Object[] { view.getXid(), view.getName(), view.getBackgroundFilename(), view.getAnonymousAccess(),
                        SerializationHelper.writeObject(view), view.getId() }, new int[] { Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.BLOB, Types.INTEGER });
    }

    public void removeView(final int viewId) {
        deleteViewUsers(viewId);
        ejt.update("delete from graphicalViews where id=?", new Object[] { viewId });
    }

    //
    //
    // View users
    //
    private void setViewUsers(GraphicalView view) {
        view.setViewUsers(query("select userId, accessType from graphicalViewUsers where graphicalViewId=?",
                new Object[] { view.getId() }, new ViewUserRowMapper()));
    }

    class ViewUserRowMapper implements RowMapper<ShareUser> {
        @Override
        public ShareUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareUser vu = new ShareUser();
            vu.setUserId(rs.getInt(1));
            vu.setAccessType(rs.getInt(2));
            return vu;
        }
    }

    private void deleteViewUsers(int viewId) {
        ejt.update("delete from graphicalViewUsers where graphicalViewId=?", new Object[] { viewId });
    }

    void saveViewUsers(final GraphicalView view) {
        // Delete anything that is currently there.
        deleteViewUsers(view.getId());

        // Add in all of the entries.
        ejt.batchUpdate("insert into graphicalViewUsers values (?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return view.getViewUsers().size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ShareUser vu = view.getViewUsers().get(i);
                ps.setInt(1, view.getId());
                ps.setInt(2, vu.getUserId());
                ps.setInt(3, vu.getAccessType());
            }
        });
    }

    public void removeUserFromView(int viewId, int userId) {
        ejt.update("delete from graphicalViewUsers where graphicalViewId=? and userId=?",
                new Object[] { viewId, userId });
    }
}
