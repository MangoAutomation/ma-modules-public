/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.TranslatableMessage;
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
