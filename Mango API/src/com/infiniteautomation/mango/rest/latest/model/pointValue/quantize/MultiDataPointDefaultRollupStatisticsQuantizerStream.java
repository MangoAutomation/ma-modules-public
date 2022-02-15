/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.goebl.simplify.SimplifyUtility;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointValueTime;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.IValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class MultiDataPointDefaultRollupStatisticsQuantizerStream <T, INFO extends ZonedDateTimeRangeQueryInfo> extends MultiDataPointStatisticsQuantizerStream<T, INFO>{

    private final Map<Integer, List<DataPointStatisticsGenerator>> valueMap;
    //If we are required to use simplify then we must cache all the data
    private final boolean useSimplify;

    public MultiDataPointDefaultRollupStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.valueMap = new HashMap<>(voMap.size());
        boolean useSimp = false;
        for(DataPointVO vo : voMap.values()) {
            if(vo.isSimplifyDataSets()) {
                useSimp = true;
                break;
            }
        }
        this.useSimplify = useSimp;
    }

    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException, QueryCancelledException {
        if(!useSimplify) {
            super.streamData(writer);
            return;
        }
        createQuantizerMap();
        Collection<? extends DataPointVO> vos = new ArrayList<>(voMap.values());
        if (info.isSingleArray()) {
            dao.wideBookendQueryCombined(vos, info.getFromMillis(), info.getToMillis(), null, this);
        } else {
            dao.wideBookendQueryPerPoint(vos, info.getFromMillis(), info.getToMillis(), null, this);
        }

        //Fast forward to end to fill any gaps at the end
        for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values()) {
            if(!quant.isDone())
                quant.done();
        }

        boolean singleArray = info.isSingleArray() && voMap.size() > 1;

        //Process the data into lists per data point, limit and simplify if necessary.
        Map<DataPointVO, List<DataPointValueTime>> processed = process(singleArray ? null : info.getLimit());

        if(singleArray) {
            //Combine into single array
            List<DataPointValueTime> values = new ArrayList<>();
            Iterator<DataPointVO> it = processed.keySet().iterator();
            while(it.hasNext()) {
                values.addAll(processed.get(it.next()));
            }

            //Sort by time
            Collections.sort(values);
            //Limit entire list
            if(info.getLimit() != null)
                values = values.subList(0, info.getLimit());

            //Reset current time and write out
            if(values.size() > 0) {
                long currentTime = values.get(0).getTime();
                List<DataPointValueTime> currentValues = new ArrayList<>();
                for(DataPointValueTime value : values) {
                    if(currentTime == value.getTime())
                        currentValues.add(value);
                    else {
                        if(currentValues.size() > 0) {
                            writer.writeDataPointValues(currentValues, currentValues.get(0).getTime());
                            currentValues.clear();
                        }
                        currentTime = value.getTime();
                        currentValues.add(value);
                    }
                }

                //Finish the current values
                if(currentValues.size() > 0)
                    writer.writeDataPointValues(currentValues, currentValues.get(0).getTime());
            }
        }else {
            Iterator<DataPointVO> it = processed.keySet().iterator();
            while(it.hasNext()) {
                DataPointVO key = it.next();
                List<DataPointValueTime> values = processed.get(key);
                if(!info.isSingleArray())
                    this.writer.writeStartArray(key.getXid());
                for(DataPointValueTime value : values) {
                    writer.writeDataPointValue(value);
                    count++;
                }
                if(!info.isSingleArray())
                    writer.writeEndArray();
            }
        }
    }

    /**
     * Process the data into lists per data point, simplify if necessary
     */
    private Map<DataPointVO, List<DataPointValueTime>> process(Integer limit) {
        Map<DataPointVO, List<DataPointValueTime>> processed = new LinkedHashMap<>();
        for(DataPointVO vo : voMap.values()) {
            List<DataPointStatisticsGenerator> generators = valueMap.get(vo.getId());
            List<DataPointValueTime> values = new ArrayList<>();
            if(generators.get(0).getGenerator() instanceof NoStatisticsGenerator) {
                //Iterate and combine into an array
                for(DataPointStatisticsGenerator gen : generators) {
                    NoStatisticsGenerator noGen = (NoStatisticsGenerator)gen.getGenerator();
                    for(IValueTime<DataValue> value : noGen.getValues()) {
                        values.add(new DataPointVOPointValueTimeBookend(vo, (IdPointValueTime)value));
                    }
                }
            }else {
                for(DataPointStatisticsGenerator generator : generators) {
                    values.add(new DataPointRollupPeriodValue(generator, RollupEnum.convertTo(vo.getRollup())));
                }
            }
            if(values.size() > 0) {

                //As the other endpoints, limit before simplification
                if(limit != null)
                    values = values.subList(0, limit);

                if(vo.isSimplifyDataSets()) {
                    if(vo.getSimplifyType() == DataPointVO.SimplifyTypes.TARGET)
                        values = SimplifyUtility.simplify(null, vo.getSimplifyTarget(), true, true, values);
                    else
                        values = SimplifyUtility.simplify(vo.getSimplifyTolerance(), null, true, true, values);
                }
                processed.put(vo, values);
            }
        }
        return processed;
    }

    @Override
    public void firstValue(IdPointValueTime value, boolean bookend) {
        try {
            if(!useSimplify) {
                super.firstValue(value, bookend);
                return;
            }
            DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
            updateQuantizers(value);
            quantizer.firstValue(value, bookend);
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void accept(IdPointValueTime value) {
        try {
            if(!useSimplify) {
                super.accept(value);
                return;
            }
            updateQuantizers(value);
            DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
            quantizer.accept(value);
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void lastValue(IdPointValueTime value, boolean bookend) {
        if(!useSimplify) {
            super.lastValue(value, bookend);
            return;
        }
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
        quantizer.lastValue(value, bookend);
    }

    @Override
    public void quantizedStatistics(DataPointStatisticsGenerator generator) throws QueryCancelledException {
        if(!useSimplify) {
            super.quantizedStatistics(generator);
            return;
        }
        //Separate them into a lists per data point
        List<DataPointStatisticsGenerator> stats = this.valueMap.get(generator.getVo().getId());
        if(stats == null) {
            stats = new ArrayList<>();
            this.valueMap.put(generator.getVo().getId(), stats);
        }
        stats.add(generator);
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        if(!useSimplify) {
            super.finish(writer);
            return;
        }

        if(info.isSingleArray())
            writer.writeEndArray();
        else {
            if(contentType == StreamContentType.JSON)
                writer.writeEndObject();
        }
    }

    @Override
    protected RollupEnum getRollup(DataPointVO vo) {
        return RollupEnum.convertTo(vo.getRollup());
    }
}
