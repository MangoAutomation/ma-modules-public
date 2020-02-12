/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.serial.rt;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Very simple program to open a port, wait for a connection and then spit out
 * some data every second.
 *
 * This was used to show that the Virtual Client type serial port does not work with this data source
 *   and should be used when we fix it to work.
 *
 * @author Terry Packer
 */
public class TestVirtualSerialServer {

    public static void main(String[] args) {

        //Frame to send
        String frame = "0238203920302036302036363832204D20312030203020302030203020302030203020302030203020302030203020353420342E35203633202B52412032302E322039312041424344030D0A";

        try (ServerSocket socket = new ServerSocket(9000);){

            Socket out = socket.accept();
            for(int i=0; i<100; i++) {
                out.getOutputStream().write(frame.getBytes());
                out.getOutputStream().flush();
                Thread.sleep(1000);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
