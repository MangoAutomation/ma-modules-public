/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.rt.script.CompiledScriptExecutor;
import com.serotonin.m2m2.rt.script.JsonImportExclusion;
import com.serotonin.m2m2.rt.script.ScriptPointValueSetter;
import com.serotonin.m2m2.rt.script.ResultTypeException;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.rt.script.ScriptPermissionsException;
import com.serotonin.m2m2.rt.script.ScriptUtils;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkRT implements DataPointListener, PointLinkSetPointSource {
    public static final String CONTEXT_SOURCE_VAR_NAME = "source";
    public static final String CONTEXT_TARGET_VAR_NAME = "target";
    private final PointLinkVO vo;
    private final SystemEventType eventType;
    private final SystemEventType alreadyRunningEvent;
    private ScriptLog scriptLog;
    private CompiledScript compiledScript;
    private boolean compiled;
    private ScriptPointValueSetter setCallback;
    private final List<JsonImportExclusion> importExclusions;
    
    //Added to stop excessive point link calls
    private volatile Boolean ready;

    public PointLinkRT(PointLinkVO vo) {
        this.vo = vo;
        eventType = new SystemEventType(SystemEvent.TYPE_NAME, vo.getId(),
                EventType.DuplicateHandling.IGNORE_SAME_MESSAGE);
        alreadyRunningEvent = new SystemEventType(PointLinkAlreadyRunningEvent.TYPE_NAME, vo.getId(),
                EventType.DuplicateHandling.IGNORE_SAME_MESSAGE);
        compiledScript = null;
        compiled = false;
        ready = true;
        setCallback = new SetCallback(vo.getScriptPermissions());
        importExclusions = new ArrayList<>();
        importExclusions.add(new JsonImportExclusion("xid", vo.getXid()) {
			@Override
			public String getImporterType() {
				return PointLinkEmportDefinition.POINT_LINKS;
			}
        });
    }

    public void initialize() {
        Common.runtimeManager.addDataPointListener(vo.getSourcePointId(), this);
        checkSource();
        try {
        	compiledScript = CompiledScriptExecutor.compile(vo.getScript());
        	compiled = true;
        } catch (ScriptException e) {
            raiseFailureEvent(Common.timer.currentTimeMillis(), new TranslatableMessage("pointLinks.validate.scriptError", e.getMessage()));
        }
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
            ready = true;
            return;
        }

        if (!targetPoint.getPointLocator().isSettable()) {
            raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.targetNotSettable"));
            ready = true;
            return;
        }

        int targetDataType = targetPoint.getVO().getPointLocator().getDataTypeId();

        if (!StringUtils.isBlank(vo.getScript())) {
            Map<String, IDataPointValueSource> context = new HashMap<String, IDataPointValueSource>();
            context.put(CONTEXT_SOURCE_VAR_NAME, Common.runtimeManager.getDataPoint(vo.getSourcePointId()));
            context.put(CONTEXT_TARGET_VAR_NAME, Common.runtimeManager.getDataPoint(vo.getTargetPointId()));

            try {
            	if(!compiled) {
            		compiledScript = CompiledScriptExecutor.compile(vo.getScript());
            		compiled = true;
            	}
            		
                PointValueTime pvt = CompiledScriptExecutor.execute(compiledScript, context, null, newValue.getTime(),
                        targetDataType, newValue.getTime(), vo.getScriptPermissions(), new PrintWriter(new NullWriter()), 
                        scriptLog, setCallback, importExclusions, false);
                if (pvt.getValue() == null) {
                    raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.nullResult"));
                    ready = true;
                    return;
                }
                newValue = pvt;
            }
            catch (ScriptException e) {
                raiseFailureEvent(newValue.getTime(), new TranslatableMessage("pointLinks.validate.scriptError", e.getMessage()));
                ready = true;
                return;
            }
            catch(ScriptPermissionsException e) {
            	raiseFailureEvent(newValue.getTime(), e.getTranslatableMessage());
            	ready = true;
                return;
            }
            catch (ResultTypeException e) {
                raiseFailureEvent(newValue.getTime(), e.getTranslatableMessage());
                ready = true;
                return;
            }
        }

        if (DataTypes.getDataType(newValue.getValue()) != targetDataType) {
            raiseFailureEvent(newValue.getTime(), new TranslatableMessage("event.pointLink.convertError"));
            ready = true;
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
    
    @Override
	public void pointLogged(PointValueTime value) {
		if (vo.getEvent() == PointLinkVO.EVENT_LOGGED)
			execute(value);
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
        if (vo.isWriteAnnotation()){
        	DataPointVO vo = DataPointDao.instance.get(this.vo.getSourcePointId());
        	String xid;
        	if(vo != null)
        		xid = vo.getXid();
        	else
        		xid = "unknown";
            return new TranslatableMessage("annotation.pointLink", xid);
        }
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

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#getListenerName()
	 */
	@Override
	public String getListenerName() {
		return "Point Link " + this.vo.getXid();
	}
	
	class SetCallback extends ScriptPointValueSetter {
        public SetCallback(ScriptPermissions permissions) {
			super(permissions);
		}

		/*
         * (non-Javadoc)
         * 
         * @see
         * com.serotonin.mango.util.script.ScriptPointValueSetter#setImpl(com.serotonin.mango.rt.dataImage.IDataPointValueSource,
         * java.lang.Object, long)
         */
        @Override
        public void setImpl(IDataPointValueSource point, Object value, long timestamp, String annotation) {
            DataPointRT dprt = (DataPointRT) point;

            // We may, however, need to coerce the given value.
            try {
                DataValue mangoValue = ScriptUtils.coerce(value, dprt.getDataTypeId());
                PointValueTime newValue;
                if(StringUtils.isBlank(annotation))
                	newValue = new PointValueTime(mangoValue, timestamp);
                else
                	newValue = new AnnotatedPointValueTime(mangoValue, timestamp, new TranslatableMessage("literal", annotation));
                DataSourceRT<? extends DataSourceVO<?>> dsrt = Common.runtimeManager.getRunningDataSource(dprt.getDataSourceId());
                dsrt.setPointValue(dprt, newValue, PointLinkRT.this);
            }
            catch (ResultTypeException e) {
                // Raise an event
            	raiseFailureEvent(Common.timer.currentTimeMillis(), e.getTranslatableMessage());
            }
        }
    }
}
