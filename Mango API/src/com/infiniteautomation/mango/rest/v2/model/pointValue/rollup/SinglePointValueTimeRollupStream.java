/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.rollup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeCsvWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.view.quantize3.AbstractPointValueTimeQuantizer;
import com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * TODO Use Limit in Quantizer!
 *
 * @author Terry Packer
 */
public abstract class SinglePointValueTimeRollupStream<T extends StatisticsGenerator> extends PointValueTimeRollupStream<PointValueTimeModel> implements StatisticsGeneratorQuantizerCallback<T>{

    protected AbstractPointValueTimeQuantizer<T> quantizer;
    protected final LimitCounter limiter;
    
    public SinglePointValueTimeRollupStream(ZonedDateTimeRangeQueryInfo info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.limiter = new LimitCounter(info.getLimit());
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(JsonGenerator jgen) throws IOException {
        this.streamType = StreamType.JSON;
        this.writer = new PointValueTimeJsonWriter(info, jgen);
        this.quantizer = getQuantizer();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), info.getLimit(), quantizer);
    }
    
    protected abstract AbstractPointValueTimeQuantizer<T> getQuantizer();

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
     */
    @Override
    public void streamData(CSVPojoWriter<PointValueTimeModel> writer) throws IOException {
        this.streamType = StreamType.CSV;
        this.writer = new PointValueTimeCsvWriter(info, writer.getWriter());
        this.quantizer = getQuantizer();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), info.getLimit(), quantizer);
    }

}
