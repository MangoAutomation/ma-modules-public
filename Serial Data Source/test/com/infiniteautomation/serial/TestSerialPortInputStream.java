/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import java.io.IOException;

import com.serotonin.io.serial.SerialPortInputStream;
import com.serotonin.util.queue.ByteQueue;

/**
 * @author Terry Packer
 *
 */
public class TestSerialPortInputStream extends SerialPortInputStream{
	ByteQueue mockStream;
	
	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if(mockStream.size() <= 0)
			return -1;
		return mockStream.pop();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return mockStream.size();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#closeImpl()
	 */
	@Override
	public void closeImpl() throws IOException {
		mockStream.popAll();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.io.serial.SerialPortInputStream#peek()
	 */
	@Override
	public int peek() {
		if(mockStream.size() <= 0)
			return -1;
		return mockStream.peek(0);
	}
	
	public void pushToMockStream(String str) {
		mockStream = new ByteQueue(1024);
		mockStream.push(str.getBytes());
	}

}
