/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.WideQueryCallback;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;

/**
 *
 * @author Terry Packer
 */
public abstract class PointValueTimeDatabaseStream<T> extends PointValueTimeQueryStream<T> implements WideQueryCallback<IdPointValueTime>{
    
    protected PointValueDao dao;

    public PointValueTimeDatabaseStream(ZonedDateTimeRangeQueryInfo info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap);
        this.dao = dao;
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(JsonGenerator jgen) throws IOException {
        this.streamType = StreamType.JSON;
        this.writer = new PointValueTimeJsonWriter(info, jgen);
        this.dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
     */
    @Override
    public void streamData(CSVPojoWriter<T> writer) throws IOException {
        this.streamType = StreamType.CSV;
        this.writer = new PointValueTimeCsvWriter(info, writer.getWriter());
        this.dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
    }
}
