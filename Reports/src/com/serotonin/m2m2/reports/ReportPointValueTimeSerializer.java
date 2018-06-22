/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports;

import java.io.IOException;

import com.serotonin.InvalidArgumentException;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.ImageSaveException;
import com.serotonin.m2m2.db.dao.nosql.ByteArrayBuilder;
import com.serotonin.m2m2.db.dao.nosql.NoSQLDataSerializer;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.ITime;

/**
 * @author Terry Packer
 *
 */
public class ReportPointValueTimeSerializer implements NoSQLDataSerializer{
	
	public static final ReportPointValueTimeSerializer instance = new ReportPointValueTimeSerializer();

	/**
	 * @return
	 */
	public static ReportPointValueTimeSerializer get() {
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.nosql.NoSQLDataSerializer#getObject(byte[], long)
	 */
	@Override
	public ITime getObject(ByteArrayBuilder b, long ts, String seriesId) {

		//Get the data type
		int dataType = b.getShort();
		DataValue dataValue = null;
		
		//Second put in the data value
		switch(dataType){
			case DataTypes.ALPHANUMERIC:
				String s = b.getString();
				dataValue = new AlphanumericValue(s);
				break;
			case DataTypes.BINARY:
				boolean bool  = b.getBoolean();
				dataValue = new BinaryValue(bool);
				break;
			case DataTypes.IMAGE:
				try {
					dataValue = new ImageValue(b.getString());
				} catch (InvalidArgumentException e1) {
					//Probably no file
				}
				break;
			case DataTypes.MULTISTATE:
				int i  = b.getInt();
				dataValue = new MultistateValue(i);
				break;
			case DataTypes.NUMERIC:
				double d  = b.getDouble();
				dataValue = new NumericValue(d);
				break;
			default:
				throw new ShouldNeverHappenException("Data type of " + dataType + " is not supported");
		}
		
		//Get the annotation
		String annotation = b.getString();
		
	
		if(annotation != null){
			try{
				return new AnnotatedPointValueTime(dataValue, ts, TranslatableMessage.deserialize(annotation));
			}catch(Exception e){
				throw new ShouldNeverHappenException(e);
			}
		}else{
			return new PointValueTime(dataValue, ts);
		}
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.nosql.NoSQLDataSerializer#getBytes(com.serotonin.m2m2.db.dao.nosql.NoSQLDataEntry)
	 */
	@Override
	public void putBytes(ByteArrayBuilder b, ITime obj, long timestamp, String seriesId) {
		PointValueTime value = (PointValueTime)obj;
		//First put in the data type
		b.putShort((short) value.getValue().getDataType());
		
		//Second put in the data value
		switch(value.getValue().getDataType()){
			case DataTypes.ALPHANUMERIC:
				b.putString(value.getStringValue());
				break;
			case DataTypes.BINARY:
				b.putBoolean(value.getBooleanValue());
				break;
			case DataTypes.IMAGE:
				ImageValue imageValue = (ImageValue) value.getValue();
	            if (!imageValue.isSaved()) {
	            	throw new ImageSaveException(new IOException("Image not saved."));
	            }
				b.putString(imageValue.getFilename());
				break;
			case DataTypes.MULTISTATE:
				b.putInt(value.getIntegerValue());
				break;
			case DataTypes.NUMERIC:
				b.putDouble(value.getDoubleValue());
				break;
			default:
				throw new ShouldNeverHappenException("Data type of " + value.getValue().getDataType() + " is not supported");

		}
		
		//Put in annotation
		if(value.isAnnotated()){
		    if(value instanceof AnnotatedPointValueTime) { //TODO resolve this workaround
    			AnnotatedPointValueTime apv = (AnnotatedPointValueTime)value;
    			b.putString(apv.getSourceMessage().serialize());
		    } else if(value instanceof AnnotatedIdPointValueTime) {
		        AnnotatedIdPointValueTime apv = (AnnotatedIdPointValueTime)value;
                b.putString(apv.getSourceMessage().serialize());
		    }
		}else
			b.putString(null);
	}

}
