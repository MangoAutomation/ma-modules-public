/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.serial;

import java.io.IOException;

import com.infiniteautomation.mango.io.serial.SerialPortInputStream;
import com.serotonin.util.queue.ByteQueue;

/**
 * @author Terry Packer
 *
 */
public class TestSerialPortInputStream extends SerialPortInputStream{
	ByteQueue mockStream;

	@Override
	public int read() throws IOException {
		if(mockStream.size() <= 0)
			return -1;
		return mockStream.pop();
	}

	@Override
	public int available() throws IOException {
		return mockStream.size();
	}

	@Override
	public void closeImpl() throws IOException {
		mockStream.popAll();
	}

	
	public void pushToMockStream(String str) {
		mockStream = new ByteQueue(1024);
		mockStream.push(str.getBytes());
	}

}
