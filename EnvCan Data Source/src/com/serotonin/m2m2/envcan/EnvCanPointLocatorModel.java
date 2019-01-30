package com.serotonin.m2m2.envcan;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=EnvCanPointLocatorModel.TYPE_NAME)
public class EnvCanPointLocatorModel extends PointLocatorModel<EnvCanPointLocatorVO>{

    public static final String TYPE_NAME = "PL.ENV_CAN";
    
	public EnvCanPointLocatorModel(EnvCanPointLocatorVO data) {
		super(data);
	}
	
	public EnvCanPointLocatorModel() {
		super(new EnvCanPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@JsonGetter("attribute")
	@CSVColumnGetter(order=18, header="attribute")
	public String getAttributeId() {
	    return EnvCanPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attribute")
	@CSVColumnSetter(order=18, header="attribute")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(EnvCanPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}


}
