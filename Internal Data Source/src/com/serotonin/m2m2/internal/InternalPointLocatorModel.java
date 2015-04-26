package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=InternalPointLocatorModelDefinition.TYPE_NAME)
public class InternalPointLocatorModel extends PointLocatorModel<InternalPointLocatorVO> {

	public InternalPointLocatorModel(InternalPointLocatorVO data) {
		super(data);
	}
	
	public InternalPointLocatorModel() {
		super(new InternalPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return InternalPointLocatorModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("attribute")
	@CSVColumnGetter(order=13, header="attribute")
	public String getAttributeId() {
	    return InternalPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attribute")
	@CSVColumnSetter(order=13, header="attribute")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(InternalPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}

}
