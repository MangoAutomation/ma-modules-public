package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class IncrementAnalogChangeModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.VIRTUAL.INCREMENT_ANALOG";
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
		return new IncrementAnalogChangeModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return IncrementAnalogChangeModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return IncrementAnalogChangeModel.class;
	}

}
