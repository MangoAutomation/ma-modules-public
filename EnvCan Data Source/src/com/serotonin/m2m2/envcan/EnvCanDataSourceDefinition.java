package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class EnvCanDataSourceDefinition extends DataSourceDefinition {
    @Override
    public String getDataSourceTypeName() {
        return "EnvCan";
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
}
