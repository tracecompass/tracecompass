/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * An entry, or row, in the Call Stack view
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackEntry extends TimeGraphEntry {

    private final int fQuark;
    private final int fStackLevel;
    private final ITmfTrace fTrace;
    private String fFunctionName;
    private long fFunctionEntryTime;
    private long fFunctionExitTime;
    private @NonNull ITmfStateSystem fSS;

    /**
     * Standard constructor
     *
     * @param quark
     *            The call stack quark
     * @param stackLevel
     *            The stack level
     * @param trace
     *            The trace that this view is talking about
     * @deprecated Use {@link #CallStackEntry(int, int, ITmfTrace, ITmfStateSystem)}
     */
    @Deprecated
    public CallStackEntry(int quark, int stackLevel, ITmfTrace trace) {
        super(null, 0, 0);
        throw new UnsupportedOperationException();
    }

    /**
     * Standard constructor
     *
     * @param name
     *            The parent thread name
     * @param quark
     *            The call stack quark
     * @param stackLevel
     *            The stack level
     * @param trace
     *            The trace that this view is talking about
     * @param ss
     *            The call stack state system
     * @since 3.1
     */
    public CallStackEntry(String name, int quark, int stackLevel, ITmfTrace trace, @NonNull ITmfStateSystem ss) {
        super(name, 0, 0);
        fQuark = quark;
        fStackLevel = stackLevel;
        fTrace = trace;
        fFunctionName = ""; //$NON-NLS-1$
        fSS = ss;
    }

    /**
     * Get the function name of the call stack entry
     * @return the function name
     */
    public String getFunctionName() {
        return fFunctionName;
    }

    /**
     * Set the function name of the call stack entry
     * @param functionName the function name
     */
    public void setFunctionName(String functionName) {
        fFunctionName = functionName;
    }

    /**
     * Set the start time of the call stack entry
     * @param startTime the start time
     * @deprecated Use {@link #setFunctionEntryTime(long)}
     */
    @Deprecated
    public void setStartTime(long startTime) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the end time of the call stack entry
     * @param endTime the end time
     * @deprecated Use {@link #setFunctionExitTime(long)}
     */
    @Deprecated
    public void setEndTime(long endTime) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the selected function entry time
     *
     * @param entryTime
     *            the function entry time
     * @since 3.1
     */
    public void setFunctionEntryTime(long entryTime) {
        fFunctionEntryTime = entryTime;
    }

    /**
     * Get the selected function entry time
     *
     * @return the function entry time
     * @since 3.1
     */
    public long getFunctionEntryTime() {
        return fFunctionEntryTime;
    }

    /**
     * Set the selected function exit time
     *
     * @param exitTime
     *            the function exit time
     * @since 3.1
     */
    public void setFunctionExitTime(long exitTime) {
        fFunctionExitTime = exitTime;
    }

    /**
     * Get the selected function exit time
     *
     * @return the function exit time
     * @since 3.1
     */
    public long getFunctionExitTime() {
        return fFunctionExitTime;
    }

    /**
     * Retrieve the attribute quark that's represented by this entry.
     *
     * @return The integer quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Retrieve the stack level associated with this entry.
     *
     * @return The stack level or 0
     */
    public int getStackLevel() {
        return fStackLevel;
    }

    /**
     * Retrieve the trace that is associated to this view.
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Retrieve the call stack state system associated with this entry.
     *
     * @return The call stack state system
     * @since 3.1
     */
    public @NonNull ITmfStateSystem getStateSystem() {
        return fSS;
    }

}
