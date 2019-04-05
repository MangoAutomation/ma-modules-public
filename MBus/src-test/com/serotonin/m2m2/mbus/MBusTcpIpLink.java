/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.net.Socket;

import net.sf.mbus4j.Connection;

/**
 * Class to link 2 Tcp Ports together for testing MBUS
 * @author Terry Packer
 *
 */
public class MBusTcpIpLink extends Connection {
    static final String TCP_IP_LINK = "tcpIpLinkConnection";
    public static final int DEFAULT_RESPONSE_TIMEOUT_OFFSET = 600;

    private final Socket socket;
    private InputStreamWrapper isWrapper;
    
	public MBusTcpIpLink(Socket socket){
		super(Connection.DEFAULT_BAUDRATE, DEFAULT_RESPONSE_TIMEOUT_OFFSET);
		this.socket = socket;
	}
	
	
	@Override
	public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            this.socket.close();
        } finally {
            setConnState(State.CLOSED);
        }
	}

	@Override
	public void open() throws IOException {
        setConnState(State.OPENING);
        this.isWrapper = new InputStreamWrapper(socket.getInputStream());
		is = this.isWrapper;
		os = this.socket.getOutputStream();
        setConnState(State.OPEN);
        
	}
	
	public boolean isConnected() {
	    return this.socket.isClosed();
	}

	@Override
	public String getJsonFieldName() {
		return TCP_IP_LINK;
	}

	public boolean isDisconnected() {
	    return this.isWrapper.isDisconnected();
	}

	/* (non-Javadoc)
	 * @see net.sf.mbus4j.Connection#getName()
	 */
	@Override
	public String getName() {
		return this.socket.getInetAddress().getHostAddress();
	}

}