package com.infiniteautomation.asciifile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.PollingDataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileDataSourceDefinition extends PollingDataSourceDefinition<AsciiFileDataSourceVO>{

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

    @Override
    public void validate(ProcessResult response, AsciiFileDataSourceVO vo,
            PermissionHolder holder) {
        super.validate(response, vo, holder);
        //TODO: ensure the path syntax is reasonable
        if (isBlank(vo.getFilePath()))
            response.addContextualMessage("filePath", "validate.required");
        if(!StringUtils.isEmpty(vo.getFilePath())) {
            File file = new File(vo.getFilePath());
            try {
                vo.setFilePath(file.getCanonicalPath());
            } catch(IOException e) {
                response.addContextualMessage("filePath", "dsEdit.file.ioexceptionCanonical", vo.getFilePath());
                return;
            }
            String restrictedPaths = SystemSettingsDao.getInstance().getValue(AsciiFileSystemSettingsDefinition.RESTRICTED_PATH);
            if(!StringUtils.isEmpty(restrictedPaths))
                for(String rPath : restrictedPaths.split(";")) {
                    if(vo.getFilePath().startsWith(rPath))
                        response.addContextualMessage("filePath", "dsEdit.file.pathRestrictedBy", vo.getFilePath());
                }
        }
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo,
            PermissionHolder user) {
        if(!(dsvo instanceof AsciiFileDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");

        AsciiFilePointLocatorVO pl = dpvo.getPointLocator();
        if (isBlank(pl.getValueRegex()))
            response.addContextualMessage("valueRegex", "validate.required");

        //Validate the regex
        if(!Pattern.compile("([^\\\\]|^)\\(.*[^\\\\]*\\)").matcher(pl.getValueRegex()).find())
            response.addContextualMessage("valueRegex", "file.validate.noCaptureGroup");

        if(pl.getPointIdentifierIndex() < 0)
            response.addContextualMessage("pointIdentifierIndex", "validate.invalidValue");

        if(pl.getValueIndex() < 0)
            response.addContextualMessage("valueIndex", "validate.invalidValue");

        if (!DataTypes.CODES.isValidId(pl.getDataTypeId()))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");

        if(pl.getHasTimestamp()) {
            if(pl.getTimestampIndex() < 0)
                response.addContextualMessage("timestampIndex", "validate.invalidValue");
            if(pl.getTimestampFormat() == null || pl.getTimestampFormat().equals(""))
                response.addContextualMessage("timestampFormat", "validate.invalidValue");
            else {
                try {
                    new SimpleDateFormat(pl.getTimestampFormat());
                } catch(IllegalArgumentException e) {
                    response.addContextualMessage("timestampFormat", "file.validate.invalidDateFormat", pl.getTimestampFormat(), e.getMessage());
                }
            }
        }

    }

    private boolean isBlank(CharSequence cs) {
        int strLen;
        if ((cs == null) || ((strLen = cs.length()) == 0))
            return true;

        for (int i = 0; i < strLen; ++i) {
            if (!(Character.isWhitespace(cs.charAt(i)))) {
                return false;
            }
        }
        return true;
    }
}
