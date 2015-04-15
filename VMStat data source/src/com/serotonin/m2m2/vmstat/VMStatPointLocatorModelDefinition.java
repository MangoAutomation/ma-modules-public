package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class VMStatPointLocatorModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.VMSTAT";
	@Override
	public String getModelKey() {
		return "";
	}

	@Override
	public String getModelTypeName() {
		return TYPE_NAME;
	}

	@Override
	public AbstractRestModel<?> createModel() {
		return new VMStatPointLocatorModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return VMStatPointLocatorModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return VMStatPointLocatorModel.class;
	}

}
