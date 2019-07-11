/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
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
                        v = new StartsAndRuntimeList(info.getFromMillis(), info.getToMillis(), value);
                    break;
                    case DataTypes.ALPHANUMERIC:
                    case DataTypes.IMAGE:
                        v = new ValueChangeCounter(info.getFromMillis(), info.getToMillis(), value);
                        break;
                    case DataTypes.NUMERIC:
                        v = new AnalogStatistics(info.getFromMillis(), info.getToMillis(), value);
                        break;
                    default:
                        throw new ShouldNeverHappenException("Invalid Data Type: "+ voMap.get(value.getId()).getPointLocator().getDataTypeId());
                }
            }
            if(!lastBookend && !firstBookend) {
                v.addValueTime(value);
            }
            return v;
        });
        
        if(lastBookend) {
            generator.done();
            this.writer.writeStartObject(vo.getXid());
            DataPointStatisticsGenerator gen = new DataPointStatisticsGenerator(vo);
            gen.setGenerator(generator);
            
            //Pre-process the fields
            boolean rendered = false;
            Set<PointValueField> fields = new HashSet<>();
            for(PointValueField field: this.writer.getInfo().getFields()) {
                if(field == PointValueField.RENDERED) {
                    rendered = true;
                }else if(field == PointValueField.ANNOTATION) {
                    continue;
                }else {
                    fields.add(field);
                }
            }
            
            //Remove the Value field we will write it after
            fields.remove(PointValueField.VALUE);
            
            for(PointValueField field: fields) {
               field.writeValue(gen, info, Common.getTranslations(), false, writer);
            }
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
