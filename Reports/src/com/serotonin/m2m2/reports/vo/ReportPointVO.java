/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.infiniteautomation.mango.spring.dao.DataPointDao;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.util.SerializationHelper;

public class ReportPointVO implements Serializable, JsonSerializable {
    
	private int pointId;
	@JsonProperty
	private String pointKey;
	@JsonProperty
    private String colour;
	@JsonProperty
    private float weight = 1;
	@JsonProperty
    private boolean consolidatedChart;
	@JsonProperty
    private int plotType;

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

	public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isConsolidatedChart() {
        return consolidatedChart;
    }

    public void setConsolidatedChart(boolean consolidatedChart) {
        this.consolidatedChart = consolidatedChart;
    }

    public int getPlotType() {
        return plotType;
    }

    public void setPlotType(int plotType) {
        this.plotType = plotType;
    }
    
    public String getPointKey() {
    	return pointKey;
    }
    
    public void setPointKey(String pointKey) {
    	this.pointKey = pointKey;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 4;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeInt(pointId);
        SerializationHelper.writeSafeUTF(out, pointKey);
        SerializationHelper.writeSafeUTF(out, colour);
        out.writeFloat(weight);
        out.writeBoolean(consolidatedChart);
        out.writeInt(plotType);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pointId = in.readInt();
            pointKey = "";
            colour = SerializationHelper.readSafeUTF(in);
            weight = in.readFloat();
            consolidatedChart = true;
            plotType = DataPointVO.PlotTypes.STEP;
        }
        else if (ver == 2) {
            pointId = in.readInt();
            pointKey = "";
            colour = SerializationHelper.readSafeUTF(in);
            weight = in.readFloat();
            consolidatedChart = in.readBoolean();
            plotType = DataPointVO.PlotTypes.STEP;
        }
        else if (ver == 3) {
            pointId = in.readInt();
            pointKey = "";
            colour = SerializationHelper.readSafeUTF(in);
            weight = in.readFloat();
            consolidatedChart = in.readBoolean();
            plotType = in.readInt();
        }
        else if (ver == 4) {
            pointId = in.readInt();
            pointKey = SerializationHelper.readSafeUTF(in);
            colour = SerializationHelper.readSafeUTF(in);
            weight = in.readFloat();
            consolidatedChart = in.readBoolean();
            plotType = in.readInt();
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonRead(com.serotonin.json.JsonReader, com.serotonin.json.type.JsonObject)
	 */
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
		
		String text = jsonObject.getString("pointXid");
		if(text == null){
			throw new TranslatableJsonException("reports.emport.point.missingAttr", "pointXid");
		}else {
		    Integer id = DataPointDao.instance.getIdByXid(text);
			if(id == null)
				throw new TranslatableJsonException("reports.emport.pointDNE", text);
			else
			    this.pointId = id;
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonWrite(com.serotonin.json.ObjectWriter)
	 */
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		String xid = DataPointDao.instance.getXidById(pointId);
		if(xid != null)
	        writer.writeEntry("pointXid", xid);		
	}
}
