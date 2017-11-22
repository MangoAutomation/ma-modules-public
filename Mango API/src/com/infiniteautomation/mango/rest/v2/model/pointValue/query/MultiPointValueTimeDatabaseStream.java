/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Concrete Implementation for a single data point
 *
 * @author Terry Packer
 */
public class MultiPointValueTimeDatabaseStream<T> extends PointValueTimeDatabaseStream<T>{
    
    protected long currentTime; //For writing multiple points single array
    protected final List<DataPointVOPointValueTimeBookend> currentValues;
    protected final List<DataPointVOPointValueTimeBookend> finalValues;
    
    public MultiPointValueTimeDatabaseStream(ZonedDateTimeRangeQueryInfo info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.currentValues = new ArrayList<>(voMap.size());
        this.finalValues = new ArrayList<>(voMap.size());
    }
    
    
    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#firstValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int, boolean)
     */
    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException{
        DataPointVO vo = this.voMap.get(value.getId());
        
        if(info.isSingleArray() && voMap.size() > 1) {
            //Do tracking
            currentTime = value.getTime();
            currentValues.add(new DataPointVOPointValueTimeBookend(vo, value, bookend));
        }else {
            if(voMap.size() > 1)
                writer.startWriteArray(vo.getXid());
            writer.writePointValueTime(vo, value, bookend);
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException{
        if(info.isSingleArray() && voMap.size() > 1) {
            if(currentTime == value.getTime())
                currentValues.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value));
            else {
                writer.writeMultiplePointsAsObject(currentValues);
                currentTime = value.getTime();
                currentValues.clear();
                currentValues.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value));
            }
        }else
            writer.writePointValueTime(this.voMap.get(value.getId()), value);
    }
    
    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index) throws IOException{
        if(info.isSingleArray() && voMap.size() > 1) {
            finalValues.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value, true));
        }else {
            if(voMap.size() > 1)
                writer.endWriteArray();
            writer.writePointValueTime(this.voMap.get(value.getId()), value, true);
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream#finish()
     */
    @Override
    public void finish() throws IOException {
        //Write out all our current values and the final bookend
        if(info.isSingleArray() && voMap.size() > 0) {
            writer.writeMultiplePointsAsObject(currentValues);
            writer.writeMultiplePointsAsObject(finalValues);
        }
    }
    
}
