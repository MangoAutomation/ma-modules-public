package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity()
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
	public double getMaxChange() {
	    return this.data.getAnalogAttractorChange().getMaxChange();
	}

	@JsonSetter("maxChange")
	public void setMaxChange(double maxChange) {
	    this.data.getAnalogAttractorChange().setMaxChange(maxChange);
	}

	@JsonGetter("volatility")
	public double getVolatility() {
	    return this.data.getAnalogAttractorChange().getVolatility();
	}

	@JsonSetter("volatility")
	public void setVolatility(double volatility) {
	    this.data.getAnalogAttractorChange().setVolatility(volatility);
	}

	@JsonGetter("attractionPointXid")
	public String getAttractionPointXid() {
		DataPointVO dpvo = DataPointDao.instance.get(this.data.getAnalogAttractorChange().getAttractionPointId());
		if(dpvo != null)
			return dpvo.getXid();
		return "";
	}

	@JsonSetter("attractionPointXid")
	public void setAttractionPointXid(String attractionPointXid) {
		DataPointVO dpvo = DataPointDao.instance.getByXid(attractionPointXid);
		if(dpvo != null)
			this.data.getAnalogAttractorChange().setAttractionPointId(dpvo.getId());
	}



}
