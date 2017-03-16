/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.time;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.IdTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Interface to write Xid Time Values into a Stream
 * @author Terry Packer
 */
public interface XidTimeJsonWriter<T extends IdTime> {
	
	/**
	 * Write an Xid Time Value
	 * @param jgen
	 * @param vo
	 * @param value
	 */
	public abstract void writeXidTime(JsonGenerator jgen, DataPointVO vo, T value) throws IOException;

}
