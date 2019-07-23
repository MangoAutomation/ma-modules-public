/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.goebl.simplify.SimplifyUtility;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class MultiPointSimplifyTimeRangeDatabaseStream<T, INFO extends ZonedDateTimeRangeQueryInfo> extends MultiPointTimeRangeDatabaseStream<T, INFO> {

    protected final Map<Integer, BookendPair> bookendMap;
    protected final Map<Integer, List<DataPointVOPointValueTimeBookend>> valuesMap;
    
    /**
     * @param info
     * @param voMap
     * @param dao
     */
    public MultiPointSimplifyTimeRangeDatabaseStream(INFO info, Map<Integer, DataPointVO> voMap,
            PointValueDao dao) {
        super(info, voMap, dao);
        this.valuesMap = new LinkedHashMap<>();
        this.bookendMap = new HashMap<>(); 
    }

    @Override
    protected void writeValue(DataPointVOPointValueTimeBookend value) throws IOException {
        if(value.isBookend()) {
            BookendPair pair = bookendMap.get(value.getId());
            if(pair == null) {
                pair = new BookendPair();
                bookendMap.put(value.getId(), pair);
            }
            pair.addBookend(value);
        }else {
            //Store it for now
            List<DataPointVOPointValueTimeBookend> values = valuesMap.get(value.getId());
            if(values == null) {
                values = new ArrayList<>();
                valuesMap.put(value.getId(), values);
            }
            values.add(value);
        }
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        //Write out the values after simplifying
        Iterator<Integer> it = voMap.keySet().iterator();
        if(info.isSingleArray() && voMap.size() > 1) {
            List<DataPointVOPointValueTimeBookend> sorted = new ArrayList<>();
            while(it.hasNext()) {
                Integer id = it.next();
                BookendPair pair = bookendMap.get(id);
                if(pair != null && pair.startBookend != null)
                    sorted.add(pair.startBookend);
                List<DataPointVOPointValueTimeBookend> values = valuesMap.get(id);
                if(values != null) {
                    sorted.addAll(SimplifyUtility.simplify(info.simplifyTolerance, info.simplifyTarget, info.simplifyHighQuality, info.simplifyPrePostProcess, values));
                }
                if(pair != null && pair.endBookend != null) //Can be null bookend if limit is hit
                    sorted.add(pair.endBookend);
            }
            //Sort the Sorted List
            Collections.sort(sorted, new Comparator<DataPointVOPointValueTimeBookend>() {
                @Override
                public int compare(DataPointVOPointValueTimeBookend o1,
                        DataPointVOPointValueTimeBookend o2) {
                    return o1.getPvt().compareTo(o2.getPvt());
                }
                
            });
            for(DataPointVOPointValueTimeBookend value : sorted)
                super.writeValue(value);
        }else {
            while(it.hasNext()) {
                Integer id = it.next();
                List<DataPointVOPointValueTimeBookend> simplified;
                List<DataPointVOPointValueTimeBookend> values = valuesMap.get(id);
                if(values == null) {
                    simplified = Collections.emptyList();
                }else {
                    simplified = SimplifyUtility.simplify(info.simplifyTolerance, info.simplifyTarget, info.simplifyHighQuality, info.simplifyPrePostProcess, values);
                }
                BookendPair pair = bookendMap.get(id);
                if(pair != null && pair.startBookend != null)
                    super.writeValue(pair.startBookend);
                for(DataPointVOPointValueTimeBookend value : simplified)
                    super.writeValue(value);
                if(pair != null && pair.endBookend != null) //Can be null bookend if limit is hit
                    super.writeValue(pair.endBookend);
            }
        }
        super.finish(writer);
    }
    
    class BookendPair {
        DataPointVOPointValueTimeBookend startBookend;
        DataPointVOPointValueTimeBookend endBookend;
        
        /**
         * Add a bookend, its either the at the from time or to time
         * @param bookend
         */
        void addBookend(DataPointVOPointValueTimeBookend bookend) {
            if(bookend.getPvt().getTime() == info.getFromMillis())
                startBookend = bookend;
            else
                endBookend = bookend;
        }
    }
    
}
