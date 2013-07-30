package com.infiniteautomation.rq;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public abstract class Transmission {

	protected Message response;
	protected Message request;
	
	public Transmission(String address){
		this.request = this.createRequestMessage(new NodeAddress(address));
	}

	/**
	 * For subclass to override and implement
	 * @return
	 */
	protected abstract Message createRequestMessage(NodeAddress address);
	
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public Message parseResponseMessage(char[] buffer){
		
		if(buffer == null)
			return null; //TODO Create error message here
		//Confirm start char is correct
		if(buffer[0] != this.request.getStartChar())
			return null; //TODO Create error message here
		//Confirm end char is correct
		if(buffer[buffer.length -1] != this.request.getTerminator())
			return null; //TODO Create error message here
		
		MessageData data = this.getMessageData(buffer);
		
		//Parse Address
		char[] addBuffer = Arrays.copyOfRange(buffer, 1, 3);
		NodeAddress address = new NodeAddress(addBuffer);
		
		Message myMessage = new Message(address, this.request.getCommand(), data);
		
		
		return myMessage;
	}
	
	/**
	 * Create the message data from the response (if any)
	 * @param buffer
	 * @return
	 */
	protected abstract MessageData getMessageData(char[] buffer);
	
	/**
	 * Is this a broadcast message that expects > 1 response
	 * @return
	 */
	public abstract boolean isBroadcast();
	
	
	
	/**
	 * Do the request and response addresses match
	 * @return
	 */
	public boolean addressesMatch(){
		
		if(response.address.getChars().length == request.address.getChars().length){
			for(int i=0; i<response.address.getChars().length; i++)
				if(response.address.getChars()[i] != request.address.getChars()[i])
					return false;
			return true;
		}else{
			return false;
		}
		
	}

	public boolean hasData(){
		return this.response.hasData();
	}
	
	public void transmit(OutputStream os, InputStream in) {
		
		//Validate the request
		this.request.validate();
		
		//Send the request
		try {
			os.write(this.request.getByteArray());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//TODO May need some delay built in...
		
		//We recieved some data, now parse it.
		char[] buffer = new char[1024]; //Max size TBD
		try{

            int len = 0;
            int data;
            
            while (( data = in.read()) > -1 ){
                buffer[len++] = (char)data;
                if ( data == this.response.getTerminator()) {
                    break;
                }
            }
		}catch(Exception e){
			e.printStackTrace();
		}

		//Parse the response
		this.response = this.parseResponseMessage(buffer);
	}

	/**
	 * Return the response
	 * @return
	 */
	public Message getResponse() {
		return this.response;
	}
}
