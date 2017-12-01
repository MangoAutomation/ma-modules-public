/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class MultiPointSimplifyLatestDatabaseStream<T, INFO extends LatestQueryInfo> extends MultiPointLatestDatabaseStream<T, INFO>{
    
    protected final Map<Integer, List<DataPointVOPointValueTimeBookend>> valuesMap;
    
    /**
     * @param info
     * @param voMap
     * @param dao
     */
    public MultiPointSimplifyLatestDatabaseStream(INFO info, Map<Integer, DataPointVO> voMap,
            PointValueDao dao) {
        super(info, voMap, dao);
        this.valuesMap = new HashMap<>();
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream#writeValue(com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend)
     */
    @Override
    protected void writeValue(DataPointVOPointValueTimeBookend value) throws IOException {
        //Store it for now
        List<DataPointVOPointValueTimeBookend> values = valuesMap.get(value.getId());
        if(values == null) {
            values = new ArrayList<>();
            valuesMap.put(value.getId(), values);
        }
        values.add(value);
    }
    
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream#finish(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        //Write out the values after simplifying
        Iterator<Integer> it = valuesMap.keySet().iterator();
        while(it.hasNext()) {
            List<DataPointVOPointValueTimeBookend> values = simplify(valuesMap.get(it.next()));
            for(DataPointVOPointValueTimeBookend value : values)
                super.writeValue(value);
        }
        super.finish(writer);
    }
}
