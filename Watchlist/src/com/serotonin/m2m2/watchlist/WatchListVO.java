/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.validation.StringValidation;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 *
 */
public class WatchListVO extends AbstractVO<WatchListVO>{

	private static final long serialVersionUID = 1L;

	public static final String XID_PREFIX = "WL_";
    public static final String DEFAULT_TYPE = "static";

    private int userId;
    //TODO When we remove the legacy code reduce these objects to summaries only
    private final List<DataPointVO> pointList = new CopyOnWriteArrayList<>();
    @JsonProperty
    private String readPermission;
    @JsonProperty
    private String editPermission;
    @JsonProperty
    private String type;
    @JsonProperty
    private String query;
    @JsonProperty
    private List<Integer> folderIds;
    @JsonProperty
    List<WatchListParameter> params;
    private Map<String, Object> data;
    
    //non-persistent members
    private String username;
    
    public WatchListVO() {
        type = DEFAULT_TYPE;
    }

    public boolean isOwner(User user) {
        return user.getId() == userId;
    }

    public boolean isEditor(User user) {
        if (isOwner(user))
            return true;
        if (Permissions.hasAdminPermission(user)) // Admin
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

	public List<Integer> getFolderIds() {
		return folderIds;
	}

	public void setFolderIds(List<Integer> folderIds) {
		this.folderIds = folderIds;
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

	public void validate(ProcessResult response) {
        if (StringUtils.isBlank(name))
            response.addMessage("name", new TranslatableMessage("validate.required"));
        else if (StringValidation.isLengthGreaterThan(name, 50))
            response.addMessage("name", new TranslatableMessage("validate.notLongerThan", 50));

        if (StringUtils.isBlank(xid))
            response.addMessage("xid", new TranslatableMessage("validate.required"));
        else if (StringValidation.isLengthGreaterThan(xid, 50))
            response.addMessage("xid", new TranslatableMessage("validate.notLongerThan", 50));
        else if (!WatchListDao.getInstance().isXidUnique(xid, id))
            response.addMessage("xid", new TranslatableMessage("validate.xidUsed"));

        //Validate the points
        UserDao dao = UserDao.getInstance();
        User user = dao.getUser(userId);
        if(user == null){
            response.addContextualMessage("userId", "watchlists.validate.userDNE");
        }
        
        //Using the owner of the report to validate against permissions if there is no current user
        User currentUser = Common.getUser();
        if(currentUser == null)
     	   currentUser = user;
 
        //Validate Points
        for(DataPointVO vo : pointList)
        	try{
        		Permissions.ensureDataPointReadPermission(user, vo);
        	}catch(PermissionException e){
        		response.addContextualMessage("points", "watchlist.vaildate.pointNoReadPermission", vo.getXid());
        	}
        
        //Validate the permissions
 		Permissions.validateAddedPermissions(this.readPermission, currentUser, response, "readPermission");
		Permissions.validateAddedPermissions(this.editPermission, currentUser, response, "editPermission");
		if(!Permissions.hasPermission(currentUser, editPermission))
		    response.addContextualMessage("editPermission", "validate.mustHaveEditPermission");
		
		//TODO Validate new members
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
    	writer.writeEntry("user", UserDao.getInstance().getUser(userId).getUsername());

        List<String> dpXids = new ArrayList<>();
        for (DataPointVO dpVO : pointList)
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
        User user = UserDao.getInstance().getUser(username);
        if (user == null)
            throw new TranslatableJsonException("emport.error.missingUser", username);
        userId = user.getId();

        JsonArray jsonDataPoints = jsonObject.getJsonArray("dataPoints");
        if (jsonDataPoints != null) {
            pointList.clear();
            DataPointDao dataPointDao = DataPointDao.getInstance();
            for (JsonValue jv : jsonDataPoints) {
                String xid = jv.toString();
                DataPointVO dpVO = dataPointDao.getDataPoint(xid);
                if (dpVO == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                pointList.add(dpVO);
            }
        }
        
        JsonObject o = jsonObject.getJsonObject("data");
		if(o != null)
			this.data = o.toMap();
    }
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.util.ChangeComparable#getTypeKey()
	 */
	@Override
	public String getTypeKey() {
		return "event.audit.watchlist";
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.AbstractVO#getDao()
	 */
	@Override
	protected AbstractDao<WatchListVO> getDao() {
		return WatchListDao.getInstance();
	}

}
