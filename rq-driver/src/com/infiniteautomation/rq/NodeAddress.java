package com.infiniteautomation.rq;

public class NodeAddress {
	private char[] address;

	public NodeAddress(String addr){
		this.address = addr.toCharArray();
		
	}
	public NodeAddress(char[] addr){
		this.address = addr;
	}
	
	/**
	 * Is this address a valid one?
	 * @return
	 */
	public boolean validate(){
		if((this.address != null)&&(this.address.length == 2)){
			//TODO Check each char for the range 0-9, A-Z
			return true;
		}else
			return false;
	}

	
	/**
	 * Return the chars that represent this address
	 * @return
	 */
	public char[] getChars() {
		return this.address;
	}
	
	
	
}
