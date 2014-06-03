/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.pcap.core.protocol.ethernet2.EthernetIIValues;
import org.eclipse.linuxtools.pcap.core.protocol.ipv4.IPv4Values;

/**
 * Class for helping with the conversion of data.
 *
 * @author Vincent Perot
 */
public final class ConversionHelper {


    @SuppressWarnings("null")
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray(); //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DEFAULT_TIME_PATTERN);

    private ConversionHelper() {
    }

    /**
     * Generate an integer from an unsigned byte.
     *
     * @param n
     *            the unsigned byte.
     * @return the integer representing the unsigned value.
     */
    public static int unsignedByteToInt(byte n) {
        return n & 0x000000FF;
    }

    /**
     * Generate an integer from an unsigned short.
     *
     * @param n
     *            the unsigned short.
     * @return the integer representing the unsigned value.
     */
    public static int unsignedShortToInt(short n) {
        return n & 0x0000FFFF;
    }

    /**
     * Generate a long from an unsigned integer.
     *
     * @param n
     *            the unsigned integer.
     * @return the long representing the unsigned value.
     */
    public static long unsignedIntToLong(int n) {
        return n & 0x00000000FFFFFFFFL;
    }

    /**
     * Generate an hex number from a byte array.
     *
     * @param bytes
     *            The array of bytes.
     * @param spaced
     *            Whether there must be a space between each byte or not.
     * @return the hex as a string.
     */
    public static String bytesToHex(byte[] bytes, boolean spaced) {
        // No need to check for character encoding since bytes represents a
        // number.

        if (bytes.length == 0) {
            return EMPTY_STRING;
        }

        char[] hexChars = spaced ? new char[bytes.length * 3 - 1] : new char[bytes.length * 2];
        int delta = spaced ? 3 : 2;
        char separator = ' ';

        for (int j = 0; j < bytes.length; j++) {

            int v = bytes[j] & 0xFF;
            hexChars[j * delta] = HEX_ARRAY[v >>> 4];
            hexChars[j * delta + 1] = HEX_ARRAY[v & 0x0F];

            if (spaced && (j != bytes.length - 1)) {
                hexChars[j * delta + 2] = separator;
            }
        }
        return new String(hexChars);
    }

    // TODO Add little endian support
    /**
     * Generate a string representing the MAC address.
     *
     * @param mac
     *            The MAC address as a byte array.
     * @return The string representing the MAC address.
     */
    public static String toMacAddress(byte[] mac) {

        if (mac.length != EthernetIIValues.MAC_ADDRESS_SIZE) {
            throw new IllegalArgumentException();
        }
        char separator = ':';
        return String.format("%02x", mac[0]) + separator + //$NON-NLS-1$
                String.format("%02x", mac[1]) + separator + //$NON-NLS-1$
                String.format("%02x", mac[2]) + separator + //$NON-NLS-1$
                String.format("%02x", mac[3]) + separator + //$NON-NLS-1$
                String.format("%02x", mac[4]) + separator + //$NON-NLS-1$
                String.format("%02x", mac[5]); //$NON-NLS-1$

    }

    // TODO Add little endian support
    /**
     * Generate a string representing the IP address.
     *
     * @param ip
     *            The IP address as a byte array.
     * @return The string representing the IP address.
     */
    public static String toIpAddress(byte[] ip) {

        if (ip.length != IPv4Values.IP_ADDRESS_SIZE) {
            throw new IllegalArgumentException();
        }
        char separator = '.';
        return Integer.toString(ip[0] & 0xFF) + separator +
                Integer.toString(ip[1] & 0xFF) + separator +
                Integer.toString(ip[2] & 0xFF) + separator +
                Integer.toString(ip[3] & 0xFF);

    }

    // TODO support non GMT time.

    /**
     * Convert a timestamp into a date.
     *
     * @param ts
     *            The timestamp. It represents the time since Epoch in
     *            microseconds.
     * @param scale
     *            The scale of the timestamp.
     * @return The date as a string.
     */
    public static String toGMTTime(long ts, PcapTimestampScale scale) {
        long timestamp;
        switch (scale) {
        case MICROSECOND:
            timestamp = ts * 1000;
            break;
        case NANOSECOND:
            timestamp = ts;
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }
        return format(timestamp);
    }

    /**
     * Format the timestamp to a string.
     *
     * @param value
     *            the timestamp value to format (in ns)
     * @return the formatted timestamp
     */
    private static String format(long value) {
        // Split the timestamp value into its sub-components
        long date = value / 1000000; // milliseconds since epoch
        long cs = Math.abs((value % 1000000) / 1000); // microseconds
        long ns = Math.abs(value % 1000); // nanoseconds

        Date dateObject = new Date(date);

        StringBuilder sb = new StringBuilder(DATE_FORMATTER.format(dateObject));
        sb.append('.')
                .append(String.format("%03d", cs)) //$NON-NLS-1$
                .append('.')
                .append(String.format("%03d", ns)); //$NON-NLS-1$

        String string = sb.toString();
        if (string == null) {
            return EMPTY_STRING;
        }
        return string;

    }

}
