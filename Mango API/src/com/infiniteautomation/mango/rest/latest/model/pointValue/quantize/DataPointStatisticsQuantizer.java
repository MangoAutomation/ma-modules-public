/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import java.io.IOException;

import com.infiniteautomation.mango.db.query.BookendQueryCallback;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.quantize.AbstractPointValueTimeQuantizer;
import com.infiniteautomation.mango.quantize.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public abstract class DataPointStatisticsQuantizer<T extends StatisticsGenerator> implements StatisticsGeneratorQuantizerCallback<T>, BookendQueryCallback<IdPointValueTime>{

    protected final ChildStatisticsGeneratorCallback callback;
    protected AbstractPointValueTimeQuantizer<T> quantizer;
    protected final DataPointVO vo;
    protected boolean open;
    protected boolean done;

    public DataPointStatisticsQuantizer(DataPointVO vo, ChildStatisticsGeneratorCallback callback) {
        this.vo = vo;
        this.callback = callback;
        this.open = false;
        this.done = false;
    }

    public void fastForward(long time) throws QueryCancelledException {
        if(!open) {
            this.quantizer.firstValue(null, 0, true);
            this.open = true;
        }
        this.quantizer.fastForward(time);
    }

    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws QueryCancelledException {
        quantizer.firstValue(value, index, bookend);
        open = true;
    }

    @Override
    public void row(IdPointValueTime value, int index) throws QueryCancelledException {
        quantizer.row(value, index);
    }

    @Override
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws QueryCancelledException {
        quantizer.lastValue(value, index, bookend);
    }

    @Override
    public void quantizedStatistics(StatisticsGenerator statisticsGenerator) throws QueryCancelledException {
        this.callback.quantizedStatistics(new DataPointStatisticsGenerator(vo, statisticsGenerator));
    }

    public boolean isOpen() {
        return open;
    }
    public boolean isDone() {
        return done;
    }

    /**
     * @throws IOException
     *
     */
    public void done() throws QueryCancelledException {
        quantizer.done();
        done = true;
    }
}
