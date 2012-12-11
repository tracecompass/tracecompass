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

/**
 * CtfLocationData, the data in a CTF location.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class CtfLocationData implements Comparable<CtfLocationData> {

    private final long timestamp;
    private final long index;

    /**
     * @param ts
     *            Timestamp
     * @param index
     *            Index of this event (if there are N elements with the same
     *            timestamp, which one is it.)
     */
    public CtfLocationData(long ts, long index) {
        this.timestamp = ts;
        this.index = index;
    }

    /**
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The index of the element
     */
    public long getIndex() {
        return index;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (index ^ (index >>> 32));
        result = (prime * result) + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CtfLocationData)) {
            return false;
        }
        CtfLocationData other = (CtfLocationData) obj;
        if (index != other.index) {
            return false;
        }
        if (timestamp != other.timestamp) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Element [" + timestamp + '/' + index + ']'; //$NON-NLS-1$
    }

    @Override
    public int compareTo(CtfLocationData other) {
        if (this.timestamp > other.getTimestamp()) {
            return 1;
        }
        if (this.timestamp < other.getTimestamp()) {
            return -1;
        }
        if (this.index > other.getIndex()) {
            return 1;
        }
        if (this.index < other.getIndex()) {
            return -1;
        }
        return 0;
    }

}
