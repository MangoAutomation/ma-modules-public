/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * TODO Fix up Map Generics in class definition
 *
 * @author Terry Packer
 */
public class MultiPointStatisticsStream extends MultiPointTimeRangeDatabaseStream<Map<String, Object>, ZonedDateTimeStatisticsQueryInfo> {

    private Map<Integer, StatisticsGenerator> statsMap;
    private boolean rendered; //TODO Implement this
    
    /**
     * @param info
     * @param voMap
     * @param dao
     */
    public MultiPointStatisticsStream(ZonedDateTimeStatisticsQueryInfo info, Map<Integer, DataPointVO> voMap,
            PointValueDao dao) {
        super(info, voMap, dao);
        this.statsMap = new HashMap<>(voMap.size());
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream#processRow(com.serotonin.m2m2.rt.dataImage.IdPointValueTime, int, boolean, boolean, boolean)
     */
    @Override
    protected void processRow(IdPointValueTime value, int index, boolean firstBookend,
            boolean lastBookend, boolean cached) throws IOException {
        
        final DataPointVO vo = voMap.get(value.getId());
        if(info.isUseCache() != PointValueTimeCacheControl.NONE && !cached)
            if(!processValueThroughCache(value, index, firstBookend, lastBookend))
                return;
        StatisticsGenerator generator = statsMap.compute(value.getId(), (k, v) -> {
            if(v == null) {
                switch(vo.getPointLocator().getDataTypeId()){
                    case DataTypes.BINARY:
                    case DataTypes.MULTISTATE:
                        v = new StartsAndRuntimeList(info.from.toInstant().toEpochMilli(), info.to.toInstant().toEpochMilli(), value.getValue() == null ? null : value.getValue());
                    break;
                    case DataTypes.ALPHANUMERIC:
                    case DataTypes.IMAGE:
                        v = new ValueChangeCounter(info.from.toInstant().toEpochMilli(), info.to.toInstant().toEpochMilli(), value.getValue() == null ? null : value.getValue());
                        break;
                    case DataTypes.NUMERIC:
                        v = new AnalogStatistics(info.from.toInstant().toEpochMilli(), info.to.toInstant().toEpochMilli(), value.getValue() == null ? null : value.getDoubleValue());
                        break;
                    default:
                        throw new ShouldNeverHappenException("Invalid Data Type: "+ voMap.get(value.getId()).getPointLocator().getDataTypeId());
                }
            }else {
                if(!lastBookend) {
                    v.addValueTime(value);
                }
            }
            return v;
        });
        
        if(lastBookend) {
            generator.done();
            this.writer.writeStartObject(vo.getXid());
            this.writer.writeAllStatistics(generator, vo, rendered); 
            this.writer.writeEndObject();
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream#start(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void start(PointValueTimeWriter writer) throws IOException {
        this.writer = writer;
        this.writer.writeStartObject();
    }
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream#finish(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        writer.writeEndObject();
    }
}
