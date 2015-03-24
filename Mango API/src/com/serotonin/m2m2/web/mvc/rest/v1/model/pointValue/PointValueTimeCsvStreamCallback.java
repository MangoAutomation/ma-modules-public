/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVWriter;

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
public class PointValueTimeCsvStreamCallback extends PointValueTimeCsvWriter implements MappedRowCallback<PointValueTime>{

	private final Log LOG = LogFactory.getLog(PointValueTimeJsonStreamCallback.class);

	private Translations translations;
	
	/**
	 * @param jgen
	 */
	public PointValueTimeCsvStreamCallback(CSVWriter writer, DataPointVO vo, boolean useRendered,  boolean unitConversion) {
		super(writer, vo, useRendered, unitConversion);
		this.translations = Common.getTranslations();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
		try{
			if(this.wroteHeaders == false)
				this.writeHeaders();
			String annotation = null;
			if(pvt.isAnnotated())
				annotation = ((AnnotatedPointValueTime) pvt).getAnnotation(translations);
			if(useRendered){
				//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, pvt);
				this.writePointValueTime(new AlphanumericValue(textValue), pvt.getTime(), annotation );
			}else if(unitConversion){
				if (pvt.getValue() instanceof NumericValue)
					this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue()), pvt.getTime(), annotation);
				else
					this.writePointValueTime(pvt.getValue(), pvt.getTime(), annotation);
			}else{
				this.writePointValueTime(pvt.getValue(), pvt.getTime(), annotation);
			}
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
		
	}

}
