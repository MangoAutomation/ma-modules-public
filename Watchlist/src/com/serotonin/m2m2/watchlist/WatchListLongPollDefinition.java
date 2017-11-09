/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.module.LongPollDefinition;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;

public class WatchListLongPollDefinition extends LongPollDefinition {
    private WatchListLongPollHandler handler;

    @Override
    public void preInitialize(boolean install, boolean upgrade) {
        WatchListDwr dwr = new WatchListDwr();
        dwr.setModule(getModule());
        handler = new WatchListLongPollHandler(dwr,"watchlist");
    }

    @Override
    public LongPollHandler getHandler() {
        return handler;
    }
}
