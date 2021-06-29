/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.io.IOException;

import com.goebl.simplify.Point;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointVOPointValueTimeBookend implements DataPointValueTime {

    final DataPointVO vo;
    final IdPointValueTime pvt;
    final boolean firstBookend;
    final boolean lastBookend;
    final boolean cached;

    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt) {
        this(vo, pvt, false, false, false);
    }

    public DataPointVOPointValueTimeBookend(DataPointVO vo, IdPointValueTime pvt, boolean firstBookend, boolean lastBookend, boolean cached) {
        this.vo = vo;
        this.pvt = pvt;
        this.firstBookend = firstBookend;
        this.lastBookend = lastBookend;
        this.cached = cached;
    }

    /**
     * @return the vo
     */
    @Override
    public DataPointVO getVo() {
        return vo;
    }
    /**
     * @return the pvt
     */
    public IdPointValueTime getPvt() {
        return pvt;
    }

    @Override
    public long getTime() {
        return pvt.getTime();
    }

    //    public int getId() {
    //        return vo.getId();
    //    }

    public int getSeriesId() {
        return vo.getSeriesId();
    }

    public boolean isFirstBookend() {
        return firstBookend;
    }
    public boolean isLastBookend() {
        return lastBookend;
    }

    /**
     * @return the bookend
     */
    public boolean isBookend() {
        return firstBookend || lastBookend;
    }

    public boolean isCached() {
        return cached;
    }

    @Override
    public double getX() {
        return pvt.getTime();
    }

    @Override
    public double getY() {
        return pvt.getDoubleValue();
    }

    @Override
    public boolean isProcessable() {
        //TODO Could check to see if we are image etc.
        return pvt.getValue() != null;
    }

    @Override
    public void writeEntry(PointValueTimeWriter writer, boolean useXid, boolean allowTimestamp)
            throws IOException {
        for(PointValueField field : writer.getInfo().getFields()) {
            if(!allowTimestamp && field == PointValueField.TIMESTAMP)
                continue;
            field.writeValue(this, writer.getInfo(), writer.getTranslations(), useXid, writer);
        }
    }

    @Override
    public int compareTo(Point that) {
        if (getX() < that.getX())
            return -1;
        if (getX() > that.getX())
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        return vo.getXid() + " - " + pvt.toString();
    }
}
