package com.serotonin.m2m2.mbus;

import java.io.IOException;

import net.sf.mbus4j.Connection;

import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;

public class MangoMBusSerialConnection extends Connection{
    static final String MANGO_SERIAL_CONNECTION = "mangoSerialConnection";
	private SerialPortProxy serialPort;
	
	public MangoMBusSerialConnection(SerialPortProxy proxy, int responseTimeoutOffset){
		super(proxy.getParameters().getBaudRate(), responseTimeoutOffset);
		this.serialPort = proxy;
		
	}
	
	
	@Override
	public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            this.serialPort.close();
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
			this.serialPort.open();
			is = this.serialPort.getInputStream();
			os = this.serialPort.getOutputStream();
            setConnState(State.OPEN);
		}catch(SerialPortException e){
			throw new IOException(e);
		}
	}

	@Override
	public String getJsonFieldName() {
		return MANGO_SERIAL_CONNECTION;
	}
}
