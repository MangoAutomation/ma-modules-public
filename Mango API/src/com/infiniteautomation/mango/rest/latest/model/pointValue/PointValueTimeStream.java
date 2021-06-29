/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.LatestQueryInfo;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public interface PointValueTimeStream<T, INFO extends LatestQueryInfo> {

    public enum StreamContentType {
        JSON,
        CSV
    }

    public INFO getQueryInfo();
    public Map<Integer, DataPointVO> getVoMap();

    public void start(PointValueTimeWriter writer) throws QueryCancelledException, IOException;
    public void streamData(PointValueTimeWriter writer) throws QueryCancelledException, IOException;
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException;

    /**
     * Set the content type of the stream CSV or JSON
     * @param type
     */
    public void setContentType(StreamContentType type);

}
