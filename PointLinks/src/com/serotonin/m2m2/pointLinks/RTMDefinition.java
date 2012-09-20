/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.Assert;

import com.serotonin.m2m2.module.RuntimeManagerDefinition;

public class RTMDefinition extends RuntimeManagerDefinition {
    public static RTMDefinition instance;

    private final List<PointLinkRT> pointLinks = new CopyOnWriteArrayList<PointLinkRT>();

    public RTMDefinition() {
        instance = this;
    }

    @Override
    public int getInitializationPriority() {
        return 10;
    }

    @Override
    public void initialize(boolean safe) {
        // Set up point links.
        PointLinkDao pointLinkDao = new PointLinkDao();
        for (PointLinkVO vo : pointLinkDao.getPointLinks()) {
            if (!vo.isDisabled()) {
                if (safe) {
                    vo.setDisabled(true);
                    pointLinkDao.savePointLink(vo);
                }
                else
                    startPointLink(vo);
            }
        }
    }

    @Override
    public void terminate() {
        while (!pointLinks.isEmpty())
            stopPointLink(pointLinks.get(0).getId());
    }

    //
    //
    // Point links
    //
    private PointLinkRT getRunningPointLink(int pointLinkId) {
        for (PointLinkRT pointLink : pointLinks) {
            if (pointLink.getId() == pointLinkId)
                return pointLink;
        }
        return null;
    }

    public boolean isPointLinkRunning(int pointLinkId) {
        return getRunningPointLink(pointLinkId) != null;
    }

    public void deletePointLink(int pointLinkId) {
        stopPointLink(pointLinkId);
        new PointLinkDao().deletePointLink(pointLinkId);
    }

    public void savePointLink(PointLinkVO vo) {
        // If the point link is running, stop it.
        stopPointLink(vo.getId());

        new PointLinkDao().savePointLink(vo);

        // If the point link is enabled, start it.
        if (!vo.isDisabled())
            startPointLink(vo);
    }

    private void startPointLink(PointLinkVO vo) {
        synchronized (pointLinks) {
            // If the point link is already running, just quit.
            if (isPointLinkRunning(vo.getId()))
                return;

            // Ensure that the point link is enabled.
            Assert.isTrue(!vo.isDisabled());

            // Create and start the runtime version of the point link.
            PointLinkRT pointLink = new PointLinkRT(vo);
            pointLink.initialize();

            // Add it to the list of running point links.
            pointLinks.add(pointLink);
        }
    }

    private void stopPointLink(int id) {
        synchronized (pointLinks) {
            PointLinkRT pointLink = getRunningPointLink(id);
            if (pointLink == null)
                return;

            pointLinks.remove(pointLink);
            pointLink.terminate();
        }
    }
}
