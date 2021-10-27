package com.infiniteautomation.serial;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class SerialDataSourceDefinition extends DataSourceDefinition<SerialDataSourceVO> {

    public static final String DATA_SOURCE_TYPE = "SERIAL";

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.serial.desc";
    }

    @Override
    protected SerialDataSourceVO createDataSourceVO() {
        return new SerialDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, SerialDataSourceVO ds) {
        if (StringUtils.isBlank(ds.getCommPortId()))
            response.addContextualMessage("commPortId", "validate.required");
        if (ds.getBaudRate() <= 0)
            response.addContextualMessage("baudRate", "validate.invalidValue");
        if (ds.getFlowControlIn() == null)
            response.addContextualMessage("flowControlIn", "validate.required");
        if (ds.getFlowControlOut() == null)
            response.addContextualMessage("flowControlOut", "validate.required");
        if (ds.getDataBits() == null)
            response.addContextualMessage("dataBits", "validate.required");
        if (ds.getStopBits() == null)
            response.addContextualMessage("stopBits", "validate.required");
        if (ds.getParity() == null)
            response.addContextualMessage("parity", "validate.required");

        if(ds.getUseTerminator()) {
            if(ds.getMessageTerminator().length() <= 0)
                response.addContextualMessage("messageTerminator", "validate.required");
            if (StringUtils.isBlank(ds.getMessageRegex()))
                response.addContextualMessage("messageRegex", "validate.required");
            if(ds.getPointIdentifierIndex() < 0)
                response.addContextualMessage("pointIdentifierIndex", "validate.invalidValue");

            if(ds.isHex()){
                if(!ds.getMessageTerminator().matches("[0-9A-Fa-f]+")){
                    response.addContextualMessage("messageTerminator", "serial.validate.notHex");
                }
            }

        }

        if(ds.getReadTimeout() <= 0)
            response.addContextualMessage("readTimeout","validate.greaterThanZero");

        if(ds.getMaxMessageSize() <= 0){
            response.addContextualMessage("maxMessageSize","validate.greaterThanZero");
        }

        if (ds.getIoLogFileSizeMBytes() <= 0)
            response.addContextualMessage("ioLogFileSizeMBytes", "validate.greaterThanZero");
        if (ds.getMaxHistoricalIOLogs() <= 0)
            response.addContextualMessage("maxHistoricalIOLogs", "validate.greaterThanZero");

        if(ds.getRetries() < 0)
            response.addContextualMessage("retries", "validate.cannotBeNegative");
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo) {
        if (!(dsvo instanceof SerialDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");

        SerialPointLocatorVO pl = dpvo.getPointLocator();

        if (pl.getPointIdentifier() == null)
            response.addContextualMessage("pointIdentifier", "validate.invalidValue");

        if (SerialDataSourceVO.isBlank(pl.getValueRegex()))
            response.addContextualMessage("valueRegex", "validate.required");
        try {
            Pattern.compile(pl.getValueRegex()).matcher("").find(); // Validate the regex
        } catch (PatternSyntaxException e) {
            response.addContextualMessage("valueRegex", "serial.validate.badRegex", e.getMessage());
        }

        if(pl.getValueIndex() < 0)
            response.addContextualMessage("valueIndex","validate.invalidValue");

        if (!DataTypes.CODES.isValidId(pl.getDataTypeId()))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");

    }

}
