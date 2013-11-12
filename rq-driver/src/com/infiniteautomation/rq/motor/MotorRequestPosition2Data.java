package com.infiniteautomation.rq.motor;

import com.infiniteautomation.rq.ErrorCode;
import com.infiniteautomation.rq.MessageData;

public class MotorRequestPosition2Data extends MessageData{

	public int motorPosition;
	
	/**
	 * Create an error message
	 * @param dataBytes
	 */
	public MotorRequestPosition2Data(char[] dataBytes) {
		super(dataBytes);
		
		//Now pull the data out of the array
		if(this.dataArray.length != 2){
			this.addError(ErrorCode.DATA_LENGTH_INVALID);
			return;
		}
		
		try{
			this.motorPosition = Integer.parseInt(String.valueOf(dataBytes));
		}catch(Exception e){
			this.addError(ErrorCode.INTEGER_VALUE_INVALID);
		}
	}
	
	public MotorRequestPosition2Data(ErrorCode code){
		super(code);
		this.motorPosition = -1; //Set to invalid?
	}
	

	/**
	 * Get the motor position
	 * @return
	 */
	public int getMotorPosition() {
		return this.motorPosition;
	}

}
