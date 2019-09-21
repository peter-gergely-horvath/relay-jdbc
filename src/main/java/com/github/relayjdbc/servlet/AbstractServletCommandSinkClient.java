// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.servlet;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import com.github.relayjdbc.command.CommandSink;
import com.github.relayjdbc.util.SQLExceptionHelper;

/**
 * Abstract base class for clients of VJDBC in Servlet-Mode.
 * @author Mike
 *
 */
public abstract class AbstractServletCommandSinkClient implements CommandSink {
    protected final URL _url;
    protected final RequestEnhancer _requestEnhancer;

    public AbstractServletCommandSinkClient(String url, RequestEnhancer requestEnhancer) throws SQLException {
        try {
            _url = new URL(url);
            _requestEnhancer = requestEnhancer;
        } catch(IOException e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void close() {
        // Nothing to do
    }
}