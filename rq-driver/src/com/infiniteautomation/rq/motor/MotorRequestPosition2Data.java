package com.infiniteautomation.rq.motor;

import com.infiniteautomation.rq.MessageData;

public class MotorRequestPosition2Data extends MessageData{

	public int motorPosition;
	
	public MotorRequestPosition2Data(char[] dataBytes) {
		super(dataBytes);
		
		//Now pull the data out of the array
		if(this.dataArray.length != 2){
			this.dataOk = false;
			return;
		}
		
		try{
			this.motorPosition = Integer.parseInt(String.valueOf(dataBytes));
		}catch(Exception e){
			this.messages.add(e.getMessage());
			this.dataOk = false;
		}
	}

}
