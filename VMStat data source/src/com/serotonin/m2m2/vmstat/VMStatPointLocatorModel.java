package com.serotonin.m2m2.vmstat;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=VMStatPointLocatorModelDefinition.TYPE_NAME)
public class VMStatPointLocatorModel extends PointLocatorModel<VMStatPointLocatorVO> {

	public VMStatPointLocatorModel(VMStatPointLocatorVO data) {
		super(data);
	}
	
	public VMStatPointLocatorModel() {
		super(new VMStatPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return VMStatPointLocatorModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("attributeId")
	@CSVColumnGetter(order=10, header="attributeId")
	public String getAttributeId() {
	    return VMStatPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attributeId")
	@CSVColumnSetter(order=10, header="attributeId")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(VMStatPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}
}
