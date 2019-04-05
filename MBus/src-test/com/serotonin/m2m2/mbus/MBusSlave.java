/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.slaves.Slave;
import net.sf.mbus4j.slaves.Slaves;

/**
 * 
 * Startup an IP Slave to listen on port 8100
 * 
 * For logging tart this Program with vm arg
 * -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager
 * 
 * @author Terry Packer
 *
 */
public class MBusSlave {

    private static final Log LOG = LogFactory.getLog(MBusSlave.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.info("Starting up");

        //Run forever
        while (true) {

            try (ServerSocket server = new ServerSocket(8100);) {
                Socket socket = server.accept();
                LOG.info("Connection recieved from " + socket.getRemoteSocketAddress());
                // New Connection
                new Thread() {
                    public void run() {

                        Slaves slaves = new Slaves();
                        try {
                            MBusTcpIpLink connection = new MBusTcpIpLink(socket);
                            slaves.setConnection(connection);
                            slaves.open();
                            slaves.addSlave(
                                    new Slave(0x01, 14491001, "DBW", 1, MBusMedium.HOT_WATER));
                            slaves.addSlave(
                                    new Slave(0x01, 14491008, "QKG", 1, MBusMedium.HOT_WATER));
                            slaves.addSlave(
                                    new Slave(0x01, 32104833, "H@P", 1, MBusMedium.ELECTRICITY));
                            slaves.addSlave(new Slave(0x01, 76543210, "H@P", 1, MBusMedium.GAS));

                            while (!connection.isDisconnected()) {
                                Thread.sleep(500);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                slaves.close();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }
    }

}
