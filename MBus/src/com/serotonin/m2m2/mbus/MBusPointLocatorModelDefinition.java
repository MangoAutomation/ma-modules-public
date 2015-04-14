package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class MBusPointLocatorModelDefinition extends ModelDefinition{

	public static final String TYPE_NAME = "PL.MBUS";
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
		return new MBusPointLocatorModel(new MBusPointLocatorVO());
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return MBusPointLocatorModel.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModelDefinition#getModelClass()
	 */
	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return MBusPointLocatorModel.class;
	}

}
