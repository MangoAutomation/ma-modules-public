package com.infiniteautomation.rq;

import java.util.ArrayList;

public class NodeAddress extends ErrorContainer{
	private char[] address;

	public NodeAddress(String addr){
		super(new ArrayList<ErrorCode>());
		this.address = addr.toCharArray();
		
	}
	
	public NodeAddress(char[] addr){
		super(new ArrayList<ErrorCode>());
		this.address = addr;
		this.validate();
	}
	
	public NodeAddress(ErrorCode code){
		super(code);
		this.address = new char[0];
		this.validate();
	}
	
	/**
	 * Is this address a valid one?
	 * @return
	 */
	private void validate(){
		if((this.address != null)){
			if(this.address.length == 2){
				//TODO Check each char for the range 0-9, A-Z
				//this.addError(ErrorCode.INVALID_ADDRESS_CHAR);
			}else{
				this.addError(ErrorCode.ADDRESS_LENGTH_INVALID);
			}
		}else{
			this.addError(ErrorCode.ADDRESS_MISSING);
		}
	}

	
	/**
	 * Return the chars that represent this address
	 * @return
	 */
	public char[] getChars() {
		return this.address;
	}
	
	
	
}
