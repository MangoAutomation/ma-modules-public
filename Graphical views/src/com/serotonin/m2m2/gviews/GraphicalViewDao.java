/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.util.SerializationHelper;

public class GraphicalViewDao extends BaseDao {
    //
    //
    // Views
    //
    private static final String VIEW_SELECT = //
    "select data, id, xid, name, background, userId, readPermission, setPermission, editPermission, anonymousAccess from graphicalViews";

    public List<GraphicalView> getViews() {
        List<GraphicalView> views = query(VIEW_SELECT, new ViewRowMapper());
        return views;
    }

    /**
     * Filter the User allowed views for the provided user
     * @param user
     * @return
     */
    public List<GraphicalView> getViews(User user) {
        List<GraphicalView> views = query(VIEW_SELECT, new ViewRowMapper());
        List<GraphicalView> userViews = new ArrayList<GraphicalView>();
        //Filtering on user
        for(GraphicalView view : views){
        	if(view.isReader(user)||view.isSetter(user))
        		userViews.add(view);
        }
        return userViews;
    }
    
    /**
     * Filter a list of View id-name pairs that a user has read access to
     * @param user
     * @return
     */
    public List<IntStringPair> getViewNames(User user) {
        List<GraphicalView> views = query(VIEW_SELECT, new ViewRowMapper());
        List<IntStringPair> userViews = new ArrayList<IntStringPair>();
        //Filtering on user
        for(GraphicalView view : views){
        	if(view.isReader(user)||view.isSetter(user)){
        		userViews.add(new IntStringPair(view.getId(), view.getName()));
        	}
        }
        return userViews;
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
        return queryForObject(sql, params, new ViewRowMapper(), null);
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
            v.setReadPermission(rs.getString(7));
            v.setSetPermission(rs.getString(8));
            v.setEditPermission(rs.getString(9));
            v.setAnonymousAccess(rs.getInt(10));

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
            }
        });
    }

    void insertView(GraphicalView view) {
        view.setId(doInsert(
                "insert into graphicalViews (xid, name, background, userId, anonymousAccess, readPermission, setPermission, editPermission, data) values (?,?,?,?,?,?,?,?,?)",
                new Object[] { view.getXid(), view.getName(), view.getBackgroundFilename(), view.getUserId(),
                        view.getAnonymousAccess(), view.getReadPermission(), view.getSetPermission(), view.getEditPermission(), SerializationHelper.writeObject(view) }, new int[] { Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB }));
    }

    void updateView(GraphicalView view) {
        ejt.update("update graphicalViews set xid=?, name=?, background=?, anonymousAccess=?, readPermission=?, setPermission=?, editPermission=?, data=? where id=?",
                new Object[] { view.getXid(), view.getName(), view.getBackgroundFilename(), view.getAnonymousAccess(),
        				view.getReadPermission(), view.getSetPermission(), view.getEditPermission(),
                        SerializationHelper.writeObject(view), view.getId() }, new int[] { Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB, Types.INTEGER });
    }

    public void removeView(final int viewId) {
        ejt.update("delete from graphicalViews where id=?", new Object[] { viewId });
    }


 
}
