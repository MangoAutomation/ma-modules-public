package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

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
}
