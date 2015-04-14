package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class InternalPointLocatorModelDefinition extends ModelDefinition{

	public static final String TYPE_NAME = "PL.INTERNAL";
	@Override
	public String getModelKey() {
		return "";//TODO
	}

	@Override
	public String getModelTypeName() {
		return TYPE_NAME;
	}

	@Override
	public AbstractRestModel<?> createModel() {
		return new InternalPointLocatorModel(new InternalPointLocatorVO());
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return InternalPointLocatorModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return InternalPointLocatorModel.class;
	}

}
