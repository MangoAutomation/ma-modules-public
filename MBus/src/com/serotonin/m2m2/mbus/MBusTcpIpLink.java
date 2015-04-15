package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.net.Socket;

import net.sf.mbus4j.Connection;

/**
 * Class to link 2 Tcp Ports together for testing MBUS
 * @author Terry Packer
 *
 */
public class MBusTcpIpLink extends Connection{
    static final String TCP_IP_LINK = "tcpIpLinkConnection";
    public static final int DEFAULT_RESPONSE_TIMEOUT_OFFSET = 600;

    private Socket in;
    
	public MBusTcpIpLink(Socket in){
		super(Connection.DEFAULT_BAUDRATE, DEFAULT_RESPONSE_TIMEOUT_OFFSET);
		this.in = in;
	}
	
	
	@Override
	public void close() throws IOException {
        setConnState(State.CLOSING);
        try {
            this.in.close();
        } finally {
            setConnState(State.CLOSED);
        }
	}

	@Override
	public void open() throws IOException {
        setConnState(State.OPENING);
		is = this.in.getInputStream();
		os = this.in.getOutputStream();
        setConnState(State.OPEN);
	}

	@Override
	public String getJsonFieldName() {
		return TCP_IP_LINK;
	}

}
