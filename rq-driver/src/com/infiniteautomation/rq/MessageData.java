package com.infiniteautomation.rq;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds RQ Message data
 * @author tpacker
 *
 */
public class MessageData {

	protected char[] dataArray;
	protected boolean dataOk = true;
	protected List<String> messages; //Messages to describe bad data.
	
	public MessageData(char[] dataArray){
		this.dataArray = dataArray;
		this.messages = new ArrayList<String>();
	}
	
	public char[] getChars(){
		return this.dataArray;
	}

	/**
	 * To be overridden by subclass if necessary
	 */
	public boolean validate() {
		return this.dataOk;
	}


	
}
