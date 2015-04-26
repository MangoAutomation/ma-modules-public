package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity(typeName=AnalogAttractorChangeModelDefinition.TYPE_NAME)
public class AnalogAttractorChangeModel extends VirtualPointLocatorModel {
	
	public AnalogAttractorChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.ANALOG_ATTRACTOR);
	}
	
	public AnalogAttractorChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.ANALOG_ATTRACTOR);
	}
	@Override
	public String getTypeName() {
		return AnalogAttractorChangeModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("maxChange")
	@CSVColumnGetter(order=14, header="maxChange")
	public double getMaxChange() {
	    return this.data.getAnalogAttractorChange().getMaxChange();
	}

	@JsonSetter("maxChange")
	@CSVColumnSetter(order=14, header="maxChange")
	public void setMaxChange(double maxChange) {
	    this.data.getAnalogAttractorChange().setMaxChange(maxChange);
	}

	@JsonGetter("volatility")
	@CSVColumnGetter(order=15, header="volatility")
	public double getVolatility() {
	    return this.data.getAnalogAttractorChange().getVolatility();
	}

	@JsonSetter("volatility")
	@CSVColumnSetter(order=15, header="volatility")
	public void setVolatility(double volatility) {
	    this.data.getAnalogAttractorChange().setVolatility(volatility);
	}

	@JsonGetter("attractionPointXid")
	@CSVColumnGetter(order=16, header="attractionPointXid")
	public String getAttractionPointXid() {
		DataPointVO dpvo = DataPointDao.instance.get(this.data.getAnalogAttractorChange().getAttractionPointId());
		if(dpvo != null)
			return dpvo.getXid();
		return "";
	}

	@JsonSetter("attractionPointXid")
	@CSVColumnSetter(order=16, header="attractionPointXid")
	public void setAttractionPointXid(String attractionPointXid) {
		DataPointVO dpvo = DataPointDao.instance.getByXid(attractionPointXid);
		if(dpvo != null)
			this.data.getAnalogAttractorChange().setAttractionPointId(dpvo.getId());
	}



}
