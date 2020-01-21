package com.infiniteautomation.asciifile;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.serotonin.m2m2.module.DataSourceDefinition;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileDataSourceDefinition extends DataSourceDefinition<AsciiFileDataSourceVO>{

    public static final String DATA_SOURCE_TYPE = "ASCII FILE";

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.file.desc";
    }

    @Override
    protected AsciiFileDataSourceVO createDataSourceVO() {
        return new AsciiFileDataSourceVO();
    }
}
