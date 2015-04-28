/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.DataPointVO.PlotTypes;
import com.serotonin.util.SerializationHelper;

/**
 * @author Terry Packer
 *
 */
public class M2MReportPointVO implements Serializable {
	
    private int pointId;
    private String colour;
    private boolean consolidatedChart;

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

    public boolean isConsolidatedChart() {
        return consolidatedChart;
    }

    public void setConsolidatedChart(boolean consolidatedChart) {
        this.consolidatedChart = consolidatedChart;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeInt(pointId);
        SerializationHelper.writeSafeUTF(out, colour);
        out.writeBoolean(consolidatedChart);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pointId = in.readInt();
            colour = SerializationHelper.readSafeUTF(in);
            consolidatedChart = true;
        }
        else if (ver == 2) {
            pointId = in.readInt();
            colour = SerializationHelper.readSafeUTF(in);
            consolidatedChart = in.readBoolean();
        }
    }

	/**
	 * @return
	 */
	public ReportPointVO convert(M2MReportDao legacyDao) {
		ReportPointVO point = new ReportPointVO();
		
		//Lookup point from M2M
		String legacyXid = legacyDao.getDataPointXid(this.pointId);
		
		//Lookup the new point by XID
		DataPointVO dp = DataPointDao.instance.getByXid(legacyXid);
		if(dp != null)
			point.setPointId(dp.getId());
		else
			throw new ShouldNeverHappenException("No point found in Mango that corresponds to M2M XID: " + legacyXid);

		point.setColour(colour);
		point.setConsolidatedChart(consolidatedChart);
		point.setPlotType(DataPointVO.PlotTypes.STEP);
		point.setPointKey("");
		point.setWeight(1);
		
		return point;
	}
	
	public void jsonWrite(JsonWriter jsonWriter, M2MReportDao legacyDao) throws IOException,JsonException {
		jsonWriter.indent();
		jsonWriter.append("{");
		jsonWriter.increaseIndent();
		
		String legacyXid = legacyDao.getDataPointXid(pointId);
		writeEntry("pointXid", legacyXid, jsonWriter, true);
		jsonWriter.append(",");
		writeEntry("pointKey", "", jsonWriter, true);
		jsonWriter.append(",");
		writeEntry("colour", colour, jsonWriter, true);
		jsonWriter.append(",");
		writeEntry("weight", new Float(1).toString(), jsonWriter, false);
		jsonWriter.append(",");
		writeEntry("consolidatedChart", Boolean.toString(consolidatedChart), jsonWriter, false);
		jsonWriter.append(",");
		writeEntry("plotType", Integer.toString(PlotTypes.STEP), jsonWriter, false);

		jsonWriter.decreaseIndent();
		jsonWriter.indent();
		jsonWriter.append("}");
	}
	
	private void writeEntry(String name, String value, JsonWriter writer, boolean quote) throws IOException{
		writer.indent();
		writer.quote(name);
		writer.append(':');
		if(quote)
			writer.quote(value);
		else
			writer.append(value);
	}
}
