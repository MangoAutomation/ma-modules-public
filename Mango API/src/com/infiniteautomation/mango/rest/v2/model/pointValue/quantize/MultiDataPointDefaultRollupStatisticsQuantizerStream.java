/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.SimplifyUtility;
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
        if(info.isSingleArray()) {
            //TODO Combine and sort the simplify and limit
        }else {
            Map<DataPointVO, List<AbstractRollupValueTime>> processed = new HashMap<>();
            for(DataPointVO vo : voMap.values()) {
                List<DataPointStatisticsGenerator> generators = valueMap.get(vo.getId());
                List<AbstractRollupValueTime> values = new ArrayList<>();
                if(generators.get(0).getGenerator() instanceof NoStatisticsGenerator) {
                    //Iterate and combine into an array
                    for(DataPointStatisticsGenerator gen : generators) {
                        NoStatisticsGenerator noGen = (NoStatisticsGenerator)gen.getGenerator();
                        for(IValueTime value : noGen.getValues()) {
                           values.add(new NoneRollupValueTime(vo, (IdPointValueTime)value));
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
                            values = SimplifyUtility.simplifyRollup(null, new Integer((int)vo.getSimplifyArgument()), true, values);
                        else
                            values = SimplifyUtility.simplifyRollup(vo.getSimplifyArgument(), null, true, values);
                    }
                    //TODO apply limit as sublist if necessary
                    processed.put(vo, values);
                }
            }
            for(Entry<DataPointVO, List<AbstractRollupValueTime>> entry : processed.entrySet()) {
                this.writer.writeStartArray(entry.getKey().getXid());
                for(AbstractRollupValueTime value : entry.getValue())
                    value.writePointValueTime(this.writer);
                this.writer.writeEndArray();
            }
        }
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
