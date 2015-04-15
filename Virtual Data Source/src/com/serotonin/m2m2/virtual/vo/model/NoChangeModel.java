package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity(typeName=NoChangeModelDefinition.TYPE_NAME)
public class NoChangeModel extends VirtualPointLocatorModel {
	
	public NoChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.NO_CHANGE);
	}
	
	public NoChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.NO_CHANGE);
	}
	@Override
	public String getTypeName() {
		return NoChangeModelDefinition.TYPE_NAME;
	}

}
