package com.serotonin.m2m2.virtual.vo.model;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class NoChangeModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.VIRTUAL.NO_CHANGE";
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
		return new NoChangeModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return NoChangeModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return NoChangeModel.class;
	}

}
