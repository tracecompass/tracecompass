/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.core.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * GDB Trace implementation of TmfEventField
 * @author Francois Chouinard
 */
public class GdbTraceEventContent extends TmfEventField {

    /** Trace Frame field name */
    public static final @NonNull String TRACE_FRAME = "Trace Frame"; //$NON-NLS-1$
    /** Tracepoint field name */
    public static final @NonNull String TRACEPOINT = "Tracepoint"; //$NON-NLS-1$

    // Tracepoint number
    private int fTracepointNumber = 0;
    // frame number
    private int fFrameNumber = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     * @param content the raw content
     * @param tracepointNumber the tracepoint number
     * @param frameNumber the frame number
     */
    public GdbTraceEventContent(String content, int tracepointNumber, int frameNumber) {
        // TmfEvent parent, Object content
        super(ITmfEventField.ROOT_FIELD_ID,
                content.replaceAll("\r?\n", " | "), //$NON-NLS-1$ //$NON-NLS-2$
                new ITmfEventField[] {
                        new TmfEventField(TRACE_FRAME, frameNumber, null),
                        new TmfEventField(TRACEPOINT, tracepointNumber, null)
                });

        fTracepointNumber = tracepointNumber;
        fFrameNumber = frameNumber;
    }

    /**
     * @param other the original event content
     */
    public GdbTraceEventContent(GdbTraceEventContent other) {
        super(other);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the tracepointNumber
     */
    public int getTracepointNumber() {
        return fTracepointNumber;
    }

    /**
     * @return the frameNumber
     */
    public int getFrameNumber() {
        return fFrameNumber;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return getValue().toString();
    }
}
