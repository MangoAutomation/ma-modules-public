package com.infiniteautomation.serial.rt;

public class ConvertHexException extends Exception {

	private static final long serialVersionUID = -1L;

	public ConvertHexException(String message) {
        super(message);
    }

    public ConvertHexException(Throwable cause) {
        super(cause);
    }
}
