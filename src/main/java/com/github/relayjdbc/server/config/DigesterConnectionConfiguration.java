package com.github.relayjdbc.server.config;

public class DigesterConnectionConfiguration extends ConnectionConfiguration {
    public void setTraceCommandCount(String traceCommandCount) {
        _traceCommandCount = ConfigurationUtil.getBooleanFromString(traceCommandCount);
    }

    public void setTraceOrphanedObjects(String traceOrphandedObjects) {
        _traceOrphanedObjects = ConfigurationUtil.getBooleanFromString(traceOrphandedObjects);
    }

    public void setConnectionPooling(String connectionPooling) {
        _connectionPooling = ConfigurationUtil.getBooleanFromString(connectionPooling);
    }
}
