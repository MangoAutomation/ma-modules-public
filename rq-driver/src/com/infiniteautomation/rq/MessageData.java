package com.infiniteautomation.rq;

import java.util.ArrayList;


/**
 * Holds RQ Message data
 * @author tpacker
 *
 */
public class MessageData extends ErrorContainer{

	protected char[] dataArray;
	
	/**
	 * Create a message data with data
	 * @param dataArray
	 */
	public MessageData(char[] dataArray){
		super(new ArrayList<ErrorCode>());
		this.dataArray = dataArray;
	}
	
	/**
	 * Create a message data from error
	 * @param dataArray
	 */
	public MessageData(ErrorCode code){
		super(code);
		this.dataArray = new char[0];
	}
	
	
	public char[] getChars(){
		return this.dataArray;
	}

	/**
	 * To be overridden by subclass if necessary
	 */
	public boolean validate() {
		return this.hasErrors();
	}
	
}
