/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.rt.script.ResultTypeException;
import com.serotonin.m2m2.rt.script.ScriptExecutor;
import com.serotonin.m2m2.rt.script.ScriptLog;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkRT implements DataPointListener, PointLinkSetPointSource {
    public static final String CONTEXT_VAR_NAME = "source";
    private final PointLinkVO vo;
    private final SystemEventType eventType;
    private final SystemEventType alreadyRunningEvent;
    private ScriptLog scriptLog;
    
    //Added to stop excessive point link calls
    private volatile Boolean ready;

    public PointLinkRT(PointLinkVO vo) {
        this.vo = vo;
        eventType = new SystemEventType(SystemEvent.TYPE_NAME, vo.getId(),
                EventType.DuplicateHandling.IGNORE_SAME_MESSAGE);
        alreadyRunningEvent = new SystemEventType(PointLinkAlreadyRunningEvent.TYPE_NAME, vo.getId(),
                EventType.DuplicateHandling.IGNORE_SAME_MESSAGE);
        ready = true;
    }

    public void initialize() {
        Common.runtimeManager.addDataPointListener(vo.getSourcePointId(), this);
        checkSource();
        File file = getLogFile(this.vo.getId());
        PrintWriter out;
        try {
            out = new PrintWriter(file);
        }
        catch (IOException e) {
            raiseFailureEvent( new TranslatableMessage(
                    "event.pointLink.logError.open", file.getPath(), e.getMessage()));
            out = new PrintWriter(new NullWriter());
        }
        scriptLog = new ScriptLog(out, vo.getLogLevel());
        scriptLog.info("Data point started");
    }

    public void terminate() {
        Common.runtimeManager.removeDataPointListener(vo.getSourcePointId(), this);
        returnToNormal();
    }

    public int getId() {
        return vo.getId();
    }

    private void checkSource() {
        DataPointRT source = Common.runtimeManager.getDataPoint(vo.getSourcePointId());
        if (source == null)
            // The source has been terminated, was never enabled, or not longer exists.
            raiseFailureEvent(new TranslatableMessage("event.pointLink.sourceUnavailable"));
        else
            // Everything is good
            returnToNormal();
    }

    private void raiseFailureEvent(TranslatableMessage message) {
        raiseFailureEvent(System.currentTimeMillis(), message);
    }

    private void raiseFailureEvent(long time, TranslatableMessage message) {
        SystemEventType.raiseEvent(eventType, time, true, message);
    }

    private void returnToNormal() {
        SystemEventType.returnToNormal(eventType, System.currentTimeMillis());
    }

    private void execute(PointValueTime newValue) {
    	
    	//Bail out if already running a point link operation
	    synchronized(ready){
	    	if(!ready){
	    		SystemEventType.raiseEvent(alreadyRunningEvent, newValue.getTime(), true, new TranslatableMessage("event.pointLink.duplicateRuns"));
	    		return;
	    	}else{
	    		ready = false; //Stop anyone else from using this 
	    		SystemEventType.returnToNormal(alreadyRunningEvent, System.currentTimeMillis());
	    	}
    	}
        // Propagate the update to the target point. Validate that the target point is available.
        DataPointRT targetPoint = Common.runtimeManager.getDataPoint(vo.getTargetPointId());
        if (targetPoint == null) {
            raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.targetUnavailable"));
            return;
        }

        if (!targetPoint.getPointLocator().isSettable()) {
            raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.targetNotSettable"));
            return;
        }

        int targetDataType = targetPoint.getVO().getPointLocator().getDataTypeId();

        if (!StringUtils.isBlank(vo.getScript())) {
            ScriptExecutor scriptExecutor = new ScriptExecutor();
            Map<String, IDataPointValueSource> context = new HashMap<String, IDataPointValueSource>();
            DataPointRT source = Common.runtimeManager.getDataPoint(vo.getSourcePointId());
            context.put(CONTEXT_VAR_NAME, source);

            try {
                PointValueTime pvt = scriptExecutor.execute(vo.getScript(), context, null, newValue.getTime(),
                        targetDataType, newValue.getTime(), vo.getScriptPermissions(), new PrintWriter(new NullWriter()), scriptLog);
                if (pvt.getValue() == null) {
                    raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.nullResult"));
                    return;
                }
                newValue = pvt;
            }
            catch (ScriptException e) {
                raiseFailureEvent(newValue.getTime(), new TranslatableMessage("common.default", e.getMessage()));
                return;
            }
            catch (ResultTypeException e) {
                raiseFailureEvent(newValue.getTime(), e.getTranslatableMessage());
                return;
            }
        }

        if (DataTypes.getDataType(newValue.getValue()) != targetDataType) {
            raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.convertError"));
            return;
        }

        // Queue a work item to perform the update.
        Common.backgroundProcessing.addWorkItem(new PointLinkSetPointWorkItem(vo.getTargetPointId(), newValue, this));
        returnToNormal();
    }

    //
    //
    // DataPointListener
    //
    @Override
    public void pointInitialized() {
        checkSource();
    }

    @Override
    public void pointTerminated() {
        checkSource();
    }

    @Override
    public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
        if (vo.getEvent() == PointLinkVO.EVENT_CHANGE)
            execute(newValue);
    }

    @Override
    public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
        // No op
    }

    @Override
    public void pointBackdated(PointValueTime value) {
        // No op
    }

    @Override
    public void pointUpdated(PointValueTime newValue) {
        if (vo.getEvent() == PointLinkVO.EVENT_UPDATE)
            execute(newValue);
    }

    //
    //
    // SetPointSource
    //
    @Override
    public int getSetPointSourceId() {
        return vo.getId();
    }

    @Override
    public String getSetPointSourceType() {
        return "POINT_LINK";
    }

    @Override
    public TranslatableMessage getSetPointSourceMessage() {
        if (vo.isWriteAnnotation())
            return new TranslatableMessage("annotation.pointLink");
        return null;
    }

    @Override
    public void raiseRecursionFailureEvent() {
        raiseFailureEvent(new TranslatableMessage("event.pointLink.recursionFailure"));
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.pointLinks.PointLinkSetPointSource#pointSetComplete()
	 */
	@Override
	public void pointSetComplete() {
		this.ready = true;
	}
	
    public static File getLogFile(int pointId) {
        return new File(Common.getLogsDir(), "pointLink-" + pointId + ".log");
    }
}
