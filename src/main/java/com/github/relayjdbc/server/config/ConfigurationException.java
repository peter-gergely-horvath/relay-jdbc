package com.github.relayjdbc.server.config;

public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 4121131450591556150L;

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String msg, Exception cause) {
        super(msg, cause);
    }
}
