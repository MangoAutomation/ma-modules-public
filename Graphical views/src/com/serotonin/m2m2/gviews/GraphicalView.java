/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.gviews.component.CompoundComponent;
import com.serotonin.m2m2.gviews.component.PointComponent;
import com.serotonin.m2m2.gviews.component.ViewComponent;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.validation.StringValidation;

public class GraphicalView implements Serializable, JsonSerializable {
    public static final String XID_PREFIX = "GV_";

    private int id = Common.NEW_ID;
    @JsonProperty
    private String xid;
    @JsonProperty
    private String name;
    private String backgroundFilename;
    private int userId;
    private List<ViewComponent> viewComponents = new CopyOnWriteArrayList<ViewComponent>();
    private int anonymousAccess = ShareUser.ACCESS_NONE;

    @JsonProperty
    private String readPermission;
    @JsonProperty
    private String editPermission;
    
    public void addViewComponent(ViewComponent viewComponent) {
        // Determine an index for the component.
        int min = 0;
        for (ViewComponent vc : viewComponents) {
            if (min < vc.getIndex())
                min = vc.getIndex();
        }
        viewComponent.setIndex(min + 1);

        viewComponents.add(viewComponent);
    }

    public ViewComponent getViewComponent(int index) {
        for (ViewComponent vc : viewComponents) {
            if (vc.getIndex() == index)
                return vc;
        }
        return null;
    }

    public void removeViewComponent(ViewComponent vc) {
        if (vc != null)
            viewComponents.remove(vc);
    }

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    public boolean containsValidVisibleDataPoint(int dataPointId) {
        for (ViewComponent vc : viewComponents) {
            if (vc.containsValidVisibleDataPoint(dataPointId))
                return true;
        }
        return false;
    }

    public DataPointVO findDataPoint(String viewComponentId) {
        for (ViewComponent vc : viewComponents) {
            if (vc.isPointComponent()) {
                if (vc.getId().equals(viewComponentId))
                    return ((PointComponent) vc).tgetDataPoint();
            }
            else if (vc.isCompoundComponent()) {
                PointComponent pc = ((CompoundComponent) vc).findPointComponent(viewComponentId);
                if (pc != null)
                    return pc.tgetDataPoint();
            }
        }
        return null;
    }

    public boolean isOwner(User user) {
        return user.getId() == userId;
    }

    public boolean isEditor(User user) {
        if (isOwner(user))
            return true;
        if (user.isAdmin()) // Admin
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

    
//    public int getUserAccess(User user) {
//        if (user == null)
//            return anonymousAccess;
//
//        if (userId == user.getId())
//            return ShareUser.ACCESS_OWNER;
//
//        for (ShareUser vu : viewUsers) {
//            if (vu.getUserId() == user.getId())
//                return vu.getAccessType();
//        }
//        return ShareUser.ACCESS_NONE;
//    }

    /**
     * This method is used before the view is displayed in order to validate: - that the given user is allowed to access
     * points that back any components - that the points that back components still have valid data types for the
     * components that render them
     */
    public void validateViewComponents(boolean makeReadOnly) {
        User owner = new UserDao().getUser(userId);
        for (ViewComponent viewComponent : viewComponents)
            viewComponent.validateDataPoint(owner, makeReadOnly);
    }

    public String getBackgroundFilename() {
        return backgroundFilename;
    }

    public void setBackgroundFilename(String backgroundFilename) {
        this.backgroundFilename = backgroundFilename;
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
        this.name = name;
    }

    public List<ViewComponent> getViewComponents() {
        return viewComponents;
    }

    public int getAnonymousAccess() {
        return anonymousAccess;
    }

    public void setAnonymousAccess(int anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
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
        else if (StringValidation.isLengthGreaterThan(name, 100))
            response.addMessage("name", new TranslatableMessage("validate.notLongerThan", 100));

        if (StringUtils.isBlank(xid))
            response.addMessage("xid", new TranslatableMessage("validate.required"));
        else if (StringValidation.isLengthGreaterThan(xid, 50))
            response.addMessage("xid", new TranslatableMessage("validate.notLongerThan", 50));
        else if (!new GraphicalViewDao().isXidUnique(xid, id))
            response.addMessage("xid", new TranslatableMessage("validate.xidUsed"));

        for (ViewComponent vc : viewComponents)
            vc.validate(response);
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeObject(viewComponents);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            viewComponents = new CopyOnWriteArrayList<ViewComponent>((List<ViewComponent>) in.readObject());
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("user", new UserDao().getUser(userId).getUsername());
        writer.writeEntry("anonymousAccess", ShareUser.ACCESS_CODES.getCode(anonymousAccess));
        writer.writeEntry("viewComponents", viewComponents);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        if (isNew()) {
            String username = jsonObject.getString("user");
            if (StringUtils.isBlank(username))
                throw new TranslatableJsonException("emport.error.missingValue", "user");
            User user = new UserDao().getUser(username);
            if (user == null)
                throw new TranslatableJsonException("emport.error.missingUser", username);
            userId = user.getId();
        }

        JsonArray components = jsonObject.getJsonArray("viewComponents");
        if (components != null) {
            viewComponents.clear();
            for (JsonValue jv : components)
                addViewComponent(reader.read(ViewComponent.class, jv));
        }

        String text = jsonObject.getString("anonymousAccess");
        if (text != null) {
            anonymousAccess = ShareUser.ACCESS_CODES.getId(text);
            if (anonymousAccess == -1)
                throw new TranslatableJsonException("emport.error.invalid", "anonymousAccess", text,
                        ShareUser.ACCESS_CODES.getCodeList());
        }
    }
}
