/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;

/**
 * @author Matthew Lohbihler
 */
public class VMStatDataSourceVO extends DataSourceVO<VMStatDataSourceVO> {
    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(VMStatDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource"), DuplicateHandling.IGNORE_SAME_MESSAGE, AlarmLevels.URGENT));
        ets.add(createEventType(VMStatDataSourceRT.PARSE_EXCEPTION_EVENT, new TranslatableMessage("event.ds.dataParse")));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(VMStatDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(VMStatDataSourceRT.PARSE_EXCEPTION_EVENT, "PARSE_EXCEPTION");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    public interface OutputScale {
        int NONE = 1;
        int LOWER_K = 2;
        int UPPER_K = 3;
        int LOWER_M = 4;
        int UPPER_M = 5;
    }

    public static final ExportCodes OUTPUT_SCALE_CODES = new ExportCodes();
    static {
        OUTPUT_SCALE_CODES.addElement(VMStatDataSourceVO.OutputScale.NONE, "NONE", "dsEdit.vmstat.scale.none");
        OUTPUT_SCALE_CODES.addElement(VMStatDataSourceVO.OutputScale.LOWER_K, "LOWER_K", "dsEdit.vmstat.scale.k");
        OUTPUT_SCALE_CODES.addElement(VMStatDataSourceVO.OutputScale.UPPER_K, "UPPER_K", "dsEdit.vmstat.scale.K");
        OUTPUT_SCALE_CODES.addElement(VMStatDataSourceVO.OutputScale.LOWER_M, "LOWER_M", "dsEdit.vmstat.scale.m");
        OUTPUT_SCALE_CODES.addElement(VMStatDataSourceVO.OutputScale.UPPER_M, "UPPER_M", "dsEdit.vmstat.scale.M");
    }

    @Override
    public TranslatableMessage getConnectionDescription() {
        return new TranslatableMessage("dsEdit.vmstat.dsconn", pollSeconds);
    }

    @Override
    public VMStatDataSourceRT createDataSourceRT() {
        return new VMStatDataSourceRT(this);
    }

    @Override
    public VMStatPointLocatorVO createPointLocator() {
        return new VMStatPointLocatorVO();
    }

    @JsonProperty
    private int pollSeconds = 60;
    private int outputScale = OutputScale.NONE;

    public int getPollSeconds() {
        return pollSeconds;
    }

    public void setPollSeconds(int pollSeconds) {
        this.pollSeconds = pollSeconds;
    }

    public int getOutputScale() {
        return outputScale;
    }

    public void setOutputScale(int outputScale) {
        this.outputScale = outputScale;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (pollSeconds < 1)
            response.addContextualMessage("pollSeconds", "validate.greaterThanZero", pollSeconds);

        if (!OUTPUT_SCALE_CODES.isValidId(outputScale))
            response.addContextualMessage("outputScale", "validate.invalidValue");
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(pollSeconds);
        out.writeInt(outputScale);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pollSeconds = in.readInt();
            outputScale = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("outputScale", OUTPUT_SCALE_CODES.getCode(outputScale));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        String text = jsonObject.getString("outputScale");
        if (text != null) {
            outputScale = OUTPUT_SCALE_CODES.getId(text);
            if (outputScale == -1)
                throw new TranslatableJsonException("emport.error.invalid", "outputScale", text,
                        OUTPUT_SCALE_CODES.getCodeList());
        }
    }
}
