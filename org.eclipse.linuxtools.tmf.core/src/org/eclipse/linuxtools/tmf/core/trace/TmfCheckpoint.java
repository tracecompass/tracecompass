/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * <b><u>TmfCheckpoint</u></b>
 * <p>
 * This class maps an event timestamp to a generic location.
 */
@SuppressWarnings("rawtypes")
public class TmfCheckpoint implements Comparable<TmfCheckpoint>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfTimestamp fTimestamp;
    private ITmfLocation<? extends Comparable> fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private TmfCheckpoint() {
    }

    /**
     * @param ts the checkpoint timestamp
     * @param location the corresponding trace location
     */
    public TmfCheckpoint(final ITmfTimestamp ts, final ITmfLocation<? extends Comparable> location) {
        fTimestamp = ts;
        fLocation = location;
    }

    /**
     * Deep copy constructor
     * 
     * @param other the other checkpoint
     */
    public TmfCheckpoint(final TmfCheckpoint other) {
        if (other == null)
            throw new IllegalArgumentException();
        fTimestamp = other.fTimestamp.clone();
        fLocation = other.fLocation.clone();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the checkpoint timestamp
     */
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the checkpoint stream location
     */
    public ITmfLocation<?> getLocation() {
        return fLocation;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public TmfCheckpoint clone() {
        TmfCheckpoint result = null;
        try {
            result = (TmfCheckpoint) super.clone();
            result.fTimestamp = fTimestamp.clone();
            result.fLocation = fLocation.clone();
            return result;
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int hashCode() {
        return fTimestamp.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TmfCheckpoint))
            return false;
        final TmfCheckpoint o = (TmfCheckpoint) other;
        return fTimestamp.equals(o.fTimestamp);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfCheckpoint(" + fTimestamp + "," + fLocation + ")]";
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(final TmfCheckpoint other) {
        if (fTimestamp == null || other.fTimestamp == null)
            return fLocation.getLocation().compareTo(other.fLocation.getLocation());
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
