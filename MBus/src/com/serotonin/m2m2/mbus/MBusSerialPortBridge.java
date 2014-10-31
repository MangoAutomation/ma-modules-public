package com.serotonin.m2m2.mbus;

import java.io.IOException;

import net.sf.mbus4j.Connection;

import com.serotonin.io.serial.SerialPortException;
import com.serotonin.m2m2.rt.serial.EthernetComBridge;

public class MBusSerialPortBridge extends Connection{
    static final String SERIAL_BRIDGE_CONNECTION = "serialBridgeConnection";
	private EthernetComBridge bridge;
	
	public MBusSerialPortBridge(EthernetComBridge bridge, int baudRate, int responseTimeoutOffset){
		super(baudRate, responseTimeoutOffset);
		this.bridge = bridge;
		
	}
	
	
	@Override
	public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            this.bridge.close();
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
			this.bridge.open();
			is = this.bridge.getSocketInputStream();
			os = this.bridge.getSocketOutputStream();
            setConnState(State.OPEN);
		}catch(SerialPortException e){
			throw new IOException(e);
		}
	}

	@Override
	public String getJsonFieldName() {
		return SERIAL_BRIDGE_CONNECTION;
	}

}
