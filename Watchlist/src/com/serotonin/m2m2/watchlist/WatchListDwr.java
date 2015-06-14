/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.directwebremoting.WebContextFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.hierarchy.PointHierarchy;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.EmportDwr;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.beans.DataExportDefinition;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import com.serotonin.m2m2.web.taglib.Functions;

public class WatchListDwr extends ModuleDwr {
    @DwrPermission(user = true)
    public Map<String, Object> init() {
        DataPointDao dataPointDao = new DataPointDao();
        Map<String, Object> data = new HashMap<String, Object>();

        PointHierarchy ph = dataPointDao.getPointHierarchy(true).copyFoldersOnly();
        User user = Common.getUser();
        List<DataPointVO> points = dataPointDao.getDataPoints(DataPointExtendedNameComparator.instance, false);
        for (DataPointVO point : points) {
            if (Permissions.hasDataPointReadPermission(user, point))
                ph.addDataPoint(point.getPointFolderId(), new DataPointSummary(point));
        }

        ph.parseEmptyFolders();

        WatchList watchList = new WatchListDao().getSelectedWatchList(user.getId());
        prepareWatchList(watchList, user);
        setWatchList(user, watchList);

        data.put("pointFolder", ph.getRoot());
        data.put("shareUsers", getShareUsers(user));
        data.put("selectedWatchList", getWatchListData(user, watchList));

        return data;
    }
    
    /**
     * We need to override this method because Admin users can view all lists,
     * even lists that are shared with them so for the Shared section to work
     * we need to allow the Admin to be part of the shared users
     */
    @Override
	protected List<User> getShareUsers(User excludeUser) {
    	
    	if(excludeUser.isAdmin()){
    		return new UserDao().getUsers();
    	}else{
			List<User> users = new ArrayList<User>();
			for (User u : new UserDao().getUsers()) {
				if (u.getId() != excludeUser.getId())
					users.add(u);
			}
			return users;
    	}
	} 
    
    /**
     * Retrieves point state for all points on the current watch list.
     * 
     * @param pointIds
     * @return
     */
    public List<WatchListState> getPointData() {
        // Get the watch list from the user's session. It should have been set by the controller.
        return getPointDataImpl(getWatchList());
    }

    private List<WatchListState> getPointDataImpl(WatchList watchList) {
        if (watchList == null)
            return new ArrayList<WatchListState>();

        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        User user = Common.getUser(request);

        WatchListState state;
        List<WatchListState> states = new ArrayList<WatchListState>(watchList.getPointList().size());
        Map<String, Object> model = new HashMap<String, Object>();
        for (DataPointVO point : watchList.getPointList()) {
            // Create the watch list state.
            state = createWatchListState(request, point, Common.runtimeManager, model, user);
            states.add(state);
        }

        return states;
    }

    @DwrPermission(user = true)
    public void updateWatchListName(String name) {
        User user = Common.getUser();
        WatchList watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        watchList.setName(name);
        new WatchListDao().saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public IntStringPair addNewWatchList(int copyId) {
        User user = Common.getUser();

        WatchListDao watchListDao = new WatchListDao();
        WatchList watchList;

        if (copyId == Common.NEW_ID) {
            watchList = new WatchList();
            watchList.setName(translate("common.newName"));
        }
        else {
            watchList = new WatchListDao().getWatchList(getWatchList().getId());
            watchList.setId(Common.NEW_ID);
            watchList.setName(translate(new TranslatableMessage("common.copyPrefix", watchList.getName())));
            //Check to see if we are a Shared User (we can't share a watchlist with ourselves)
            List<ShareUser> watchListShared =  new ArrayList<ShareUser>();
            for(ShareUser shareUser : watchList.getWatchListUsers()){
            	//Don't add yourself
            	if(shareUser.getUserId() != user.getId())
            		watchListShared.add(shareUser); 
            }
            watchList.setWatchListUsers(watchListShared);
            
        }
        watchList.setUserId(user.getId());
        watchList.setXid(watchListDao.generateUniqueXid());

        watchListDao.saveWatchList(watchList);

        setWatchList(user, watchList);
        watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());

        return new IntStringPair(watchList.getId(), watchList.getName());
    }

    @DwrPermission(user = true)
    public boolean deleteWatchList(int watchListId) {
        User user = Common.getUser();

        WatchListDao watchListDao = new WatchListDao();
        WatchList watchList = getWatchList(user);
        if (watchList == null || watchListId != watchList.getId())
            watchList = watchListDao.getWatchList(watchListId);

        if (watchList == null || watchListDao.getWatchLists(user.getId()).size() == 1)
            // Only one watch list left. Leave it.
            return false;

        // Allow the delete.
        if (watchList.getUserAccess(user) == ShareUser.ACCESS_OWNER)
            watchListDao.deleteWatchList(watchListId);
        else
            watchListDao.removeUserFromWatchList(watchListId, user.getId());
        
        return true;
    }

    @DwrPermission(user = true)
    public Map<String, Object> setSelectedWatchList(int watchListId) {
        User user = Common.getUser();

        WatchListDao watchListDao = new WatchListDao();
        WatchList watchList = watchListDao.getWatchList(watchListId);
        WatchListCommon.ensureWatchListPermission(user, watchList);
        prepareWatchList(watchList, user);

        watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());

        Map<String, Object> data = getWatchListData(user, watchList);
        // Set the watchlist in the user object after getting the data since it may take a while, and the long poll
        // updates will all be missed in the meantime.
        setWatchList(user, watchList);

        return data;
    }

    @DwrPermission(user = true)
    public WatchListState addToWatchList(int pointId) {
        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        User user = Common.getUser();
        DataPointVO point = new DataPointDao().getDataPoint(pointId);
        if (point == null)
            return null;
        WatchList watchList = getWatchList(user);

        // Check permissions.
        Permissions.ensureDataPointReadPermission(user, point);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);

        // Add it to the watch list.
        watchList.getPointList().add(point);
        new WatchListDao().saveWatchList(watchList);
        updateSetPermission(point, watchList.getUserAccess(user), new UserDao().getUser(watchList.getUserId()));

        // Return the watch list state for it.
        return createWatchListState(request, point, Common.runtimeManager, new HashMap<String, Object>(), user);
    }

    @DwrPermission(user = true)
    public void removeFromWatchList(int pointId) {
        // Remove the point from the user's list.
        User user = Common.getUser();
        WatchList watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        for (DataPointVO point : watchList.getPointList()) {
            if (point.getId() == pointId) {
                watchList.getPointList().remove(point);
                break;
            }
        }
        new WatchListDao().saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public void moveUp(int pointId) {
        User user = Common.getUser();
        WatchList watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        List<DataPointVO> points = watchList.getPointList();

        DataPointVO point;
        for (int i = 0; i < points.size(); i++) {
            point = points.get(i);
            if (point.getId() == pointId) {
                points.set(i, points.get(i - 1));
                points.set(i - 1, point);
                break;
            }
        }

        new WatchListDao().saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public void moveDown(int pointId) {
        User user = Common.getUser();
        WatchList watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        List<DataPointVO> points = watchList.getPointList();

        DataPointVO point;
        for (int i = 0; i < points.size(); i++) {
            point = points.get(i);
            if (point.getId() == pointId) {
                points.set(i, points.get(i + 1));
                points.set(i + 1, point);
                break;
            }
        }

        new WatchListDao().saveWatchList(watchList);
    }

    /**
     * Convenience method for creating a populated view state.
     */
    private WatchListState createWatchListState(HttpServletRequest request, DataPointVO pointVO, RuntimeManager rtm,
            Map<String, Object> model, User user) {
        // Get the data point status from the data image.
        DataPointRT point = rtm.getDataPoint(pointVO.getId());

        WatchListState state = new WatchListState();
        state.setId(Integer.toString(pointVO.getId()));

        PointValueTime pointValue = prepareBasePointState(Integer.toString(pointVO.getId()), state, pointVO, point,
                model);
        setEvents(pointVO, user, model);
        if (pointValue != null && pointValue.getValue() instanceof ImageValue) {
            // Text renderers don't help here. Create a thumbnail.
            setImageText(request, state, pointVO, model, pointValue);
        }
        else
            setPrettyText(state, pointVO, model, pointValue);

        if (pointVO.isSettable())
            setChange(pointVO, state, point, request, model, user);

        if (state.getValue() != null)
            setChart(pointVO, state, request, model);
        setMessages(state, request, getModule().getWebPath() + "/web/snippet/watchListMessages.jsp", model);

        return state;
    }

    private static void setImageText(HttpServletRequest request, WatchListState state, DataPointVO pointVO,
            Map<String, Object> model, PointValueTime pointValue) {
        if (!ObjectUtils.equals(pointVO.lastValue(), pointValue)) {
            state.setValue(generateContent(request, "imageValueThumbnail.jsp", model));
            if (pointValue != null)
                state.setTime(Functions.getTime(pointValue));
            pointVO.updateLastValue(pointValue);
        }
    }

    /**
     * Method for creating image charts of the points on the watch list.
     */
    @DwrPermission(user = true)
    public String getImageChartData(int[] pointIds, int fromYear, int fromMonth, int fromDay, int fromHour,
            int fromMinute, int fromSecond, boolean fromNone, int toYear, int toMonth, int toDay, int toHour,
            int toMinute, int toSecond, boolean toNone, int width, int height) {
        DateTimeZone dtz = Common.getUser().getDateTimeZoneInstance();
        DateTime from = createDateTime(fromYear, fromMonth, fromDay, fromHour, fromMinute, fromSecond, fromNone, dtz);
        DateTime to = createDateTime(toYear, toMonth, toDay, toHour, toMinute, toSecond, toNone, dtz);

        StringBuilder htmlData = new StringBuilder();
        htmlData.append("<img src=\"chart/ft_");
        htmlData.append(System.currentTimeMillis());
        htmlData.append('_');
        htmlData.append(fromNone ? -1 : from.getMillis());
        htmlData.append('_');
        htmlData.append(toNone ? -1 : to.getMillis());

        boolean pointsFound = false;
        // Add the list of points that are numeric.
        List<DataPointVO> watchList = getWatchList().getPointList();
        for (DataPointVO dp : watchList) {
            int dtid = dp.getPointLocator().getDataTypeId();
            if ((dtid == DataTypes.NUMERIC || dtid == DataTypes.BINARY || dtid == DataTypes.MULTISTATE)
                    && ArrayUtils.contains(pointIds, dp.getId())) {
                pointsFound = true;
                htmlData.append('_');
                htmlData.append(dp.getId());
            }
        }

        if (!pointsFound)
            // There are no chartable points, so abort the image creation.
            return translate("watchlist.noChartables");

        htmlData.append(".png?w=");
        htmlData.append(width);
        htmlData.append("&h=");
        htmlData.append(height);
        htmlData.append("\" alt=\"" + translate("common.imageChart") + "\"/>");

        return htmlData.toString();
    }

    private Map<String, Object> getWatchListData(User user, WatchList watchList) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (watchList == null)
            return data;

        List<DataPointVO> points = watchList.getPointList();
        List<Integer> pointIds = new ArrayList<Integer>(points.size());
        for (DataPointVO point : points) {
            if (Permissions.hasDataPointReadPermission(user, point))
                pointIds.add(point.getId());
        }

        data.put("points", pointIds);
        List<ShareUser> watchListUsers = watchList.getWatchListUsers();
        data.put("users", watchListUsers);
        data.put("access", watchList.getUserAccess(user));

        return data;
    }

    private void prepareWatchList(WatchList watchList, User user) {
        int access = watchList.getUserAccess(user);
        User owner = new UserDao().getUser(watchList.getUserId());
        for (DataPointVO point : watchList.getPointList())
            updateSetPermission(point, access, owner);
    }

    private void updateSetPermission(DataPointVO point, int access, User owner) {
        // Point isn't settable
        if (!point.getPointLocator().isSettable())
            return;

        // Read-only access
        if (access != ShareUser.ACCESS_OWNER && access != ShareUser.ACCESS_SET)
            return;

        // Watch list owner doesn't have set permission
        if (!Permissions.hasDataPointSetPermission(owner, point))
            return;

        // All good.
        point.setSettable(true);
    }

    private static void setPrettyText(WatchListState state, DataPointVO pointVO, Map<String, Object> model,
            PointValueTime pointValue) {
        String prettyText = Functions.getHtmlText(pointVO, pointValue);
        model.put("text", prettyText);
        if (!ObjectUtils.equals(pointVO.lastValue(), pointValue)) {
            state.setValue(prettyText);
            if (pointValue != null)
                state.setTime(Functions.getTime(pointValue));
            pointVO.updateLastValue(pointValue);
        }
    }

    //
    // Share users
    //
    @DwrPermission(user = true)
    public List<ShareUser> addUpdateSharedUser(int userId, int accessType) {
        WatchList watchList = getWatchList();
        boolean found = false;
        for (ShareUser su : watchList.getWatchListUsers()) {
            if (su.getUserId() == userId) {
                found = true;
                su.setAccessType(accessType);
                break;
            }
        }

        if (!found) {
            ShareUser su = new ShareUser();
            su.setUserId(userId);
            su.setAccessType(accessType);
            watchList.getWatchListUsers().add(su);
        }

        new WatchListDao().saveWatchList(watchList);

        return watchList.getWatchListUsers();
    }

    @DwrPermission(user = true)
    public List<ShareUser> removeSharedUser(int userId) {
        WatchList watchList = getWatchList();

        for (ShareUser su : watchList.getWatchListUsers()) {
            if (su.getUserId() == userId) {
                watchList.getWatchListUsers().remove(su);
                break;
            }
        }

        new WatchListDao().saveWatchList(watchList);

        return watchList.getWatchListUsers();
    }

    private void setWatchList(User user, WatchList watchList) {
        user.setAttribute("watchList", watchList);
    }

    private static WatchList getWatchList() {
        return getWatchList(Common.getUser());
    }

    private static WatchList getWatchList(User user) {
        return user.getAttribute("watchList", WatchList.class);
    }

    @DwrPermission(anonymous = true)
    public void resetWatchListState(int pollSessionId) {
        LongPollData data = getLongPollData(pollSessionId, false);

        synchronized (data.getState()) {
            WatchListCommon.getWatchListStates(data).clear();
            WatchList wl = getWatchList();
            for (DataPointVO dp : wl.getPointList())
                dp.resetLastValue();
        }
        notifyLongPollImpl(data.getRequest());
    }

    @DwrPermission(user = true)
    public void getChartData(int[] pointIds, int fromYear, int fromMonth, int fromDay, int fromHour, int fromMinute,
            int fromSecond, boolean fromNone, int toYear, int toMonth, int toDay, int toHour, int toMinute,
            int toSecond, boolean toNone) {
        User user = Common.getUser();
        DateTimeZone dtz = user.getDateTimeZoneInstance();
        DateTime from = createDateTime(fromYear, fromMonth, fromDay, fromHour, fromMinute, fromSecond, fromNone, dtz);
        DateTime to = createDateTime(toYear, toMonth, toDay, toHour, toMinute, toSecond, toNone, dtz);
        DataExportDefinition def = new DataExportDefinition(pointIds, from, to);
        user.setDataExportDefinition(def);
    }
    
    @DwrPermission(user = true)
    public ProcessResult exportCurrentWatchlist(){
    	ProcessResult result = new ProcessResult();
    	WatchList wl = getWatchList();
    	
    	//TODO Could check for null, probably unlikely though
    	
    	Map<String, Object> data = new LinkedHashMap<String, Object>();
        //Get the Full VO for the export
        List<WatchList> vos = new ArrayList<WatchList>();
    	vos.add(wl);
        data.put(WatchListEmportDefinition.elementId, vos);
        
        result.addData("json", EmportDwr.export(data, 3));
        
    	return result;
    }
    
}
