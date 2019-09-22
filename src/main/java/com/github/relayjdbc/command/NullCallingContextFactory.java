package com.github.relayjdbc.command;

import com.github.relayjdbc.serial.CallingContext;

/**
 * Dummy class which is doesn't create CallingContexts but returns null.
 */
public class NullCallingContextFactory implements CallingContextFactory {
    public CallingContext create() {
        return null;
    }
}
