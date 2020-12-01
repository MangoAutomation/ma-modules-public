/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.db.query.BookendQueryCallback;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public abstract class PointValueTimeDatabaseStream<T, INFO extends LatestQueryInfo> extends PointValueTimeQueryStream<T, INFO> implements BookendQueryCallback<IdPointValueTime>{

    protected PointValueDao dao;
    protected PointValueTimeWriter writer;

    public PointValueTimeDatabaseStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap);
        this.dao = dao;
    }

    @Override
    public void start(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        this.writer = writer;
        super.start(writer);
    }
}
