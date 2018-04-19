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
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
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
	
	private static final int pointEventsLimit = 10; //10 most recent events
	
    @DwrPermission(user = true)
    public Map<String, Object> init() {
        Map<String, Object> data = new HashMap<>();

        PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true).copyFoldersOnly();
        User user = Common.getHttpUser();
        List<DataPointVO> points = DataPointDao.instance.getDataPoints(DataPointExtendedNameComparator.instance, false);
        final boolean admin = Permissions.hasAdmin(user);
        for (DataPointVO point : points) {
            if (admin || Permissions.hasDataPointReadPermission(user, point))
                ph.addDataPoint(point.getPointFolderId(), new DataPointSummary(point));
        }

        ph.parseEmptyFolders();

        WatchListVO watchList = WatchListDao.instance.getSelectedWatchList(user.getId());
        setWatchList(user, watchList);

        data.put("pointFolder", ph.getRoot());
        data.put("selectedWatchList", getWatchListData(user, watchList));

        return data;
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

    private List<WatchListState> getPointDataImpl(WatchListVO watchList) {
        if (watchList == null)
            return new ArrayList<>();

        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        User user = Common.getUser(request);

        WatchListState state;
        List<WatchListState> states = new ArrayList<>(watchList.getPointList().size());
        Map<String, Object> model = new HashMap<>();
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
        WatchListVO watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        watchList.setName(name);
        WatchListDao.instance.saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public IntStringPair addNewWatchList(int copyId) {
        User user = Common.getUser();

        WatchListVO watchList;

        if (copyId == Common.NEW_ID) {
            watchList = new WatchListVO();
            watchList.setName(translate("common.newName"));
        }
        else {
            watchList = WatchListDao.instance.get(getWatchList().getId());
            watchList.setId(Common.NEW_ID);
            watchList.setName(translate(new TranslatableMessage("common.copyPrefix", watchList.getName())));
        }
        watchList.setUserId(user.getId());
        watchList.setXid(WatchListDao.instance.generateUniqueXid());

        WatchListDao.instance.saveWatchList(watchList);

        setWatchList(user, watchList);
        WatchListDao.instance.saveSelectedWatchList(user.getId(), watchList.getId());

        return new IntStringPair(watchList.getId(), watchList.getName());
    }

    @DwrPermission(user = true)
    public boolean deleteWatchList(int watchListId) {
        User user = Common.getUser();

        WatchListVO watchList = getWatchList(user);
        if (watchList == null || watchListId != watchList.getId())
            watchList = WatchListDao.instance.get(watchListId);

        if (watchList == null || WatchListDao.instance.getWatchLists(user).size() == 1)
            // Only one watch list left. Leave it.
        	return false;

        // Allow the delete if the user is an editor.
        if (watchList.isEditor(user)){
        	WatchListDao.instance.deleteWatchList(watchListId);
        	return true;
        }
        return false;
    }

    @DwrPermission(user = true)
    public Map<String, Object> setSelectedWatchList(int watchListId) {
        User user = Common.getUser();

        WatchListVO watchList = WatchListDao.instance.get(watchListId);
        WatchListCommon.ensureWatchListPermission(user, watchList);
        WatchListDao.instance.saveSelectedWatchList(user.getId(), watchList.getId());

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
        DataPointVO point = DataPointDao.instance.getDataPoint(pointId);
        if (point == null)
            return null;
        WatchListVO watchList = getWatchList(user);

        // Check permissions.
        Permissions.ensureDataPointReadPermission(user, point);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);

        // Add it to the watch list.
        watchList.getPointList().add(point);
        WatchListDao.instance.saveWatchList(watchList);

        // Return the watch list state for it.
        return createWatchListState(request, point, Common.runtimeManager, new HashMap<String, Object>(), user);
    }

    @DwrPermission(user = true)
    public void removeFromWatchList(int pointId) {
        // Remove the point from the user's list.
        User user = Common.getUser();
        WatchListVO watchList = getWatchList(user);
        WatchListCommon.ensureWatchListEditPermission(user, watchList);
        for (DataPointVO point : watchList.getPointList()) {
            if (point.getId() == pointId) {
                watchList.getPointList().remove(point);
                break;
            }
        }
        WatchListDao.instance.saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public void moveUp(int pointId) {
        User user = Common.getUser();
        WatchListVO watchList = getWatchList(user);
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

        WatchListDao.instance.saveWatchList(watchList);
    }

    @DwrPermission(user = true)
    public void moveDown(int pointId) {
        User user = Common.getUser();
        WatchListVO watchList = getWatchList(user);
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

        WatchListDao.instance.saveWatchList(watchList);
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
        setEvents(pointVO, user, model, pointEventsLimit);
        if (pointValue != null && pointValue.getValue() instanceof ImageValue) {
            // Text renderers don't help here. Create a thumbnail.
            setImageText(request, state, pointVO, model, pointValue);
        }
        else
            setPrettyText(state, pointVO, model, pointValue);

        if (isSettable(pointVO, user))
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
        
        //Define a list of colours to use for the chart, each must be unique
        //currently using the JFree Chart method of determining these
        //ChartColourGenerator colors = new VisuallyDistinctChartColourGenerator(watchList.size(), .1f, .9f);
        ChartColourGenerator colors = new DefaultJFreeChartColourGenerator();

        //Allow charts with 1 point to use the default color
        boolean usePointChartColour = false;
        if(watchList.size() == 1)
        	usePointChartColour = true;
        
        for (DataPointVO dp : watchList) {
            int dtid = dp.getPointLocator().getDataTypeId();
            if ((dtid == DataTypes.NUMERIC || dtid == DataTypes.BINARY || dtid == DataTypes.MULTISTATE)
                    && ArrayUtils.contains(pointIds, dp.getId())) {
                pointsFound = true;
                htmlData.append('_');
                htmlData.append(dp.getId());
                //Assign Colour or let chart servlet use point settings
                if(!usePointChartColour){
                	htmlData.append("|");
                	htmlData.append(colors.getNextHexColour());
                }
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

	private Map<String, Object> getWatchListData(User user, WatchListVO watchList) {
        Map<String, Object> data = new HashMap<>();
        if (watchList == null)
            return data;

        List<DataPointVO> points = watchList.getPointList();
        List<Integer> pointIds = new ArrayList<>(points.size());
        for (DataPointVO point : points) {
            if (Permissions.hasDataPointReadPermission(user, point))
                pointIds.add(point.getId());
        }

        data.put("points", pointIds);
        data.put("access", watchList.getUserAccess(user));
        data.put("readPermission", watchList.getReadPermission());
        data.put("editPermission", watchList.getEditPermission());

        return data;
    }

    private boolean isSettable(DataPointVO point, User user) {
        if (!point.getPointLocator().isSettable())
            return false;
        // User doesn't have set permission
        if (!Permissions.hasDataPointSetPermission(user, point))
            return false;
        return true;
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

    private void setWatchList(User user, WatchListVO watchList) {
        user.setAttribute("watchList", watchList);
    }

    private static WatchListVO getWatchList() {
        return getWatchList(Common.getUser());
    }

    private static WatchListVO getWatchList(User user) {
        return user.getAttribute("watchList", WatchListVO.class);
    }

    @DwrPermission(anonymous = true)
    public void resetWatchListState(int pollSessionId) {
        LongPollData data = getLongPollData(pollSessionId, false);

        synchronized (data.getState()) {
            WatchListCommon.getWatchListStates(data).clear();
            WatchListVO wl = getWatchList();
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
    public ProcessResult exportCurrentWatchlist() {
        ProcessResult result = new ProcessResult();
        WatchListVO wl = getWatchList();

        Map<String, Object> data = new LinkedHashMap<>();
        //Get the Full VO for the export
        List<WatchListVO> vos = new ArrayList<>();
        vos.add(wl);
        data.put(WatchListEmportDefinition.elementId, vos);

        result.addData("json", EmportDwr.export(data, 3));

        return result;
    }

    @DwrPermission(user = true)
    public ProcessResult savePermissions(String readPermission, String editPermission) {
        WatchListVO wl = getWatchList();
        wl.setReadPermission(readPermission);
        wl.setEditPermission(editPermission);
        
        ProcessResult response = new ProcessResult();
        wl.validate(response);
        if(!response.getHasMessages())
        	WatchListDao.instance.saveWatchList(wl);
        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult getPermissions() {
        WatchListVO wl = getWatchList();
        ProcessResult result = new ProcessResult();
        result.addData("readPermission", wl.getReadPermission());
        result.addData("editPermission", wl.getEditPermission());
        return result;
    }
}
