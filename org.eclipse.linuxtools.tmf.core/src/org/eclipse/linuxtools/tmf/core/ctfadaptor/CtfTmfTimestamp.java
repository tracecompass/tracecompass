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

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * The CTF adapter for the TMF timestamp
 *
 * @version 1.1
 * @author Matthew khouzam
 */
public class CtfTmfTimestamp extends TmfTimestamp {

    /**
     */
    public enum TimestampType {
        /**
         * yyyy/mm/dd hh:mm:ss.nnnnnnnnnn
         */
        FULL_DATE,
        /**
         * hh:mm:ss.nnnnnnnnnn
         */
        DAY,
        /**
         * nnnnnnnnnnnnnnnnnnnnn ns
         */
        NANOS,
        /**
         * ssssssssss.nnnnnnnnnn s
         */
        SECONDS
    }

    private TimestampType type;

    /**
     * Constructor for CtfTmfTimestamp.
     * @param timestamp long
     */
    public CtfTmfTimestamp(long timestamp) {
        super(timestamp, ITmfTimestamp.NANOSECOND_SCALE, 0);
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
