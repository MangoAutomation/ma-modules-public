package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class RandomAnalogChangeModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.VIRTUAL.RANDOM_ANALOG";
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
		return new RandomAnalogChangeModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return RandomAnalogChangeModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return RandomAnalogChangeModel.class;
	}

}
