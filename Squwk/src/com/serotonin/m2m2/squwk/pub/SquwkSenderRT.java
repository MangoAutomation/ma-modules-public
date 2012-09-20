package com.serotonin.m2m2.squwk.pub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.rt.publish.PublishQueue;
import com.serotonin.m2m2.rt.publish.PublishQueueEntry;
import com.serotonin.m2m2.rt.publish.PublishedPointRT;
import com.serotonin.m2m2.rt.publish.PublisherRT;
import com.serotonin.m2m2.rt.publish.SendThread;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.squwk.client.ServiceRequest;
import com.serotonin.squwk.client.ServiceResultObjectHandler;
import com.serotonin.squwk.client.SquwkClient;
import com.serotonin.squwk.client.SquwkException;
import com.serotonin.squwk.client.request.PointListRequest;
import com.serotonin.squwk.client.request.SampleAppendRequest;
import com.serotonin.squwk.client.vo.DataType;
import com.serotonin.squwk.client.vo.Point;

public class SquwkSenderRT extends PublisherRT<SquwkPointVO> {
    private static final Log LOG = LogFactory.getLog(SquwkSenderRT.class);
    private static final int MAX_BATCH = 100;

    public static final int REQUEST_EXCEPTION_EVENT = 11;
    public static final int SERVICE_EXCEPTION_EVENT = 12;

    final EventType requestExceptionEventType = new PublisherEventType(getId(), REQUEST_EXCEPTION_EVENT);
    final EventType serviceExceptionEventType = new PublisherEventType(getId(), SERVICE_EXCEPTION_EVENT);

    final SquwkSenderVO vo;
    final SquwkClient squwkClient;
    private List<Point> squwkPoints;

    public SquwkSenderRT(SquwkSenderVO vo) {
        super(vo);
        this.vo = vo;
        squwkClient = new SquwkClient(vo.getAccessKey(), vo.getSecretKey());
    }

    @Override
    protected PublishQueue<SquwkPointVO> createPublishQueue(PublisherVO<SquwkPointVO> vo) {
        return new SquwkPublishQueue(this, vo.getCacheWarningSize(), vo.getCacheDiscardSize());
    }

    //
    //
    // Lifecycle
    //
    @Override
    public void initialize() {
        super.initialize(new SquwkSendThread());

        // Get the list of points from Squwk.
        try {
            squwkPoints = squwkClient.send(new PointListRequest());
        }
        catch (SquwkException e) {
            LOG.warn("Request exception", e);
            Common.eventManager.raiseEvent(requestExceptionEventType, System.currentTimeMillis(), true,
                    AlarmLevels.URGENT, toTranslatableMessage(e), createEventContext());
        }
    }

    @Override
    protected void pointInitialized(PublishedPointRT<SquwkPointVO> rt) {
        super.pointInitialized(rt);

        if (squwkClient != null) {
            String guid = rt.getVo().getGuid();
            for (Point point : squwkPoints) {
                if (point.getGuid().equals(guid)) {
                    rt.getVo().setDataType(point.getDataType());
                    break;
                }
            }
        }
    }

    PublishQueue<SquwkPointVO> getPublishQueue() {
        return queue;
    }

    class SquwkSendThread extends SendThread {
        SquwkSendThread() {
            super("SquwkSenderRT.SendThread");
        }

        @Override
        protected void runImpl() {
            while (isRunning()) {
                List<PublishQueueEntry<SquwkPointVO>> entries = getPublishQueue().get(MAX_BATCH);

                if (entries != null) {
                    if (send(entries))
                        getPublishQueue().removeAll(entries);
                    else
                        // The send failed, so take a break so as not to over exert ourselves.
                        sleepImpl(2000);
                }
                else
                    waitImpl(10000);
            }
        }

        @SuppressWarnings("synthetic-access")
        private boolean send(List<PublishQueueEntry<SquwkPointVO>> entries) {
            List<SampleAppendRequest> reqs = new ArrayList<SampleAppendRequest>();

            for (PublishQueueEntry<SquwkPointVO> entry : entries) {
                SquwkPointVO vo = entry.getVo();
                PointValueTime pvt = entry.getPvt();
                SampleAppendRequest req = new SampleAppendRequest(vo.getGuid(), pvt.getTime(), coerceDataValue(
                        pvt.getValue(), vo.getDataType()));
                reqs.add(req);
            }

            // Send the request. Set message non-null if there is a failure.
            ResultHandler resultHandler = new ResultHandler();

            try {
                squwkClient.sendBatch(reqs, resultHandler);

                // If we made it this far, the request was ok.
                Common.eventManager.returnToNormal(requestExceptionEventType, System.currentTimeMillis());

                // Check for service exception.
                if (resultHandler.getSquwkException() != null) {
                    LOG.warn("Service exception", resultHandler.getSquwkException());
                    Common.eventManager.raiseEvent(serviceExceptionEventType, System.currentTimeMillis(), false,
                            AlarmLevels.URGENT, toTranslatableMessage(resultHandler.getSquwkException()),
                            createEventContext());
                }
            }
            catch (SquwkException e) {
                LOG.warn("Request exception", e);
                Common.eventManager.raiseEvent(requestExceptionEventType, System.currentTimeMillis(), true,
                        AlarmLevels.URGENT, toTranslatableMessage(e), createEventContext());
                return false;
            }

            return true;
        }
    }

    private Object coerceDataValue(DataValue mv, DataType dataType) {
        if (dataType == null)
            return mv.getObjectValue();

        switch (dataType) {
        case EVENT:
            return null;
        case BINARY:
            return mv.getBooleanValue();
        case INTEGER:
            return mv.getIntegerValue();
        case FLOAT:
            return mv.getDoubleValue();
        }
        return mv.getStringValue();
    }

    class ResultHandler extends ServiceResultObjectHandler {
        private SquwkException squwkException;

        @Override
        public <T> void exception(ServiceRequest<T> serviceRequest, SquwkException squwkException) {
            if (this.squwkException != null)
                this.squwkException = squwkException;
        }

        @Override
        public <T> void handleServiceResultObject(ServiceRequest<T> request, T result) {
            // Sample append requests return null, so no need to do anything here.
        }

        public SquwkException getSquwkException() {
            return squwkException;
        }
    }

    private TranslatableMessage toTranslatableMessage(SquwkException e) {
        return new TranslatableMessage("common.default", e.getMessage());
    }
}
