package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class SinusoidalChangeModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.VIRTUAL.SINUSOIDAL";
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
		return new SinusoidalChangeModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return SinusoidalChangeModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return SinusoidalChangeModel.class;
	}

}
