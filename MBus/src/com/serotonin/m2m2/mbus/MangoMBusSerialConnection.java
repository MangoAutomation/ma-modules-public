package com.serotonin.m2m2.mbus;

import java.io.IOException;

import net.sf.mbus4j.Connection;

import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialUtils;

public class MangoMBusSerialConnection extends Connection{
	
    static final String MANGO_SERIAL_CONNECTION = "mangoSerialConnection";
	private SerialPortProxy serialPort;
	private SerialParameters parameters;
	
	
	public MangoMBusSerialConnection(SerialParameters parameters, int responseTimeoutOffset){
		super(parameters.getBaudRate(), responseTimeoutOffset);
		this.parameters = parameters;
	}
	
	
	@Override
	public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            SerialUtils.close(serialPort);
        } catch (SerialPortException e) {
			throw new IOException(e);
		} finally {
            setConnState(State.CLOSED);
        }
	}

	@Override
	public void open() throws IOException {
		try{
            setConnState(State.OPENING);
            this.serialPort = SerialUtils.openSerialPort(this.parameters);
			this.is = this.serialPort.getInputStream();
			this.os = this.serialPort.getOutputStream();
            setConnState(State.OPEN);
		}catch(SerialPortException e){
			close();
			throw new IOException(e);
		}
	}

	@Override
	public String getJsonFieldName() {
		return MANGO_SERIAL_CONNECTION;
	}
}
