package com.serotonin.m2m2.mbus;

import java.io.IOException;

import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.serotonin.m2m2.Common;

import net.sf.mbus4j.Connection;

public class MangoMBusSerialConnection extends Connection{
	
    static final String MANGO_SERIAL_CONNECTION = "mangoSerialConnection";
	private SerialPortProxy serialPort;
	
	private String ownerName;
	private String commPortId;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private int dataBits;
    private int stopBits;
    private int parity;
	
	public MangoMBusSerialConnection(String ownerName, String commPortId, int baudRate, int flowControlIn,
			int flowControlOut, int dataBits, int stopBits, int parity, int responseTimeoutOffset){
		super(baudRate, responseTimeoutOffset);
		
		this.ownerName = ownerName;
		this.commPortId = commPortId;
        this.baudRate = baudRate;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
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
		try{
            setConnState(State.OPENING);
            this.serialPort = Common.serialPortManager.open(
            		ownerName,
            		commPortId,
            		baudRate,
            		flowControlIn,
            		flowControlOut,
            		dataBits,
            		stopBits,
            		parity
            		);
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
