/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import java.io.IOException;

import com.serotonin.io.serial.SerialPortInputStream;

/**
 * @author Terry Packer
 *
 */
public class TestSerialPortInputStream extends SerialPortInputStream{

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return 10;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#closeImpl()
	 */
	@Override
	public void closeImpl() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#peek()
	 */
	@Override
	public int peek() {
		return 0;
	}

}
