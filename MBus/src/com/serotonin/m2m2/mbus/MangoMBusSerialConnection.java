/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;

import com.infiniteautomation.mango.io.serial.DataBits;
import com.infiniteautomation.mango.io.serial.FlowControl;
import com.infiniteautomation.mango.io.serial.Parity;
import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.StopBits;
import com.serotonin.m2m2.Common;

import net.sf.mbus4j.Connection;

public class MangoMBusSerialConnection extends Connection {

    static final String MANGO_SERIAL_CONNECTION = "mangoSerialConnection";
    private SerialPortProxy serialPort;

    private final String ownerName;
    private final String commPortId;
    private final int baudRate;

    public MangoMBusSerialConnection(String ownerName, String commPortId, int baudRate, int responseTimeoutOffset) {
        super(baudRate, responseTimeoutOffset);

        this.ownerName = ownerName;
        this.commPortId = commPortId;
        this.baudRate = baudRate;
    }

    @Override
    public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            Common.serialPortManager.close(serialPort);
        } catch (SerialPortException e) {
            throw new IOException(e);
        } finally {
            setConnState(State.CLOSED);
        }
    }

    @Override
    public void open() throws IOException {
        try {
            setConnState(State.OPENING);
            this.serialPort = Common.serialPortManager.open(
                    ownerName,
                    commPortId,
                    baudRate,
                    FlowControl.NONE,
                    FlowControl.NONE,
                    DataBits.DATA_BITS_8,
                    StopBits.STOP_BITS_1,
                    Parity.EVEN
            );
            this.is = this.serialPort.getInputStream();
            this.os = this.serialPort.getOutputStream();
            setConnState(State.OPEN);
        } catch (SerialPortException e) {
            close();
            throw new IOException(e);
        }
    }

    @Override
    public String getJsonFieldName() {
        return MANGO_SERIAL_CONNECTION;
    }

    @Override
    public String getName() {
        return commPortId;
    }
}
