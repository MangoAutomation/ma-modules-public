/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import net.sf.mbus4j.dataframes.datablocks.DataBlock;

public class MBusDataBlockBean {
    private final int dbIndex;
    private final int devIndex;
    private final int rsIndex;
    private final DataBlock db;

    MBusDataBlockBean(int devIndex, int rsIndex, int dbIndex, DataBlock db) {
        this.devIndex = devIndex;
        this.rsIndex = rsIndex;
        this.dbIndex = dbIndex;
        this.db = db;
    }

    public String getName() {
        return db.getParamDescr();
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public int getRsIndex() {
        return rsIndex;
    }

    public int getDevIndex() {
        return devIndex;
    }

    public String getParams() {
        return db.toString();
    }

    public String getValue() {
        return db.getValueAsString();
    }
}
