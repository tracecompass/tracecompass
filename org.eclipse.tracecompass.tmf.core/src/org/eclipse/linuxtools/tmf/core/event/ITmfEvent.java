/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The generic event structure in TMF. In its canonical form, an event has:
 * <ul>
 * <li>a parent trace
 * <li>a rank (order within the trace)
 * <li>a timestamp
 * <li>a source (reporting component)
 * <li>a type
 * <li>a content (payload)
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker (filename:lineno) to indicate where the event
 * was generated.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see TmfEvent
 */
public interface ITmfEvent extends IAdaptable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Pre-defined event timestamp attribute (for searching &filtering purposes)
     */
    public static final @NonNull String EVENT_FIELD_TIMESTAMP = ":timestamp:"; //$NON-NLS-1$

    /**
     * Pre-defined event source attribute (for searching &filtering purposes)
     */
    public static final @NonNull String EVENT_FIELD_SOURCE = ":source:"; //$NON-NLS-1$

    /**
     * Pre-defined event type attribute (for searching &filtering purposes)
     */
    public static final @NonNull String EVENT_FIELD_TYPE = ":type:"; //$NON-NLS-1$

    /**
     * Pre-defined event content attribute (for searching &filtering purposes)
     */
    public static final @NonNull String EVENT_FIELD_CONTENT = ":content:"; //$NON-NLS-1$

    /**
     * Pre-defined event reference attribute (for searching &filtering purposes)
     */
    public static final @NonNull String EVENT_FIELD_REFERENCE = ":reference:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace that 'owns' the event
     */
    ITmfTrace getTrace();

    /**
     * @return the event rank within the parent trace
     */
    long getRank();

    /**
     * @return the event timestamp
     * @since 2.0
     */
    ITmfTimestamp getTimestamp();

    /**
     * @return the event source
     */
    String getSource();

    /**
     * @return the event type
     */
    ITmfEventType getType();

    /**
     * @return the event content
     */
    ITmfEventField getContent();

    /**
     * @return the event reference
     */
    String getReference();

}
