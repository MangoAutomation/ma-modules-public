/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.RoleDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.IDataPoint;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.vo.role.RoleVO;

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

    private final List<IDataPoint> pointList = new CopyOnWriteArrayList<>();
    private MangoPermission readPermission = new MangoPermission();
    private MangoPermission editPermission = new MangoPermission();
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

    public MangoPermission getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(MangoPermission readPermission) {
        this.readPermission = readPermission;
    }

    public MangoPermission getEditPermission() {
        return editPermission;
    }

    public void setEditPermission(MangoPermission editPermission) {
        this.editPermission = editPermission;
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
        writer.writeEntry("readPermission", readPermission);
        writer.writeEntry("editPermission", editPermission);

        List<String> dpXids = new ArrayList<>();
        for (IDataPoint dpVO : pointList)
            dpXids.add(dpVO.getXid());
        writer.writeEntry("dataPoints", dpXids);
        writer.writeEntry("data", this.data);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        JsonValue read = jsonObject.get("readPermission");
        if(read != null) {
            this.readPermission = reader.read(MangoPermission.class, read);
        }

        JsonValue edit = jsonObject.get("editPermission");
        if(edit != null) {
            this.editPermission = reader.read(MangoPermission.class, edit);
        }

        if(jsonObject.containsKey("user")){
            String username = jsonObject.getString("user");
            if (StringUtils.isBlank(username))
                throw new TranslatableJsonException("emport.error.missingValue", "user");
            User user = UserDao.getInstance().getByXid(username);
            if (user == null) {
                throw new TranslatableJsonException("emport.error.missingUser", username);
            }else if(!Common.getBean(PermissionService.class).hasAdminRole(user)) {

                RoleDao dao = Common.getBean(RoleDao.class);
                String name = jsonObject.getString("name", new TranslatableMessage("header.watchlist").translate(user.getTranslations()));

                //Create a role for this user to be able to edit this item
                String editName = new TranslatableMessage("watchList.watchListEditRolePrefix", name).translate(user.getTranslations());

                RoleVO editRole = new RoleVO(Common.NEW_ID, UUID.randomUUID().toString(), editName);
                dao.insert(editRole);
                Set<Set<Role>> editRoles = new HashSet<>(this.editPermission.getRoles());
                editRoles.add(Collections.singleton(editRole.getRole()));
                this.editPermission = new MangoPermission(this.editPermission.getId(), editRoles);

                //Create a role for this user to be able to read this item
                String readName = new TranslatableMessage("watchList.watchListReadRolePrefix", name).translate(user.getTranslations());
                RoleVO readRole = new RoleVO(Common.NEW_ID, UUID.randomUUID().toString(), readName);
                dao.insert(readRole);
                Set<Set<Role>> readRoles = new HashSet<>(this.readPermission.getRoles());
                readRoles.add(Collections.singleton(readRole.getRole()));
                this.readPermission = new MangoPermission(this.readPermission.getId(), readRoles);

                //Update the user to have this role
                UserDao userDao = Common.getBean(UserDao.class);
                Set<Role> newUserRoles = new HashSet<>(user.getRoles());
                newUserRoles.add(editRole.getRole());
                newUserRoles.add(readRole.getRole());
                user.setRoles(newUserRoles);
                userDao.update(user.getId(), user);
            }
        }

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
    }

    @Override
    public String getTypeKey() {
        return "event.audit.watchlist";
    }
}
