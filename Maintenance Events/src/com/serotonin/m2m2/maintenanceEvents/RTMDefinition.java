/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.Assert;

import com.serotonin.m2m2.module.RuntimeManagerDefinition;

public class RTMDefinition extends RuntimeManagerDefinition {
    public static RTMDefinition instance;

    private final List<MaintenanceEventRT> maintenanceEvents = new CopyOnWriteArrayList<MaintenanceEventRT>();

    public RTMDefinition() {
        instance = this;
    }

    @Override
    public int getInitializationPriority() {
        return 11;
    }

    @Override
    public void initialize(boolean safe) {
        MaintenanceEventDao maintenanceEventDao = new MaintenanceEventDao();
        for (MaintenanceEventVO vo : maintenanceEventDao.getMaintenanceEvents()) {
            if (!vo.isDisabled()) {
                if (safe) {
                    vo.setDisabled(true);
                    maintenanceEventDao.saveMaintenanceEvent(vo);
                }
                else
                    startMaintenanceEvent(vo);
            }
        }
    }

    @Override
    public void terminate() {
        while (!maintenanceEvents.isEmpty())
            stopMaintenanceEvent(maintenanceEvents.get(0).getVo().getId());
    }

    //
    //
    // Maintenance events
    //
    public MaintenanceEventRT getRunningMaintenanceEvent(int id) {
        for (MaintenanceEventRT rt : maintenanceEvents) {
            if (rt.getVo().getId() == id)
                return rt;
        }
        return null;
    }

    public boolean isActiveMaintenanceEvent(int dataSourceId) {
        for (MaintenanceEventRT rt : maintenanceEvents) {
            if (rt.getVo().getDataSourceId() == dataSourceId && rt.isEventActive())
                return true;
        }
        return false;
    }

    public boolean isMaintenanceEventRunning(int id) {
        return getRunningMaintenanceEvent(id) != null;
    }

    public void deleteMaintenanceEvent(int id) {
        stopMaintenanceEvent(id);
        new MaintenanceEventDao().deleteMaintenanceEvent(id);
    }

    public void saveMaintenanceEvent(MaintenanceEventVO vo) {
        // If the maintenance event is running, stop it.
        stopMaintenanceEvent(vo.getId());

        new MaintenanceEventDao().saveMaintenanceEvent(vo);

        // If the maintenance event is enabled, start it.
        if (!vo.isDisabled())
            startMaintenanceEvent(vo);
    }

    private void startMaintenanceEvent(MaintenanceEventVO vo) {
        synchronized (maintenanceEvents) {
            // If the maintenance event is already running, just quit.
            if (isMaintenanceEventRunning(vo.getId()))
                return;

            // Ensure that the maintenance event is enabled.
            Assert.isTrue(!vo.isDisabled());

            // Create and start the runtime version of the maintenance event.
            MaintenanceEventRT rt = new MaintenanceEventRT(vo);
            rt.initialize();

            // Add it to the list of running maintenance events.
            maintenanceEvents.add(rt);
        }
    }

    private void stopMaintenanceEvent(int id) {
        synchronized (maintenanceEvents) {
            MaintenanceEventRT rt = getRunningMaintenanceEvent(id);
            if (rt == null)
                return;

            maintenanceEvents.remove(rt);
            rt.terminate();
        }
    }
}
