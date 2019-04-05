/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Terry Packer
 *
 */
public class InputStreamWrapper extends InputStream {

    private final InputStream delegate;
    private boolean disconnected;
    
    public InputStreamWrapper(InputStream stream) {
        this.delegate = stream;
    }
    
    @Override
    public int read() throws IOException {
        int read = this.delegate.read();
        if(read == -1) {
            this.disconnected = true;
        }
        return read;
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}
