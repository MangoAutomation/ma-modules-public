package com.infiniteautomation.rq.motor;

import java.util.Arrays;

import com.infiniteautomation.rq.Message;
import com.infiniteautomation.rq.MessageData;
import com.infiniteautomation.rq.NodeAddress;
import com.infiniteautomation.rq.Transmission;

public class MotorRequestPosition2 extends Transmission{

	/**
	 * Create a request message
	 * @param address
	 */
	public MotorRequestPosition2(String address) {
		super(address);
	}

	@Override
	protected Message createRequestMessage(NodeAddress address) {
		return new Message(address,'r',new MessageData(new char[]{'?'}));
	}



	@Override
	public boolean isBroadcast() {
		return false;
	}

	@Override
	protected MessageData getMessageData(char[] buffer) {
		//Filter out the bytes
		char[] dataBytes = Arrays.copyOfRange(buffer,4,5);
		
		MotorRequestPosition2Data data = new MotorRequestPosition2Data(dataBytes);
		return data;
	}


}
