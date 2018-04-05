/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.goebl.simplify.SimplifiableValue;
import com.goebl.simplify.SimplifyUtility;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.stats.IValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class MultiDataPointDefaultRollupStatisticsQuantizerStream <T, INFO extends ZonedDateTimeRangeQueryInfo> extends AbstractMultiDataPointStatisticsQuantizerStream<T, INFO>{

    private Map<Integer, List<DataPointStatisticsGenerator>> valueMap;
    
    public MultiDataPointDefaultRollupStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.valueMap = new HashMap<>(voMap.size());
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), null, this);
    
        //Fast forward to end to fill any gaps at the end
        for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values()) {
            if(!quant.isDone())
                quant.done();
        }
        //Re-assemble as per output format
        Map<DataPointVO, List<SimplifiableValue>> processed = process();
        if(info.isSingleArray() && voMap.size() > 1) {
            //Combine into single array
            List<SimplifiableValue> values = new ArrayList<>();
            for(Entry<DataPointVO, List<SimplifiableValue>> entry : processed.entrySet()) {
                values.addAll(entry.getValue());
            }
            //Sort by time
            Collections.sort(values);
            //Limit entire list
            if(info.getLimit() != null)
                values = values.subList(0, info.getLimit());
            
            //Reset current time and write out
            if(values.size() > 0) {
                long currentTime = values.get(0).getTime();
                List<SimplifiableValue> currentValues = new ArrayList<>();
                for(SimplifiableValue value : values) {
                    if(currentTime == value.getTime())
                        currentValues.add(value);
                    else {
                        if(currentValues.size() > 0) {
                            writer.writeMultipleSimplifiablValuesAtSameTime(currentValues, currentValues.get(0).getTime());
                            currentValues.clear();
                        }
                        currentTime = value.getTime();
                        currentValues.add(value);
                    }
                }
            }
        }else {
            for(Entry<DataPointVO, List<SimplifiableValue>> entry : processed.entrySet()) {
                int count = 0;
                this.writer.writeStartArray(entry.getKey().getXid());
                for(SimplifiableValue value : entry.getValue()) {
                    if(info.getLimit() != null && count >= info.getLimit())
                        break;
                    writer.writeSimplifiableValue(value);
                    count++;
                }
                this.writer.writeEndArray();
            }
        }
    }
    
    /**
     * @return
     */
    private Map<DataPointVO, List<SimplifiableValue>> process() {
        Map<DataPointVO, List<SimplifiableValue>> processed = new HashMap<>();
        for(DataPointVO vo : voMap.values()) {
            List<DataPointStatisticsGenerator> generators = valueMap.get(vo.getId());
            List<SimplifiableValue> values = new ArrayList<>();
            if(generators.get(0).getGenerator() instanceof NoStatisticsGenerator) {
                //Iterate and combine into an array
                for(DataPointStatisticsGenerator gen : generators) {
                    NoStatisticsGenerator noGen = (NoStatisticsGenerator)gen.getGenerator();
                    for(IValueTime value : noGen.getValues()) {
                       values.add(new DataPointVOPointValueTimeBookend(vo, (IdPointValueTime)value));
                    }
                }
            }else {
                for(DataPointStatisticsGenerator generator : generators) {
                    values.add(new RollupValueTime(generator, RollupEnum.convertTo(vo.getRollup())));
                }
            }
            if(values.size() > 0) {
                if(vo.isSimplifyDataSets()) {
                    if(vo.getSimplifyType() == DataPointVO.SimplifyTypes.TARGET)
                        values = SimplifyUtility.simplify(null, new Integer((int)vo.getSimplifyArgument()), true, true, values);
                    else
                        values = SimplifyUtility.simplify(vo.getSimplifyArgument(), null, true, true, values);
                }
                processed.put(vo, values);
            }
        }
        return processed;
    }
    
    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#firstValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int, boolean)
     */
    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());        
        updateQuantizers(value);
        quantizer.firstValue(value, index, bookend);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.PVTQueryCallback#row(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        updateQuantizers(value);
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.row(value, index);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.lastValue(value, index, bookend);
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.ChildStatisticsGeneratorCallback#quantizedStatistics(com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator)
     */
    @Override
    public void quantizedStatistics(DataPointStatisticsGenerator generator) throws IOException {
        //Separate them into a lists per data point
        List<DataPointStatisticsGenerator> stats = this.valueMap.get(generator.getVo().getId());
        if(stats == null) {
            stats = new ArrayList<>();
            this.valueMap.put(generator.getVo().getId(), stats);
        }
        stats.add(generator);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeQueryStream#finish(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        super.finish(writer);
        //TODO Any finishing logic?
    }
    
}
