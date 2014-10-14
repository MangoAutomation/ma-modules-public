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

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonStreamCallback extends PointValueTimeJsonWriter implements MappedRowCallback<PointValueTime>{

	private final Log LOG = LogFactory.getLog(PointValueTimeJsonStreamCallback.class);

	private Translations translations;
	
	/**
	 * @param jgen
	 */
	public PointValueTimeJsonStreamCallback(JsonGenerator jgen) {
		super(jgen);
		this.translations = Common.getTranslations();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
		try{
		if(pvt instanceof AnnotatedPointValueTime)
			this.writePointValueTime(pvt.getValue(), pvt.getTime(), ((AnnotatedPointValueTime) pvt).getAnnotation(translations) );
		else
			this.writePointValueTime(pvt.getValue(), pvt.getTime(), null);
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
		
	}

}
