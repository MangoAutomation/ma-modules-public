/**
 * Copyright (C) 2013 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.SinusoidalChangeRT;

/**
 * @author Terry Packer
 * 
 */
public class SinusoidalChangeVO extends ChangeTypeVO {
	public static final TranslatableMessage KEY = new TranslatableMessage(
			"dsEdit.virtual.changeType.sinusoidal");

	@JsonProperty
	private double amplitude;
	@JsonProperty
	private double offset;
	@JsonProperty
	private double period;
	@JsonProperty
	private double phaseShift;
	
	@Override
	public int typeId() {
		return Types.SINUSOIDAL;
	}

	@Override
	public TranslatableMessage getDescription() {
		return KEY;
	}

	@Override
	public ChangeTypeRT createRuntime() {
		return new SinusoidalChangeRT(this);
	}

	public double getAmplitude() {
		return amplitude;
	}
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}
    public double getOffset() {
		return offset;
	}
	public void setOffset(double offset) {
		this.offset = offset;
	}
	public double getPeriod() {
		return period;
	}
	public void setPeriod(double period) {
		this.period = period;
	}
	public double getPhaseShift() {
		return phaseShift;
	}
	public void setPhaseShift(double phaseShift) {
		this.phaseShift = phaseShift;
	}

	@Override
    public void addProperties(List<TranslatableMessage> list) {
        super.addProperties(list);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.amplitude", amplitude);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.offset", offset);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.period", period);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.phaseShift", phaseShift);
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
        super.addPropertyChanges(list, o);
        SinusoidalChangeVO from = (SinusoidalChangeVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.amplitude", from.amplitude, amplitude);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.offset", from.offset, offset);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.period", from.period, period);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.phaseShift", from.phaseShift, phaseShift);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeDouble(amplitude);
        out.writeDouble(offset);
        out.writeDouble(period);
        out.writeDouble(phaseShift);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            amplitude = in.readDouble();
            offset = in.readDouble();
            period = in.readDouble();
            phaseShift = in.readDouble();
        }
    }

}
