package com.serotonin.m2m2.watchlist.mobile;

import com.serotonin.m2m2.watchlist.WatchListDwr;
import com.serotonin.m2m2.watchlist.WatchListLongPollHandler;

public class MobileWatchListLongPollHandler extends WatchListLongPollHandler{

	public MobileWatchListLongPollHandler(WatchListDwr watchListDwr,String handlerKey) {
		super(watchListDwr,handlerKey);
	}

}
