/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.server;

/**
 * Used to execute a command on the server
 * 
 * @author Terry Packer
 *
 */
public class ServerCommandModel {
    
    private int timeout;
    private String command;
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

}
