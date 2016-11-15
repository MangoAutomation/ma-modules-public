/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeCsvStreamCallback extends PointValueTimeCsvWriter implements MappedRowCallback<PointValueTime>{

	private final Log LOG = LogFactory.getLog(PointValueTimeJsonStreamCallback.class);

	private Translations translations;
	private DataPointVO vo;
	
	/**
	 * @param jgen
	 */
	public PointValueTimeCsvStreamCallback(String host, int port, CSVWriter writer, DataPointVO vo, boolean useRendered,  boolean unitConversion, boolean writeXid, boolean writeHeaders) {
		super(host, port, writer, useRendered, unitConversion, writeXid, writeHeaders);
		this.translations = Common.getTranslations();
		this.vo = vo;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
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
				if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
					this.writePointValueTime(imageServletBuilder.buildAndExpand(pvt.getTime(), vo.getId()).toUri().toString(), pvt.getTime(), annotation, vo);
				else
					this.writePointValueTime(pvt.getValue(), pvt.getTime(), annotation, vo);
			}
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
		
	}

}
