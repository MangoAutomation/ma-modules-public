/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
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
     */
    public MultiPointStatisticsStream(ZonedDateTimeStatisticsQueryInfo info, Map<Integer, DataPointVO> voMap,
            PointValueDao dao) {
        super(info, voMap, dao);
        this.statsMap = new HashMap<>(voMap.size());
    }

    @Override
    protected void processRow(IdPointValueTime value, boolean firstBookend,
            boolean lastBookend, boolean cached) throws QueryCancelledException {

        try {
            final DataPointVO vo = voMap.get(value.getSeriesId());
            if(info.isUseCache() != PointValueTimeCacheControl.NONE && !cached)
                if(!processValueThroughCache(value, firstBookend, lastBookend))
                    return;
            StatisticsGenerator generator = statsMap.compute(value.getSeriesId(), (k, v) -> {
                if(v == null) {
                    switch(vo.getPointLocator().getDataType()){
                        case BINARY:
                        case MULTISTATE:
                            v = new StartsAndRuntimeList(info.getFromMillis(), info.getToMillis(), value);
                            break;
                        case ALPHANUMERIC:
                            v = new ValueChangeCounter(info.getFromMillis(), info.getToMillis(), value);
                            break;
                        case NUMERIC:
                            v = new AnalogStatistics(info.getFromMillis(), info.getToMillis(), value);
                            break;
                        default:
                            throw new ShouldNeverHappenException("Invalid Data Type: "+ voMap.get(value.getSeriesId()).getPointLocator().getDataType());
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
                DataPointStatisticsGenerator gen = new DataPointStatisticsGenerator(vo, generator);

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
                this.writer.writeAllStatistics(generator, vo, rendered, fields.contains(PointValueField.RAW));

                this.writer.writeEndObject();
            }
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }

    }

    @Override
    public void start(PointValueTimeWriter writer) throws IOException {
        this.writer = writer;
        this.writer.writeStartObject();
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        writer.writeEndObject();
    }
}
