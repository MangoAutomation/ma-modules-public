/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonTypeReader;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor;

/**
 * @author Terry Packer
 *
 */
public class PointLocatorPropertyEditor extends CSVPropertyEditor{

	private static Log LOG = LogFactory.getLog(PointLocatorPropertyEditor.class);


	private PointLocatorVO vo;
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		this.vo = (PointLocatorVO)value;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#getValue()
	 */
	@Override
	public Object getValue() {
		return this.vo;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#getAsText()
	 */
	@Override
	public String getAsText() {
		//Simply use the Sero JSON for now
		StringWriter sw = new StringWriter();
		JsonWriter writer = new JsonWriter(Common.JSON_CONTEXT, sw);
		//TODO Make optional somehow
        int prettyIndent = 3;
        writer.setPrettyIndent(prettyIndent);
        writer.setPrettyOutput(true);
        
		try {
			writer.writeObject(this.vo);
			writer.flush();
			return sw.toString();
		} catch (JsonException e) {
			LOG.error(e);
			throw new ShouldNeverHappenException(e.getMessage());
		} catch (IOException e) {
			LOG.error(e);
			throw new ShouldNeverHappenException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPropertyEditor#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		JsonTypeReader typeReader = new JsonTypeReader(text);
		try{
			JsonValue value = typeReader.read();
	        if (value instanceof JsonObject) {
	            JsonObject root = value.toJsonObject();
	            
	            //Use the context to build get the Data Point to set
	            DataPointVO dp = (DataPointVO)this.context;
	            //Use the XID 
	            DataPointVO vo = DataPointDao.instance.getDataPoint(dp.getXid());
	            if (vo == null) {
	                // Locate the data source for the point.
	                DataSourceVO<?> dsvo = DataSourceDao.instance.getDataSource(dp.getDataSourceXid());
	                if (dsvo == null)
	                    throw new ShouldNeverHappenException("Bad DS XID."); //addFailureMessage("emport.dataPoint.badReference", xid);
	                else {
	                    this.vo = dsvo.createPointLocator();
	                }
	            }else{
	            	this.vo = vo.getPointLocator();
	            }
	            //Now Fill it
	            JsonReader reader = new JsonReader(Common.JSON_CONTEXT, root);
	            reader.readInto(this.vo);
	            
	        }
		}
        catch (ClassCastException e) {
            LOG.error(e.getMessage());
        }
        catch (TranslatableJsonException e) {
        	LOG.error(e.getMessage());
        }
        catch (IOException e) {
        	LOG.error(e.getMessage());
        }
        catch (JsonException e) {
        	LOG.error(e.getMessage());
        }
	}

}
