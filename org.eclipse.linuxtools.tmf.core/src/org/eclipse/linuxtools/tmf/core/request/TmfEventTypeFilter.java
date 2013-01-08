/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

/**
 * An event filter base on the requested data type.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public final class TmfEventTypeFilter implements ITmfFilter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Filter for all types of events
     */
    public static final TmfEventTypeFilter ALL_EVENTS = new TmfEventTypeFilter(ITmfEvent.class);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Event type
     */
    private final Class<? extends ITmfEvent> fEventType;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param eventType the event type
     */
    public TmfEventTypeFilter(Class<? extends ITmfEvent> eventType) {
        fEventType = eventType != null ? eventType : ITmfEvent.class;
    }

    /**
     * Copy constructor
     *
     * @param other the other filter
     */
    public TmfEventTypeFilter(TmfEventTypeFilter other) {
        fEventType = other.fEventType;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the event type
     */
    public Class<? extends ITmfEvent> getEventType() {
        return fEventType;
    }

    // ------------------------------------------------------------------------
    // ITmfFilter
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.filter.ITmfFilter#matches(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public boolean matches(ITmfEvent event) {
        return fEventType.isInstance(event);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fEventType == null) ? 0 : fEventType.hashCode());
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventTypeFilter)) {
            return false;
        }
        TmfEventTypeFilter other = (TmfEventTypeFilter) obj;
        if (fEventType == null) {
            if (other.fEventType != null) {
                return false;
            }
        } else if (!fEventType.equals(other.fEventType)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventTypeFilter [fEventType=" + fEventType.getSimpleName() + "]";
    }

}
