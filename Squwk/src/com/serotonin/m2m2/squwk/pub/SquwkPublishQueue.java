package com.serotonin.m2m2.squwk.pub;

import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.publish.PublishQueue;
import com.serotonin.m2m2.rt.publish.PublisherRT;

public class SquwkPublishQueue extends PublishQueue<SquwkPointVO> {
    public SquwkPublishQueue(PublisherRT<SquwkPointVO> owner, int warningSize, int discardSize) {
        super(owner, warningSize, discardSize);
    }

    @Override
    public synchronized void add(SquwkPointVO vo, PointValueTime pvt) {
        //        // Remove duplicate points.
        //        Iterator<PublishQueueEntry<SquwkPointVO>> iter = queue.iterator();
        //        while (iter.hasNext()) {
        //            SquwkPointVO entry = iter.next().getVo();
        //            if (entry.getFeedId() == vo.getFeedId() && entry.getDataStreamId() == vo.getDataStreamId())
        //                iter.remove();
        //        }
        //
        super.add(vo, pvt);
    }
}
