/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfStreamBookmark</u></b>
 * <p>
 * This class maps an event timestamp with a trace location.
 */
public class TmfStreamBookmark implements Comparable<TmfStreamBookmark> {

    // ========================================================================
    // Attributes
    // ========================================================================
    
    private final TmfTimestamp fTimestamp;
    private final Object fLocation;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param ts
     * @param location
     */
    public TmfStreamBookmark(TmfTimestamp ts, Object location) {
        fTimestamp = ts;
        fLocation = location;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the bookmarked event timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the bookmarked event stream location
     */
    public Object getLocation() {
        return fLocation;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public int compareTo(TmfStreamBookmark other) {
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
