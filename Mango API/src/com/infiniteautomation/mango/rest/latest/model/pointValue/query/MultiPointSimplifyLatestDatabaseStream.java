/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.goebl.simplify.SimplifyUtility;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class MultiPointSimplifyLatestDatabaseStream<T, INFO extends LatestQueryInfo> extends MultiPointLatestDatabaseStream<T, INFO>{

    //Map of seriesId to list
    protected final Map<Integer, List<DataPointVOPointValueTimeBookend>> valuesMap;

    /**
     */
    public MultiPointSimplifyLatestDatabaseStream(INFO info, Map<Integer, DataPointVO> voMap,
            PointValueDao dao) {
        super(info, voMap, dao);
        this.valuesMap = new HashMap<>();
    }

    @Override
    protected void writeValue(DataPointVOPointValueTimeBookend value) throws IOException {
        //Store it for now
        List<DataPointVOPointValueTimeBookend> values = valuesMap.get(value.getSeriesId());
        if(values == null) {
            values = new ArrayList<>();
            valuesMap.put(value.getSeriesId(), values);
        }
        values.add(value);
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        //Write out the values after simplifying
        Iterator<Integer> it = valuesMap.keySet().iterator();
        if(info.isSingleArray() && voMap.size() > 1) {
            List<DataPointVOPointValueTimeBookend> sorted = new ArrayList<>();
            while(it.hasNext())
                sorted.addAll(SimplifyUtility.simplify(info.simplifyTolerance, info.simplifyTarget, info.simplifyHighQuality, info.simplifyPrePostProcess, valuesMap.get(it.next())));
            //Sort the Sorted List
            sorted.sort(Comparator.comparingLong(DataPointVOPointValueTimeBookend::getTime));
            for(DataPointVOPointValueTimeBookend value : sorted)
                super.writeValue(value);
        }else {
            while(it.hasNext()) {
                List<DataPointVOPointValueTimeBookend> values = SimplifyUtility.simplify(info.simplifyTolerance, info.simplifyTarget, info.simplifyHighQuality, info.simplifyPrePostProcess, valuesMap.get(it.next()));
                for(DataPointVOPointValueTimeBookend value : values)
                    super.writeValue(value);
            }
        }
        super.finish(writer);
    }
}
