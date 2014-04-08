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

package org.eclipse.linuxtools.tmf.ctf.core;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventTypeManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The CTF extension of the TMF event type
 *
 * @version 1.0
 * @author Matthew khouzam
 */
public class CtfTmfEventType extends TmfEventType {

    /**
     * CTFTmfEventType context for the event type manager
     */
    private static final String CONTEXT_ID = "Ctf Event"; //$NON-NLS-1$

    private static final String UNKNOWN_TRACE = "unknown"; //$NON-NLS-1$

    /**
     * Constructor for CtfTmfEventType.
     *
     * @param eventName
     *            The event name
     * @param trace
     *            the parent trace
     * @param content
     *            The event field
     * @since 3.0
     */
    public CtfTmfEventType(String eventName, ITmfTrace trace, ITmfEventField content) {
        super(computeContextName(trace), eventName, content);
    }

    /**
     * Method toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * gets the event type for an event name
     *
     * @param trace
     *            the parent trace
     * @param eventName
     *            the event name
     * @return the event type
     * @since 3.0
     */
    public static CtfTmfEventType get(CtfTmfTrace trace, String eventName) {
        return (CtfTmfEventType) TmfEventTypeManager.getInstance().getType(computeContextName(trace), eventName);
    }

    /**
     * Get the context name of a ctf trace
     *
     * @param trace
     *            the trace
     * @return the context name
     * @since 3.0
     */
    public static String computeContextName(ITmfTrace trace) {
        return CONTEXT_ID + "/" + (trace == null ? UNKNOWN_TRACE : trace.getPath()); //$NON-NLS-1$
    }
}
