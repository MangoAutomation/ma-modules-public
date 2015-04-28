package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity(typeName=RandomBooleanChangeModelDefinition.TYPE_NAME)
public class RandomBooleanChangeModel extends VirtualPointLocatorModel {
	
	public RandomBooleanChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.RANDOM_BOOLEAN);
	}
	
	public RandomBooleanChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.RANDOM_BOOLEAN);
	}
	@Override
	public String getTypeName() {
		return RandomBooleanChangeModelDefinition.TYPE_NAME;
	}

}
