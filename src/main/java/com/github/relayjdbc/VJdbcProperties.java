// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc;

public final class VJdbcProperties {
    // System properties to transfer to the server when opening a connection 
    public static final String CLIENTINFO_PROPERTIES = "vjdbc.clientinfo.properties";
    // Tables to be cached, property must be in the format "Table[:Refresh-Interval],Table..."
    public static final String CACHE_TABLES = "vjdbc.cache.tables";
    // Login-Handler-Class which authenticates the user
    public static final String LOGIN_USER = "vjdbc.login.user";
    public static final String LOGIN_PASSWORD = "vjdbc.login.password";
    // Signaling using of SSL sockets for RMI communication (true or false, default: false)
    public static final String RMI_SSL = "vjdbc.rmi.ssl";
    // Flag that signals usage of Jakarta HTTP-Client instead of the default implementation
    public static final String SERVLET_USE_JAKARTA_HTTP_CLIENT = "vjdbc.servlet.use_jakarta_http_client";
   
    public static final String SERVLET_USE_KRYO_HTTP_CLIENT = "vjdbc.servlet.use_kryo_http_client";
    
    // Factory class that create Servlet-Request enhancers which can put additional Request-Properties
    // in HTTP-Requests
    public static final String SERVLET_REQUEST_ENHANCER_FACTORY = "vjdbc.servlet.request_enhancer_factory";
    
    public static final String USER_NAME = "vjdbc.user.name";
    
    public static final String COMPRESSION_MODE = "vjdbc.compression.mode";
    public static final String COMPRESSION_THRESHOLD = "vjdbc.compression.threshold";
    public static final String ROW_PACKET_SIZE = "vjdbc.row.packet.size";
    public static final String PERFORMANCE_PROFILE = "vjdbc.performance.profile";
}