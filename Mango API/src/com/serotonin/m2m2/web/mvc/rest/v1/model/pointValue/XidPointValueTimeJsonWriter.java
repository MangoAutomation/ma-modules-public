/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.XidTimeJsonWriter;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * 
 * Write 'xid': value 
 * 
 * output for JSON Stream Renderers
 * 
 * @author Terry Packer
 */
public class XidPointValueTimeJsonWriter extends PointValueTimeJsonWriter implements XidTimeJsonWriter<IdPointValueTime>{

	/**
	 * @param host
	 * @param port
	 * @param jgen
	 * @param useRendered
	 * @param unitConversion
	 */
	public XidPointValueTimeJsonWriter(String host, int port, JsonGenerator jgen, boolean useRendered,
			boolean unitConversion) {
		super(host, port, jgen, useRendered, unitConversion);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.time.XidTimeJsonWriter#writeXidTime(com.fasterxml.jackson.core.JsonGenerator, com.serotonin.m2m2.vo.DataPointVO, com.serotonin.m2m2.rt.dataImage.IdTime)
	 */
	@Override
	public void writeXidTime(JsonGenerator jgen, DataPointVO vo, IdPointValueTime value) throws IOException {
		if(useRendered){
			//Convert to Alphanumeric Value
			this.writeXidPointValue(value.getTime(), new AlphanumericValue(Functions.getRenderedText(vo, value)), vo);
		}else if(unitConversion){
			if (value.getValue() instanceof NumericValue)
				this.writeXidPointValue(value.getTime(), new NumericValue(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getValue().getDoubleValue())), vo);
			else
				this.writeXidPointValue(value.getTime(), value.getValue(), vo);
		}else{
			if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
				this.writeXidPointValue(value.getTime(), new AlphanumericValue(imageServletBuilder.buildAndExpand(value.getTime(), vo.getId()).toUri().toString()), vo);
			else
				this.writeXidPointValue(value.getTime(), value.getValue(), vo);
		}
	}
	
	private void writeXidPointValue(long timestamp, DataValue value, DataPointVO vo) throws IOException {
		if(value == null){
			this.jgen.writeNullField(vo.getXid());
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					this.jgen.writeStringField(vo.getXid(), value.getStringValue());
				break;
				case DataTypes.BINARY:
					this.jgen.writeBooleanField(vo.getXid(), value.getBooleanValue());
				break;
				case DataTypes.MULTISTATE:
					this.jgen.writeNumberField(vo.getXid(), value.getIntegerValue());
				break;
				case DataTypes.NUMERIC:
					this.jgen.writeNumberField(vo.getXid(), value.getDoubleValue());
				break;
				case DataTypes.IMAGE:
					this.jgen.writeStringField(vo.getXid(), imageServletBuilder.buildAndExpand(timestamp, vo.getId()).toUri().toString());
				break;
				default:
					throw new ShouldNeverHappenException("Uknown Data type: " + value.getDataType());
			}
		}
	}

}
