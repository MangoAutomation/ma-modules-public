/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.validation.StringValidation;

/**
 * @author Matthew Lohbihler
 */
public class WatchList implements JsonSerializable {
    public static final String XID_PREFIX = "WL_";

    private int id = Common.NEW_ID;
    @JsonProperty
    private String xid;
    private int userId;
    @JsonProperty
    private String name;
    private final List<DataPointVO> pointList = new CopyOnWriteArrayList<>();
    @JsonProperty
    private String readPermission;
    @JsonProperty
    private String editPermission;

    public boolean isOwner(User user) {
        return user.getId() == userId;
    }

    public boolean isEditor(User user) {
        if (isOwner(user))
            return true;
        if (Permissions.hasAdmin(user)) // Admin
            return true;
        return Permissions.hasPermission(user, editPermission); // Edit group
    }

    public boolean isReader(User user) {
        if (isEditor(user))
            return true;
        return Permissions.hasPermission(user, readPermission); // Read group
    }

    public String getUserAccess(User user) {
        if (isEditor(user))
            return "edit";
        if (isReader(user))
            return "read";
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null)
            this.name = "";
        else
            this.name = name;
    }

    public List<DataPointVO> getPointList() {
        return pointList;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(String readPermission) {
        this.readPermission = readPermission;
    }

    public String getEditPermission() {
        return editPermission;
    }

    public void setEditPermission(String editPermission) {
        this.editPermission = editPermission;
    }

    public void validate(ProcessResult response) {
        if (StringUtils.isBlank(name))
            response.addMessage("name", new TranslatableMessage("validate.required"));
        else if (StringValidation.isLengthGreaterThan(name, 50))
            response.addMessage("name", new TranslatableMessage("validate.notLongerThan", 50));

        if (StringUtils.isBlank(xid))
            response.addMessage("xid", new TranslatableMessage("validate.required"));
        else if (StringValidation.isLengthGreaterThan(xid, 50))
            response.addMessage("xid", new TranslatableMessage("validate.notLongerThan", 50));
        else if (!new WatchListDao().isXidUnique(xid, id))
            response.addMessage("xid", new TranslatableMessage("validate.xidUsed"));
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("user", new UserDao().getUser(userId).getUsername());

        List<String> dpXids = new ArrayList<>();
        for (DataPointVO dpVO : pointList)
            dpXids.add(dpVO.getXid());
        writer.writeEntry("dataPoints", dpXids);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String username = jsonObject.getString("user");
        if (StringUtils.isBlank(username))
            throw new TranslatableJsonException("emport.error.missingValue", "user");
        User user = new UserDao().getUser(username);
        if (user == null)
            throw new TranslatableJsonException("emport.error.missingUser", username);
        userId = user.getId();

        JsonArray jsonDataPoints = jsonObject.getJsonArray("dataPoints");
        if (jsonDataPoints != null) {
            pointList.clear();
            DataPointDao dataPointDao = new DataPointDao();
            for (JsonValue jv : jsonDataPoints) {
                String xid = jv.toString();
                DataPointVO dpVO = dataPointDao.getDataPoint(xid);
                if (dpVO == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                pointList.add(dpVO);
            }
        }
    }
}
