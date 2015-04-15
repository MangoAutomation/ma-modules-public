package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity(typeName=AlternateBooleanChangeModelDefinition.TYPE_NAME)
public class AlternateBooleanChangeModel extends VirtualPointLocatorModel {
	
	public AlternateBooleanChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.ALTERNATE_BOOLEAN);
	}
	
	public AlternateBooleanChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.ALTERNATE_BOOLEAN);
	}
	@Override
	public String getTypeName() {
		return AlternateBooleanChangeModelDefinition.TYPE_NAME;
	}

}
