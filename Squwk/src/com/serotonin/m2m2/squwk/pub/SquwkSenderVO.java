package com.serotonin.m2m2.squwk.pub;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.publish.PublisherRT;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;
import com.serotonin.util.SerializationHelper;

public class SquwkSenderVO extends PublisherVO<SquwkPointVO> {
    @Override
    protected void getEventTypesImpl(List<EventTypeVO> eventTypes) {
        eventTypes.add(new EventTypeVO(EventType.EventTypeNames.PUBLISHER, null, getId(),
                SquwkSenderRT.REQUEST_EXCEPTION_EVENT, new TranslatableMessage("event.pb.squwk.request"),
                getAlarmLevel(SquwkSenderRT.REQUEST_EXCEPTION_EVENT, AlarmLevels.URGENT)));
        eventTypes.add(new EventTypeVO(EventType.EventTypeNames.PUBLISHER, null, getId(),
                SquwkSenderRT.SERVICE_EXCEPTION_EVENT, new TranslatableMessage("event.pb.squwk.service"),
                getAlarmLevel(SquwkSenderRT.SERVICE_EXCEPTION_EVENT, AlarmLevels.URGENT)));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        PublisherVO.addDefaultEventCodes(EVENT_CODES);
        EVENT_CODES.addElement(SquwkSenderRT.REQUEST_EXCEPTION_EVENT, "REQUEST_EXCEPTION_EVENT");
        EVENT_CODES.addElement(SquwkSenderRT.SERVICE_EXCEPTION_EVENT, "SERVICE_EXCEPTION_EVENT");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public TranslatableMessage getConfigDescription() {
        return new TranslatableMessage("common.noMessage");
    }

    @Override
    public PublisherRT<SquwkPointVO> createPublisherRT() {
        return new SquwkSenderRT(this);
    }

    @Override
    protected SquwkPointVO createPublishedPointInstance() {
        return new SquwkPointVO();
    }

    @JsonProperty
    private String accessKey;
    @JsonProperty
    private String secretKey;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (StringUtils.isBlank(accessKey))
            response.addContextualMessage("accessKey", "validate.required");
        if (StringUtils.isBlank(secretKey))
            response.addContextualMessage("secretKey", "validate.required");

        for (SquwkPointVO point : points) {
            if (StringUtils.isBlank(point.getGuid())) {
                response.addContextualMessage("points", "validate.squwk.guidRequired");
                break;
            }
        }
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, accessKey);
        SerializationHelper.writeSafeUTF(out, secretKey);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            accessKey = SerializationHelper.readSafeUTF(in);
            secretKey = SerializationHelper.readSafeUTF(in);
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.publish.PublisherVO#asModel()
	 */
	@Override
	public AbstractPublisherModel<?, ?> asModel() {
		// TODO Auto-generated method stub
		return null;
	}
}
