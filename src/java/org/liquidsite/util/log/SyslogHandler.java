/*
 * SyslogHandler.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Copyright (c) 2005 Per Cederberg. All rights reserved.
 */

package org.liquidsite.util.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A UNIX syslog handler. This implementation is based on RFC 3164.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SyslogHandler extends Handler {

    /**
     * The emergency log severity. Used when the system is unusable.
     */
    protected static final int EMERGENCY_SEVERITY = 0;

    /**
     * The alert log severity. Used when action must be taken
     * immediately.
     */
    protected static final int ALERT_SEVERITY = 1;

    /**
     * The critical log severity. Used for critical conditions.
     */
    protected static final int CRITICAL_SEVERITY = 2;

    /**
     * The error log severity. Used for error conditions.
     */
    protected static final int ERROR_SEVERITY = 3;

    /**
     * The warning log severity. Used for warning conditions.
     */
    protected static final int WARNING_SEVERITY = 4;

    /**
     * The notice log severity. Used for normal but significant
     * conditions.
     */
    protected static final int NOTICE_SEVERITY = 5;

    /**
     * The informational log severity. Used for informational
     * messages.
     */
    protected static final int INFO_SEVERITY = 6;

    /**
     * The debug log severity. Used for debug-level messages.
     */
    protected static final int DEBUG_SEVERITY = 7;

    /**
     * The date format to use.
     */
    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("MMM dd HH:MM:ss");

    /**
     * The maximum log message size.
     */
    private static int MAX_SIZE = 1024;

    /**
     * The datagram socket used for sending log messages.
     */
    private DatagramSocket socket = null;

    /**
     * The IP address of the syslog server.
     */
    private InetAddress address = null;

    /**
     * The port number of the syslog server.
     */
    private int port = 514;

    /**
     * The facility number.
     */
    private int facility = 16;

    /**
     * The host name.
     */
    private String hostname = "localhost";

    /**
     * The tag name.
     */
    private String tag = "java";

    /**
     * Creates a new syslog handler.
     *
     * @throws SocketException if an UDP socket couldn't be opened
     * @throws UnknownHostException if the localhost IP address
     *             couldn't be determined
     */
    public SyslogHandler() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getLocalHost();
    }    

    /**
     * Returns the IP address of the syslog server. The default is
     * to use localhost.
     *
     * @return the IP address of the syslog server
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * Sets the IP address of the syslog server.
     *
     * @param address        the IP address of the syslog server
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Returns the port number of the syslog server. The default is
     * to use port 514.
     *
     * @return the port number of the syslog server
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Sets the port number of the syslog server.
     *
     * @param port           the port number of the syslog server
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the log facility to use.
     *
     * @return the log facility to use
     */
    public int getFacility() {
        return facility;
    }

    /**
     * Sets the log facility to use.
     *
     * @param facility       the new log facility
     */
    public void setFacility(int facility) {
        this.facility = facility;
    }

    /**
     * Returns the hostname to use.
     *
     * @return the hostname to use
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname to use.
     *
     * @param hostname       the new hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Returns the log tag to use.
     *
     * @return the log tag to use
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the log tag to use.
     *
     * @param tag            the new log tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Publishes a log record.
     *
     * @param record         the log record to publish
     */
    public void publish(LogRecord record) {
        StringBuffer    buffer = new StringBuffer(256);
        DatagramPacket  packet;
        byte[]          payload;
        int             size;

        // Write the PRI part
        buffer.append("<");
        buffer.append(facility << 3 + getSeverity(record));
        buffer.append(">");

        // Write the HEADER part
        buffer.append(DATE_FORMAT.format(new Date(record.getMillis())));
        buffer.append(" ");
        buffer.append(hostname);
        buffer.append(" ");

        // Write the MSG part
        buffer.append(tag);
        buffer.append(": ");
        buffer.append("[");
        buffer.append(record.getSourceClassName());
        buffer.append(" ");
        buffer.append(record.getSourceMethodName());
        buffer.append("] ");
        buffer.append(record.getMessage());

        // Send datagram packet
        try {
            payload = buffer.toString().getBytes();
            size = (payload.length > MAX_SIZE) ? MAX_SIZE : payload.length;
            packet = new DatagramPacket(payload, size, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("failed to write to syslog: " +
                               e.getMessage());
        }
    }

    /**
     * Closes the log handler and frees any resources.
     */
    public void close() {
        socket.close();
    }

    /**
     * Flushes any buffered output.
     */
    public void flush() {
        // Do nothing
    }

    /**
     * Returns the syslog severity constant corresponding to a log
     * record.
     *
     * @param record         the log record
     *
     * @return the syslog severity constant value
     */
    private int getSeverity(LogRecord record) {
        int  level = record.getLevel().intValue();

        if (level == Level.SEVERE.intValue()) {
            return ERROR_SEVERITY;
        } else if (level == Level.WARNING.intValue()) {
            return WARNING_SEVERITY;
        } else if (level == Level.INFO.intValue()) {
            return INFO_SEVERITY;
        } else {
            return DEBUG_SEVERITY;
        }
    }
}
