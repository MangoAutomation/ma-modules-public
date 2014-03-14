/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.m2m2.module.RuntimeManagerDefinition;

public class RTMDefinition extends RuntimeManagerDefinition {
    public static RTMDefinition instance;

    private final List<ScheduledEventRT> scheduledEvents = new CopyOnWriteArrayList<ScheduledEventRT>();

    public RTMDefinition() {
        instance = this;
    }

    @Override
    public int getInitializationPriority() {
        return 10;
    }

    @Override
    public void initialize(boolean safe) {
        ScheduledEventDao scheduledEventDao = new ScheduledEventDao();
        for (ScheduledEventVO se : scheduledEventDao.getScheduledEvents()) {
            if (!se.isDisabled()) {
                if (safe) {
                    se.setDisabled(true);
                    scheduledEventDao.saveScheduledEvent(se);
                }
                else
                    startScheduledEvent(se);
            }
        }
    }

    @Override
    public void terminate() {
        while (!scheduledEvents.isEmpty())
            stopScheduledEvent(scheduledEvents.get(0).getVo().getId());
    }

    public void saveScheduledEvent(ScheduledEventVO vo) {
        // If the scheduled event is running, stop it.
        stopScheduledEvent(vo.getId());

        new ScheduledEventDao().saveScheduledEvent(vo);

        // If the scheduled event is enabled, start it.
        if (!vo.isDisabled())
            startScheduledEvent(vo);
    }

    public void deleteScheduledEvent(int id) {
        stopScheduledEvent(id);
        new ScheduledEventDao().deleteScheduledEvent(id);
    }

    private void startScheduledEvent(ScheduledEventVO vo) {
        synchronized (scheduledEvents) {
            ScheduledEventRT rt = vo.createRuntime();
            scheduledEvents.add(rt);
            rt.initialize();
        }
    }

    public void stopScheduledEvent(int id) {
        synchronized (scheduledEvents) {
            ScheduledEventRT sert = null;
            for (ScheduledEventRT s : scheduledEvents) {
                if (s.getVo().getId() == id) {
                    sert = s;
                    break;
                }
            }

            if (sert != null) {
                scheduledEvents.remove(sert);
                sert.terminate();
            }
        }
    }
}
