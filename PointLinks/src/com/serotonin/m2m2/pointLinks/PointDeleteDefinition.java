package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.module.DataPointChangeDefinition;
import com.serotonin.m2m2.vo.DataPointVO;

public class PointDeleteDefinition extends DataPointChangeDefinition {
    @Override
    public void beforeInsert(DataPointVO dpvo) {
        // no op
    }

    @Override
    public void afterInsert(DataPointVO dpvo) {
        // no op
    }

    @Override
    public void beforeUpdate(DataPointVO dpvo) {
        // no op
    }

    @Override
    public void afterUpdate(DataPointVO dpvo) {
        // no op
    }

    @Override
    public void beforeDelete(int dataPointId) {
        for (PointLinkVO link : new PointLinkDao().getPointLinksForPoint(dataPointId))
            RTMDefinition.instance.deletePointLink(link.getId());
    }

    @Override
    public void afterDelete(int dataPointId) {
        // no op
    }
}
