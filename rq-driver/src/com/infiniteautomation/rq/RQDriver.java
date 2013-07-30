package com.infiniteautomation.rq;

import gnu.io.CommPort;

import java.io.IOException;

import com.infiniteautomation.rq.motor.MotorRequestPosition2;
import com.infiniteautomation.rq.motor.MotorRequestPosition2Data;

public class RQDriver {
	
	private CommPort port;
	
	public void demoCall(){
		Transmission t = new MotorRequestPosition2("A3");
		this.request(t);
		
		//Get the data
		MotorRequestPosition2Data d = (MotorRequestPosition2Data) t.getResponse().getData();
	}
	
	
	public void request(Transmission trans){
		//Perform request, response will be store in the transmission.response.data
		try {
			trans.transmit(this.port.getOutputStream(), this.port.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		
		
	}
	
	
	
}
