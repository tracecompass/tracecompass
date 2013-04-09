/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

/**
 * Callsite information to help with cdt integration
 *
 * @author Matthew Khouzam
 *
 * @since 1.2
 */
public class CTFCallsite implements Comparable<CTFCallsite> {
    /**
     * The callsite constructor
     *
     * @param en
     *            The event name
     * @param func
     *            the function name
     * @param ip
     *            the instruction pointer of the callsite
     * @param fn
     *            the file name of the callsite
     * @param line
     *            the line number of the callsite
     */
    public CTFCallsite(String en, String func, long ip, String fn, long line) {
        EventName = en;
        FileName = fn;
        FunctionName = func;
        this.ip = ip;
        this.LineNumber = line;
    }

    static private final long MASK32 = 0x00000000ffffffffL;
    /**
     * The event name
     */
    private final String EventName;
    /**
     * the file name of the callsite
     */
    private final String FileName;
    /**
     * the instruction pointer
     */
    private final long ip;
    /**
     * the function name
     */
    private final String FunctionName;
    /**
     * the line number of the callsite
     */
    private final long LineNumber;

    /* Getters */
    /**
     * @return the eventName
     */
    public String getEventName() {
        return EventName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return FileName;
    }

    /**
     * @return the ip
     */
    public long getIp() {
        return ip;
    }

    /**
     * @return the functionName
     */
    public String getFunctionName() {
        return FunctionName;
    }

    /**
     * @return the lineNumber
     */
    public long getLineNumber() {
        return LineNumber;
    }

    /*
     * The callsites will be sorted by calling addresses. To do this we take IPs
     * (instruction pointers) and compare them. Java only supports signed
     * operation and since memory addresses are unsigned, we will convert the
     * longs into integers that contain the high and low bytes and compare them.
     */
    @Override
    public int compareTo(CTFCallsite o) {
        /*
         * mask32 is 32 zeros followed by 32 ones, when we bitwise and this it
         * will return the lower 32 bits
         */

        long other = o.ip;
        /*
         * To get a high int: we downshift by 32 and bitwise and with the mask
         * to get rid of the sign
         *
         * To get the low int: we bitwise and with the mask.
         */
        long otherHigh = (other >> 32) & MASK32;
        long otherLow = other & MASK32;
        long ownHigh = (ip >> 32) & MASK32;
        long ownLow = ip & MASK32;
        /* are the high values different, if so ignore the lower values */
        if (ownHigh > otherHigh) {
            return 1;
        }
        if (ownHigh < otherHigh ) {
            return -1;
        }
        /* the high values are the same, compare the lower values */
        if (ownLow > otherLow) {
            return 1;
        }
        if (ownLow < otherLow) {
            return -1;
        }
        /* the values are identical */
        return 0;
    }

    @Override
    public String toString() {
        return FileName + "/" + FunctionName + ":" + LineNumber; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
