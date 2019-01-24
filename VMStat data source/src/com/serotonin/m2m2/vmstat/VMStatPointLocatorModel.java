package com.serotonin.m2m2.vmstat;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=VMStatPointLocatorModel.TYPE_NAME)
public class VMStatPointLocatorModel extends PointLocatorModel<VMStatPointLocatorVO> {

    public static final String TYPE_NAME = "PL.VMSTAT";
    
	public VMStatPointLocatorModel(VMStatPointLocatorVO data) {
		super(data);
	}
	
	public VMStatPointLocatorModel() {
		super(new VMStatPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	@JsonGetter("attribute")
	@CSVColumnGetter(order=19, header="attribute")
	public String getAttributeId() {
	    return VMStatPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attribute")
	@CSVColumnSetter(order=19, header="attribute")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(VMStatPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}
}
