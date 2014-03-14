/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.watchlist.WatchList;
import com.serotonin.m2m2.watchlist.WatchListCommon;
import com.serotonin.m2m2.watchlist.WatchListDao;
import com.serotonin.m2m2.watchlist.WatchListHandler;
import com.serotonin.m2m2.web.dwr.BaseDwr;
import com.serotonin.m2m2.web.taglib.Functions;

public class MobileWatchListHandler extends WatchListHandler {
    public static final String KEY_WATCHLIST_DATA = "watchListData";

    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        User user = Common.getUser(request);
        WatchListDao watchListDao = new WatchListDao();

        // Check for a watchlist id parameter. If given, update the user.
        try {
            int watchListId = Integer.parseInt(request.getParameter("watchListId"));

            WatchList watchList = watchListDao.getWatchList(watchListId);
            WatchListCommon.ensureWatchListPermission(user, watchList);
            watchListDao.saveSelectedWatchList(user.getId(), watchList.getId());
        }
        catch (NumberFormatException e) {
            // no op
        }

        prepareModel(request, model, user);

        // Get the selected watchlist.
        int watchListId = (Integer) model.get(KEY_SELECTED_WATCHLIST);

        // Get the point data.
        List<MobileWatchListState> states = new ArrayList<MobileWatchListState>();
        for (DataPointVO pointVO : watchListDao.getWatchList(watchListId).getPointList()) {
            MobileWatchListState state = createState(request, pointVO);
            states.add(state);
        }

        model.put(KEY_WATCHLIST_DATA, states);

        return null;
    }

    private MobileWatchListState createState(HttpServletRequest request, DataPointVO pointVO) {
        MobileWatchListState state = new MobileWatchListState();
        state.setId(Integer.toString(pointVO.getId()));
        state.setName(pointVO.getExtendedName());

        // Get the data point status from the data image.
        DataPointRT pointRT = Common.runtimeManager.getDataPoint(pointVO.getId());
        if (pointRT == null)
            state.setDisabled(true);
        else {
            PointValueTime pvt = pointRT.getPointValue();
            state.setTime(Functions.getTime(pvt));

            if (pvt != null && pvt.getValue() instanceof ImageValue) {
                // Text renderers don't help here. Create a thumbnail.
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("point", pointVO);
                model.put("pointValue", pvt);
                state.setValue(BaseDwr.generateContent(request, "imageValueThumbnail.jsp", model));
            }
            else
                state.setValue(Functions.getHtmlText(pointVO, pvt));
        }

        return state;
    }
}
