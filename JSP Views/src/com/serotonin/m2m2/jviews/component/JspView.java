/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.component;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Matthew Lohbihler
 */
public class JspView {
    private final User authorityUser;
    private final List<JspViewComponent> components = new ArrayList<JspViewComponent>();
    private final List<DataPointVO> pointCache = new ArrayList<DataPointVO>();

    public JspView(User authorityUser) {
        this.authorityUser = authorityUser;
    }

    public User getAuthorityUser() {
        return authorityUser;
    }

    public int addPoint(DataPointVO dataPointVO, boolean raw, String disabledValue, boolean time) {
        JspViewPoint point = new JspViewPoint(components.size(), dataPointVO, raw, disabledValue, time);
        components.add(point);
        return point.getId();
    }

    public int addChart(long duration, int width, int height, List<JspViewChartPoint> points) {
        JspViewChart chart = new JspViewChart(duration, components.size(), width, height, points);
        components.add(chart);
        return chart.getId();
    }

    public List<JspViewComponent> getComponents() {
        return components;
    }

    synchronized public DataPointVO getPoint(String xid) {
        for (DataPointVO dp : pointCache) {
            if (dp.getXid().equals(xid))
                return dp;
        }

        DataPointVO dp = DataPointDao.instance.getDataPoint(xid);
        if (dp != null) {
            // Check permissions.
            Permissions.ensureDataPointSetPermission(authorityUser, dp);

            pointCache.add(dp);
        }
        return dp;
    }
}
