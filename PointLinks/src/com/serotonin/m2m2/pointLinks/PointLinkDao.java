/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.infiniteautomation.mango.util.LazyInitSupplier;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.log.LogLevel;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Matthew Lohbihler
 */
@Repository
public class PointLinkDao extends AbstractDao<PointLinkVO> {
	
    private static final LazyInitSupplier<PointLinkDao> springInstance = new LazyInitSupplier<>(() -> {
        Object o = Common.getRuntimeContext().getBean(PointLinkDao.class);
        if(o == null)
            throw new ShouldNeverHappenException("DAO not initialized in Spring Runtime Context");
        return (PointLinkDao)o;
    });
    
    private PointLinkDao() {
        super(AuditEvent.TYPE_NAME, "pl", new String[] {}, false, new TranslatableMessage("header.pointLinks"));
    }
	
    public static PointLinkDao getInstance() {
        return springInstance.get();
    }
	

    public List<PointLinkVO> getPointLinksForPoint(int dataPointId) {
        return query(SELECT_ALL + " where sourcePointId=? or targetPointId=?", new Object[] { dataPointId,
                dataPointId }, new PointLinkRowMapper());
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertiesMap()
     */
    @Override
    protected Map<String, IntStringPair> getPropertiesMap() {
        HashMap<String, IntStringPair> map = new HashMap<String, IntStringPair>();
        map.put("event", new IntStringPair(Types.INTEGER, "eventType"));
        map.put("logLevel", new IntStringPair(Types.INTEGER, "logLevel"));
        return map;
    }
    
    @Override
    protected Map<String, Function<Object, Object>> createValueConverterMap() {
        Map<String, Function<Object, Object>> map = new HashMap<>();
        map.put("eventType", (code) -> {
            return PointLinkVO.EVENT_CODES.getId((String)code);
        });
        
        return map;
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertyTypeMap()
     */
    @Override
    protected LinkedHashMap<String, Integer> getPropertyTypeMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("id", Types.INTEGER);
        map.put("xid", Types.VARCHAR);
        map.put("name", Types.VARCHAR);
        map.put("sourcePointId", Types.INTEGER);
        map.put("targetPointId", Types.INTEGER);
        map.put("script", Types.CLOB);
        map.put("eventType", Types.INTEGER);
        map.put("writeAnnotation", Types.CHAR);
        map.put("disabled", Types.CHAR);
        map.put("logLevel", Types.INTEGER);
        map.put("logSize", Types.DOUBLE);
        map.put("logCount", Types.INTEGER);
        map.put("scriptPermissions", Types.VARCHAR);
        return map;
    }

    @Override
    protected String getXidPrefix() {
        return PointLinkVO.XID_PREFIX;
    }

    @Override
    public PointLinkVO getNewVo() {
        return new PointLinkVO();
    }

    @Override
    protected String getTableName() {
        return PointLinkSchemaDefinition.TABLE_NAME;
    }

    @Override
    protected Object[] voToObjectArray(PointLinkVO vo) {
        return new Object[] {
                vo.getXid(),
                vo.getName(),
                vo.getSourcePointId(),
                vo.getTargetPointId(),
                vo.getScript(),
                vo.getEvent(),
                boolToChar(vo.isWriteAnnotation()),
                boolToChar(vo.isDisabled()),
                vo.getLogLevel(),
                vo.getLogSize(),
                vo.getLogCount(),
                vo.getScriptPermissions().getPermissions()
        };
    }
    
    @Override
    public RowMapper<PointLinkVO> getRowMapper() {
        return new PointLinkRowMapper();
    }
    class PointLinkRowMapper implements RowMapper<PointLinkVO> {
        @Override
        public PointLinkVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointLinkVO pl = new PointLinkVO();
            int i = 0;
            pl.setId(rs.getInt(++i));
            pl.setXid(rs.getString(++i));
            pl.setName(rs.getString(++i));
            pl.setSourcePointId(rs.getInt(++i));
            pl.setTargetPointId(rs.getInt(++i));
            pl.setScript(rs.getString(++i));
            pl.setEvent(rs.getInt(++i));
            pl.setWriteAnnotation(charToBool(rs.getString(++i)));
            pl.setDisabled(charToBool(rs.getString(++i)));
            pl.setLogLevel(LogLevel.fromValue(rs.getInt(++i)));
            pl.setLogSize(rs.getFloat(++i));
            pl.setLogCount(rs.getInt(++i));
            pl.setScriptPermissions(new ScriptPermissions(Permissions.explodePermissionGroups(rs.getString(++i))));
            return pl;
        }
    }



}
