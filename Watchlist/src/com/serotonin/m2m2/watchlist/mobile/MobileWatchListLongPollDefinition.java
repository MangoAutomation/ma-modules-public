package com.serotonin.m2m2.watchlist.mobile;

import com.serotonin.m2m2.module.LongPollDefinition;
import com.serotonin.m2m2.watchlist.WatchListDwr;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;

public class MobileWatchListLongPollDefinition extends LongPollDefinition{
    private MobileWatchListLongPollHandler handler;

    @Override
    public void preInitialize(boolean install, boolean upgrade) {
        WatchListDwr dwr = new WatchListDwr();
        dwr.setModule(getModule());
        handler = new MobileWatchListLongPollHandler(dwr,"mobileWatchlist");
    }

    @Override
    public LongPollHandler getHandler() {
        return handler;
    }
}
