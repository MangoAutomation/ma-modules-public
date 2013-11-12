package com.infiniteautomation.rq;

import gnu.io.CommPort;

import java.io.IOException;
import java.util.List;

import com.infiniteautomation.rq.motor.MotorRequestPosition2;
import com.infiniteautomation.rq.motor.MotorRequestPosition2Data;

public class RQDriver {
	
	private CommPort port;
	
	public void demoCall(){
		Transmission t = new MotorRequestPosition2("A3");
		this.request(t);
				
		//Do we have a valid message and valid data.
		if(t.getResponse().hasErrors()||t.getRequest().hasErrors()){
			//Capture errors
			List<ErrorCode> errors = t.getRequest().getErrors();
			for(ErrorCode error : errors)
				System.out.println("Request Error Code: " + error);

			errors = t.getResponse().getErrors();
			for(ErrorCode error : errors)
				System.out.println("Response Error Code: " + error);

		}else{
			System.out.println("Data Recieved: " + ((MotorRequestPosition2Data)t.getResponse().getData()).getMotorPosition());
		}
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
