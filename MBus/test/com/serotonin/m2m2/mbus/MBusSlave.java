/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;

import net.sf.mbus4j.TcpIpConnection;
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
		
		String host = "localhost";
		int port = 8100;
		TcpIpConnection connection = new TcpIpConnection(host,port);
		
		slaves.setConnection(connection);
        slaves.open();
        
		Slave slave = new Slave(0x01, 12345678, "AMK", 0, MBusMedium.OTHER);
        slaves.addSlave(slave);
        slave = new Slave(0x0F, 01234567, "AMK", 0, MBusMedium.OTHER);
        slaves.addSlave(slave);
        slave = new Slave(0x10, 00123456, "AMK", 0, MBusMedium.OTHER);
        slaves.addSlave(slave);
        slave = new Slave(0xFA, 00012345, "AMK", 0, MBusMedium.OTHER);
        slaves.addSlave(slave);
        
        while(true){
        	Thread.sleep(1000);
        }

		
	}

}
