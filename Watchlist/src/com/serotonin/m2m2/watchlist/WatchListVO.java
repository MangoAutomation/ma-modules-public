/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.role.Role;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 *
 */
public class WatchListVO extends AbstractVO {

    private static final long serialVersionUID = 1L;

    public static final String XID_PREFIX = "WL_";
    public static final String STATIC_TYPE = "static"; //current types also include hierarchy, query, and tags
    public static final String QUERY_TYPE = "query";
    public static final String TAGS_TYPE = "tags";

    private int userId;
    private final List<IDataPoint> pointList = new CopyOnWriteArrayList<>();
    @JsonProperty
    private Set<Role> readRoles = Collections.emptySet();
    @JsonProperty
    private Set<Role> editRoles = Collections.emptySet();
    @JsonProperty
    private String type;
    @JsonProperty
    private String query;
    @JsonProperty
    List<WatchListParameter> params;
    private Map<String, Object> data;

    public WatchListVO() {
        type = STATIC_TYPE;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getXid() {
        return xid;
    }

    @Override
    public void setXid(String xid) {
        this.xid = xid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name == null)
            this.name = "";
        else
            this.name = name;
    }

    public List<IDataPoint> getPointList() {
        return pointList;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Set<Role> getReadRoles() {
        return readRoles;
    }

    public void setReadRoles(Set<Role> readRoles) {
        this.readRoles = readRoles;
    }

    public Set<Role> getEditRoles() {
        return editRoles;
    }

    public void setEditRoles(Set<Role> editRoles) {
        this.editRoles = editRoles;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<WatchListParameter> getParams() {
        return params;
    }

    public void setParams(List<WatchListParameter> params) {
        this.params = params;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("user", UserDao.getInstance().getXidById(userId));

        List<String> dpXids = new ArrayList<>();
        for (IDataPoint dpVO : pointList)
            dpXids.add(dpVO.getXid());
        writer.writeEntry("dataPoints", dpXids);
        writer.writeEntry("data", this.data);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        String username = jsonObject.getString("user");
        if (StringUtils.isBlank(username))
            throw new TranslatableJsonException("emport.error.missingValue", "user");
        Integer user = UserDao.getInstance().getIdByXid(username);
        if (user == null)
            throw new TranslatableJsonException("emport.error.missingUser", username);
        userId = user;

        JsonArray jsonDataPoints = jsonObject.getJsonArray("dataPoints");
        if (jsonDataPoints != null) {
            pointList.clear();
            DataPointDao dataPointDao = DataPointDao.getInstance();
            for (JsonValue jv : jsonDataPoints) {
                String xid = jv.toString();
                DataPointSummary dpVO = dataPointDao.getSummary(xid);
                if (dpVO == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                pointList.add(dpVO);
            }
        }

        JsonObject o = jsonObject.getJsonObject("data");
        if(o != null)
            this.data = o.toMap();

        //Legacy permissions support
        this.readRoles = readLegacyPermissions("readPermissions", this.readRoles, jsonObject);
        this.editRoles = readLegacyPermissions("editPermissions", this.editRoles, jsonObject);

    }

    @Override
    public String getTypeKey() {
        return "event.audit.watchlist";
    }
}
