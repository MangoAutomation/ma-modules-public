package com.serotonin.m2m2.envcan;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=EnvCanPointLocatorModelDefinition.TYPE_NAME)
public class EnvCanPointLocatorModel extends PointLocatorModel<EnvCanPointLocatorVO>{

	private EnvCanPointLocatorVO data;
	
	public EnvCanPointLocatorModel(EnvCanPointLocatorVO data) {
		super(data);
	}
	
	public EnvCanPointLocatorModel() {
		super(new EnvCanPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return EnvCanPointLocatorModelDefinition.TYPE_NAME;
	}

	@JsonGetter("attributeId")
	@CSVColumnGetter(order=10, header="attributeId")
	public String getAttributeId() {
	    return EnvCanPointLocatorVO.ATTRIBUTE_CODES.getCode(this.data.getAttributeId());
	}

	@JsonSetter("attributeId")
	@CSVColumnSetter(order=10, header="attributeId")
	public void setAttributeId(String attributeId) {
	    this.data.setAttributeId(EnvCanPointLocatorVO.ATTRIBUTE_CODES.getId(attributeId));
	}


}
