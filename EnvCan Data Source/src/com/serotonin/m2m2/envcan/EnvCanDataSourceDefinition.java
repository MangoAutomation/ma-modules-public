package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

public class EnvCanDataSourceDefinition extends DataSourceDefinition {
    public static final String DATA_SOURCE_TYPE = "EnvCan";
	
	@Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "envcands.desc";
    }

    @Override
    protected DataSourceVO<?> createDataSourceVO() {
        return new EnvCanDataSourceVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editEnvCan.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return EnvCanEditDwr.class;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.DataSourceDefinition#getModelClass()
	 */
	@Override
	public Class<? extends AbstractDataSourceModel<?>> getModelClass() {
		return EnvCanDataSourceModel.class;
	}
}
