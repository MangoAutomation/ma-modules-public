/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.slaves.Slave;
import net.sf.mbus4j.slaves.Slaves;

import org.junit.Test;

/**
 * @author Terry Packer
 *
 */
public class MBusSlave {
	
	private Slaves slaves;
	
	@Test
	public void startSlave() throws IOException, InterruptedException{
		
		slaves = new Slaves();

		int inPort = 8100;

		ServerSocket server = new ServerSocket(inPort);
		Socket in = server.accept();
		
		
		MBusTcpIpLink connection = new MBusTcpIpLink(in);
		
		slaves.setConnection(connection);
		slaves.open();
		
        slaves.addSlave(new Slave(0x01, 14491001, "DBW", 1, MBusMedium.HOT_WATER));
        slaves.addSlave(new Slave(0x01, 14491008, "QKG", 1, MBusMedium.HOT_WATER));
        slaves.addSlave(new Slave(0x01, 32104833, "H@P", 1, MBusMedium.ELECTRICITY));
        slaves.addSlave(new Slave(0x01, 76543210, "H@P", 1, MBusMedium.GAS));
        int ctr = 0;
        while(ctr < 100){
        	Thread.sleep(1000);
        	ctr++;
        }

        server.close();
		
	}

}
