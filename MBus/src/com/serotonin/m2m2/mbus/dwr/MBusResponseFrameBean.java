/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import java.util.ArrayList;
import java.util.List;

import net.sf.mbus4j.dataframes.Frame;
import net.sf.mbus4j.dataframes.UserDataResponse;

/**
 * 
 * @author aploese
 */
public class MBusResponseFrameBean {
    private final String name;

    public MBusResponseFrameBean(Frame rsf, int devIndex, int frameIndex, String name) {
        this.rsf = rsf;
        this.name = name;
        if (rsf instanceof UserDataResponse) {
            UserDataResponse rf = (UserDataResponse) rsf;
            for (int i = 0; i < rf.getDataBlockCount(); i++) {
                dataBlocks.add(new MBusDataBlockBean(devIndex, frameIndex, i, rf.getDataBlock(i)));
            }
        }

    }

    private final Frame rsf;
    private final List<MBusDataBlockBean> dataBlocks = new ArrayList<MBusDataBlockBean>();

    public boolean addDataBlock(MBusDataBlockBean bean) {
        return dataBlocks.add(bean);
    }

    public MBusDataBlockBean[] getDataBlocks() {
        return dataBlocks.toArray(new MBusDataBlockBean[dataBlocks.size()]);
    }

    public String getName() {
        return name;
    }

    public Frame getRsf() {
        return rsf;
    }
}
