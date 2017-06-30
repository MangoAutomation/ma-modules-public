/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonStreamCallback extends PointValueTimeJsonWriter implements MappedRowCallback<PointValueTime>{

	private final Log LOG = LogFactory.getLog(PointValueTimeJsonStreamCallback.class);

	private Translations translations;
	private DataPointVO vo;
	private final LimitCounter limiter;
	
	/**
	 * @param jgen
	 */
	public PointValueTimeJsonStreamCallback(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered,  boolean unitConversion, Integer limit) {
		super(host, port, jgen, useRendered, unitConversion);
		this.vo = vo;
		this.limiter = new LimitCounter(limit);
		this.translations = Common.getTranslations();		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
		if(this.limiter.limited())
			return;
		
		try{
			String annotation = null;
			if(pvt.isAnnotated())
				annotation = ((AnnotatedPointValueTime) pvt).getAnnotation(translations);
			if(useRendered){
				//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, pvt);
				this.writePointValueTime(new AlphanumericValue(textValue), pvt.getTime(), annotation, vo);
			}else if(unitConversion){
				if (pvt.getValue() instanceof NumericValue)
					this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue()), pvt.getTime(), annotation, vo);
				else
					this.writePointValueTime(pvt.getValue(), pvt.getTime(), annotation, vo);
			}else{
				this.writePointValueTime(pvt.getValue(), pvt.getTime(), annotation, vo);
			}
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
		
	}

}
