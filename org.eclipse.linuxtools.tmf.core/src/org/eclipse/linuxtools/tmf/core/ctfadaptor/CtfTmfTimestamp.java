/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 */
public class CtfTmfTimestamp extends TmfTimestamp {

    /**
     */
    public enum TimestampType {
        FULL_DATE, DAY, NANOS, SECONDS
    }

    private TimestampType type;

    /**
     * Constructor for CtfTmfTimestamp.
     * @param timestamp long
     */
    public CtfTmfTimestamp(long timestamp) {
        setValue(timestamp, -9, 0);
        type = TimestampType.DAY;
    }

    /**
     * Method setType.
     * @param value TimestampType
     */
    public void setType(TimestampType value) {
        type = value;
    }

    /**
     * Method getType.
     * @return TimestampType
     */
    public TimestampType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.linuxtools.tmf.core.event.TmfTimestamp#getDelta(org.eclipse
     * .linuxtools.tmf.core.event.ITmfTimestamp)
     */
    /**
     * Method getDelta.
     * @param ts ITmfTimestamp
     * @return ITmfTimestamp
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#getDelta(ITmfTimestamp)
     */
    @Override
    public ITmfTimestamp getDelta(ITmfTimestamp ts) {
        TmfTimestamp parent = (TmfTimestamp) super.getDelta(ts);
        long value = parent.getValue();
        long exp = parent.getScale();
        long diff = exp + 9;
        for (int i = 0; i < diff; i++) {
            value *= 10;
        }
        CtfTmfTimestamp retVal = new CtfTmfTimestamp(value);
        if (value > 100000000) {
            retVal.type = TimestampType.SECONDS;
        } else {
            retVal.type = TimestampType.NANOS;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        switch (type) {
        case DAY: {
            return dateToString();
        }
        case FULL_DATE: {
            return toFullDateString();
        }
        case NANOS: {
            return nanoToString();
        }
        case SECONDS:{
            return secondsToString();
        }
        }
        return super.toString();
    }

    /**
     * Method secondsToString.
     * @return String
     */
    private String secondsToString() {
        double timestamp = getValue();
        timestamp /= 1000000000;
        StringBuilder retVal = new StringBuilder();
        retVal.append(timestamp);
        retVal.append(" s"); //$NON-NLS-1$
        return retVal.toString();
    }

    /**
     * Method nanoToString.
     * @return String
     */
    private String nanoToString() {
        final long timestamp = getValue();
        StringBuilder retVal = new StringBuilder();
        retVal.append(timestamp);
        retVal.append(" ns"); //$NON-NLS-1$
        return retVal.toString();
    }

    /**
     * Method dateToString.
     * @return String
     */
    private String dateToString() {
        final long timestamp = getValue();
        final Date d = new Date(timestamp / 1000000);
        final DateFormat df = new SimpleDateFormat("HH:mm:ss."); //$NON-NLS-1$
        final long nanos = (timestamp % 1000000000);
        StringBuilder output = new StringBuilder();
        output.append(df.format(d));
        output.append(String.format("%09d", nanos)); //$NON-NLS-1$
        return output.toString();
    }

    /**
     * Method toFullDateString.
     * @return String
     */
    private String toFullDateString() {
        final long timestamp = getValue();
        final Date d = new Date(timestamp / 1000000);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        final long nanos = (timestamp % 1000000000);
        StringBuilder output = new StringBuilder();
        output.append(df.format(d));
        output.append(String.format("%09d", nanos)); //$NON-NLS-1$
        return output.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode() * prime;
        result += ((type == null) ? 0 : type.toString().hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CtfTmfTimestamp)) {
            return false;
        }
        CtfTmfTimestamp other = (CtfTmfTimestamp) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
