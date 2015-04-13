package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class EnvCanPointLocatorModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.ENV_CAN";
	@Override
	public String getModelKey() {
		return ""; //TODO
	}

	@Override
	public String getModelTypeName() {
		return TYPE_NAME;
	}

	@Override
	public AbstractRestModel<?> createModel() {
		return new EnvCanPointLocatorModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return EnvCanPointLocatorModel.class.equals(clazz);
	}
}
