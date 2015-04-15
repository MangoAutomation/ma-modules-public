/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

/**
 * @author aploese
 */
public interface MBusConnectionType {
    public static int SERIAL_DIRECT = 1;
    public static int SERIAL_AT_MODEM = 2;
    public static int TCPIP = 3;
    public static int SERIAL_BRIDGE = 4;
}
