/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.rt.event.type.AuditEventType;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkDao extends BaseDao {
    public String generateUniqueXid() {
        return generateUniqueXid(PointLinkVO.XID_PREFIX, "pointLinks");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "pointLinks");
    }

    private static final String POINT_LINK_SELECT = "select id, xid, sourcePointId, targetPointId, script, eventType, writeAnnotation, disabled from pointLinks ";

    public List<PointLinkVO> getPointLinks() {
        return query(POINT_LINK_SELECT, new PointLinkRowMapper());
    }

    public List<PointLinkVO> getPointLinksForPoint(int dataPointId) {
        return query(POINT_LINK_SELECT + "where sourcePointId=? or targetPointId=?", new Object[] { dataPointId,
                dataPointId }, new PointLinkRowMapper());
    }

    public PointLinkVO getPointLink(int id) {
        return queryForObject(POINT_LINK_SELECT + "where id=?", new Object[] { id }, new PointLinkRowMapper(), null);
    }

    public PointLinkVO getPointLink(String xid) {
        return queryForObject(POINT_LINK_SELECT + "where xid=?", new Object[] { xid }, new PointLinkRowMapper(), null);
    }

    class PointLinkRowMapper implements RowMapper<PointLinkVO> {
        @Override
        public PointLinkVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointLinkVO pl = new PointLinkVO();
            int i = 0;
            pl.setId(rs.getInt(++i));
            pl.setXid(rs.getString(++i));
            pl.setSourcePointId(rs.getInt(++i));
            pl.setTargetPointId(rs.getInt(++i));
            pl.setScript(rs.getString(++i));
            pl.setEvent(rs.getInt(++i));
            pl.setWriteAnnotation(charToBool(rs.getString(++i)));
            pl.setDisabled(charToBool(rs.getString(++i)));
            return pl;
        }
    }

    public void savePointLink(final PointLinkVO pl) {
        if (pl.getId() == Common.NEW_ID)
            insertPointLink(pl);
        else
            updatePointLink(pl);
    }

    private static final String POINT_LINK_INSERT = //
    "insert into pointLinks (xid, sourcePointId, targetPointId, script, eventType, writeAnnotation, disabled) "
            + "values (?,?,?,?,?,?,?)";

    private void insertPointLink(PointLinkVO pl) {
        int id = doInsert(POINT_LINK_INSERT, new Object[] { pl.getXid(), pl.getSourcePointId(), pl.getTargetPointId(),
                pl.getScript(), pl.getEvent(), boolToChar(pl.isWriteAnnotation()), boolToChar(pl.isDisabled()) });
        pl.setId(id);
        AuditEventType.raiseAddedEvent(AuditEvent.TYPE_NAME, pl);
    }

    private static final String POINT_LINK_UPDATE = //
    "update pointLinks set xid=?, sourcePointId=?, targetPointId=?, script=?, eventType=?, writeAnnotation=?, disabled=? "
            + "where id=?";

    private void updatePointLink(PointLinkVO pl) {
        PointLinkVO old = getPointLink(pl.getId());

        ejt.update(POINT_LINK_UPDATE,
                new Object[] { pl.getXid(), pl.getSourcePointId(), pl.getTargetPointId(), pl.getScript(),
                        pl.getEvent(), boolToChar(pl.isWriteAnnotation()), boolToChar(pl.isDisabled()), pl.getId() });

        AuditEventType.raiseChangedEvent(AuditEvent.TYPE_NAME, old, pl);
    }

    public void deletePointLink(final int pointLinkId) {
        PointLinkVO pl = getPointLink(pointLinkId);
        if (pl != null) {
            ejt.update("delete from pointLinks where id=?", new Object[] { pointLinkId });
            AuditEventType.raiseDeletedEvent(AuditEvent.TYPE_NAME, pl);
        }
    }
}
