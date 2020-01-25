package com.infiniteautomation.asciifile.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.infiniteautomation.asciifile.rt.AsciiFileDataSourceRT;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.PollingDataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;

/**
 * @author Phillip Dunlap
 */

@JsonEntity
public class AsciiFileDataSourceVO extends PollingDataSourceVO {

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(AsciiFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, "POINT_READ_PATTERN_MISMATCH_EVENT");
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
    }

    @JsonProperty
    private String filePath;

    @Override
    public TranslatableMessage getConnectionDescription() {
        return new TranslatableMessage("file.path",this.filePath);
    }

    @Override
    public AsciiFilePointLocatorVO createPointLocator() {
        return new AsciiFilePointLocatorVO();
    }

    @Override
    public AsciiFileDataSourceRT createDataSourceRT() {
        return new AsciiFileDataSourceRT(this);
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    protected void addEventTypes(List<EventTypeVO> eventTypes) {
        super.addEventTypes(eventTypes);
        eventTypes.add(createEventType(AsciiFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
        eventTypes.add(createEventType(AsciiFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointRead")));
        eventTypes.add(createEventType(AsciiFileDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, new TranslatableMessage(
                "file.event.patternMismatchEventd")));
    }

    @Override
    public int getPollAbortedExceptionEventId() {
        return AsciiFileDataSourceRT.POLL_ABORTED_EVENT;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int getUpdatePeriods() {
        return this.updatePeriods;
    }

    @Override
    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    @Override
    public int getUpdatePeriodType() {
        return this.updatePeriodType;
    }

    @Override
    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, this.filePath);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            this.filePath = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
        }else if(ver == 2) {
            filePath = SerializationHelper.readSafeUTF(in);
        }
    }
}
