package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.PollingDataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class EnvCanDataSourceDefinition extends PollingDataSourceDefinition<EnvCanDataSourceVO> {
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
    protected EnvCanDataSourceVO createDataSourceVO() {
        return new EnvCanDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, EnvCanDataSourceVO ds) {
        super.validate(response, ds);
        if (ds.getStationId() < 1)
            response.addContextualMessage("stationId", "validate.greaterThanZero", ds.getStationId());
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo) {
        if (!(dsvo instanceof EnvCanDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");

        EnvCanPointLocatorVO pl = dpvo.getPointLocator();

        if (!EnvCanPointLocatorVO.ATTRIBUTE_CODES.isValidId(pl.getAttributeId()))
            response.addContextualMessage("attributeId", "validate.invalidValue");
    }
}
