package com.infiniteautomation.rq;


public class Message {

	//Request Parameters
	private static char start = '!';
	protected NodeAddress address;
	protected char command; 
	protected MessageData data; //Optional
	private char terminator = ';'; //Potentially this is configurable between CR and ;
	

	
	/**
	 * Create a message out of its parts for transmission
	 * @param address
	 * @param command
	 * @param data
	 */
	public Message(NodeAddress address, char command, MessageData data){
		this.address = address;
		this.command = command;
		this.data = data;
	}
	
	/**
	 * Return an array of Chars representing the message
	 * @return
	 */
	public byte[] getByteArray(){
		
		int len = 3 + address.getChars().length + data.getChars().length;
		
		byte[] bytes = new byte[len];
		bytes[0] = (byte)start;
		int i=1;
		for(char add : address.getChars())
			bytes[i++] = (byte)add;
		bytes[i++] = (byte)command;
		for(char dat : data.getChars())
			bytes[i++] = (byte)dat;
		bytes[i++] = (byte)terminator;
		
		return bytes;
	}
	


	public boolean hasData() {
		if(this.data.getChars().length > 0)
			return true;
		else
			return false;
	}

	/**
	 * Validate the message
	 */
	public boolean validate() {
		return this.address.validate()&&this.data.validate();
		
	}

	/**
	 * Get the message data
	 * @return
	 */
	public MessageData getData() {
		return this.data;
	}

	public char getStartChar() {
		return start;
	}
	/**
	 * Get the end of data signal for this message
	 * @return
	 */
	public char getTerminator(){
		return this.terminator;
	}

	/**
	 * Get the Commmand
	 * @return
	 */
	public char getCommand() {
		return this.command;
	}
}
