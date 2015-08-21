/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortIdentifier;
import com.infiniteautomation.mango.io.serial.SerialPortInputStream;
import com.infiniteautomation.mango.io.serial.SerialPortOutputStream;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfig.SerialPortTypes;

/**
 * @author Terry Packer
 *
 */
public class TestSerialPortProxy extends SerialPortProxy{

	TestSerialPortInputStream inputStream;
	TestSerialPortOutputStream outputStream;
	
	/**
	 * @param name
	 */
	public TestSerialPortProxy(String name, TestSerialPortInputStream input, TestSerialPortOutputStream output) {
		super(new SerialPortIdentifier("test", SerialPortTypes.JSSC));
		this.inputStream = input;
		this.outputStream = output;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#readBytes(int)
	 */
	@Override
	public byte[] readBytes(int i) throws SerialPortException {
		return new byte[i];
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#writeInt(int)
	 */
	@Override
	public void writeInt(int arg0) throws SerialPortException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#closeImpl()
	 */
	@Override
	public void closeImpl() throws SerialPortException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#openImpl()
	 */
	@Override
	public void openImpl() throws SerialPortException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#getInputStream()
	 */
	@Override
	public SerialPortInputStream getInputStream() {
		return inputStream;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortProxy#getOutputStream()
	 */
	@Override
	public SerialPortOutputStream getOutputStream() {
		return outputStream;
	}

}
