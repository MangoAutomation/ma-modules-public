package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=InternalPointLocatorModelDefinition.TYPE_NAME)
public class InternalPointLocatorModel extends PointLocatorModel<InternalPointLocatorVO> {

	private InternalPointLocatorVO data;
	public InternalPointLocatorModel(InternalPointLocatorVO data) {
		super(data);
		this.data = data;
	}
	
	public InternalPointLocatorModel() {
		super(new InternalPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return InternalPointLocatorModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("attributeId")
	@CSVColumnGetter(order=10, header="attributeId")
	public String getAttributeId() {
	    return InternalPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attributeId")
	@CSVColumnSetter(order=10, header="attributeId")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(InternalPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}

}
