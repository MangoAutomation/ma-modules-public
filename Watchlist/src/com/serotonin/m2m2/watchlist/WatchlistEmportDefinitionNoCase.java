/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.

 */
package com.serotonin.m2m2.watchlist;

/**
 * This class is to fix a bug introduced where the watchlist JSON export
 * was named to watchList with a captial L.  This class will allow the import
 * of a definition using all lower case.
 * @author Terry Packer
 *
 */
public class WatchlistEmportDefinitionNoCase extends WatchListEmportDefinition {

    public static final String elementId = "watchlists";

    @Override
    public boolean getInView(){
        return false;
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
