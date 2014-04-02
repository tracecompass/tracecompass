/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support filters tracking
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventFilterAppliedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Central trace manager for TMF. It tracks the currently opened traces and
 * experiment, as well as the currently-selected time or time range and the
 * current window time range for each one of those. It also tracks filters
 * applied for each trace.
 *
 * It's a singleton class, so only one instance should exist (available via
 * {@link #getInstance()}).
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class TmfTraceManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Map<ITmfTrace, TmfTraceContext> fTraces;

    /** The currently-selected trace. Should always be part of the trace map */
    private ITmfTrace fCurrentTrace = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    private TmfTraceManager() {
        fTraces = new LinkedHashMap<>();
        TmfSignalManager.registerVIP(this);
    }

    /** Singleton instance */
    private static TmfTraceManager tm = null;

    /**
     * Get an instance of the trace manager.
     *
     * @return The trace manager
     */
    public static synchronized TmfTraceManager getInstance() {
        if (tm == null) {
            tm = new TmfTraceManager();
        }
        return tm;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return The begin timestamp of selection
     * @since 2.1
     */
    public ITmfTimestamp getSelectionBeginTime() {
        return getCurrentTraceContext().getSelectionBegin();
    }

    /**
     * @return The end timestamp of selection
     * @since 2.1
     */
    public ITmfTimestamp getSelectionEndTime() {
        return getCurrentTraceContext().getSelectionEnd();
    }

    /**
     * Return the current window time range.
     *
     * @return the current window time range
     */
    public synchronized TmfTimeRange getCurrentRange() {
        return getCurrentTraceContext().getWindowRange();
    }

    /**
     * Gets the filter applied to the current trace
     *
     * @return
     *          a filter, or <code>null</code>
     * @since 2.2
     */
    public synchronized ITmfFilter getCurrentFilter() {
        return getCurrentTraceContext().getFilter();
    }

    /**
     * Get the currently selected trace (normally, the focused editor).
     *
     * @return The active trace
     */
    public synchronized ITmfTrace getActiveTrace() {
        return fCurrentTrace;
    }

    /**
     * Get the trace set of the currently active trace.
     *
     * @return The active trace set
     * @see #getTraceSet(ITmfTrace)
     */
    public synchronized ITmfTrace[] getActiveTraceSet() {
        final ITmfTrace trace = fCurrentTrace;
        return getTraceSet(trace);
    }

    /**
     * Get the currently-opened traces, as an unmodifiable set.
     *
     * @return A set containing the opened traces
     */
    public synchronized Set<ITmfTrace> getOpenedTraces() {
        return Collections.unmodifiableSet(fTraces.keySet());
    }

    /**
     * Get the editor file for an opened trace.
     *
     * @param trace
     *            the trace
     * @return the editor file or null if the trace is not opened
     * @since 3.0
     */
    public synchronized IFile getTraceEditorFile(ITmfTrace trace) {
        TmfTraceContext ctx = fTraces.get(trace);
        if (ctx != null) {
            return ctx.getEditorFile();
        }
        return null;
    }

    private TmfTraceContext getCurrentTraceContext() {
        TmfTraceContext curCtx = fTraces.get(fCurrentTrace);
        if (curCtx == null) {
            /* There are no traces opened at the moment. */
            return TmfTraceContext.NULL_CONTEXT;
        }
        return curCtx;
    }

    // ------------------------------------------------------------------------
    // Public utility methods
    // ------------------------------------------------------------------------

    /**
     * Get the trace set of a given trace. For a standard trace, this is simply
     * an array with only that trace in it. For experiments, this is an array of
     * all the traces contained in this experiment.
     *
     * @param trace
     *            The trace or experiment
     * @return The corresponding trace set
     */
    public static ITmfTrace[] getTraceSet(ITmfTrace trace) {
        if (trace == null) {
            return null;
        }
        if (trace instanceof TmfExperiment) {
            TmfExperiment exp = (TmfExperiment) trace;
            return exp.getTraces();
        }
        return new ITmfTrace[] { trace };
    }

    /**
     * Return the path (as a string) to the directory for supplementary files to
     * use with a given trace. If no supplementary file directory has been
     * configured, a temporary directory based on the trace's name will be
     * provided.
     *
     * @param trace
     *            The trace
     * @return The path to the supplementary file directory (trailing slash is
     *         INCLUDED!)
     */
    public static String getSupplementaryFileDir(ITmfTrace trace) {
        IResource resource = trace.getResource();
        if (resource == null) {
            return getTemporaryDir(trace);
        }

        String supplDir = null;
        try {
            supplDir = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
        } catch (CoreException e) {
            return getTemporaryDir(trace);
        }
        return supplDir + File.separator;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Signal handler for the traceOpened signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceOpened(final TmfTraceOpenedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        final IFile editorFile = signal.getEditorFile();
        final ITmfTimestamp startTs = trace.getStartTime();

        /* Calculate the initial time range */
        final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;
        long offset = trace.getInitialRangeOffset().normalize(0, SCALE).getValue();
        long endTime = startTs.normalize(0, SCALE).getValue() + offset;
        final TmfTimeRange startTr = new TmfTimeRange(startTs, new TmfTimestamp(endTime, SCALE));

        final TmfTraceContext startCtx = new TmfTraceContext(startTs, startTs, startTr, editorFile);

        fTraces.put(trace, startCtx);

        /* We also want to set the newly-opened trace as the active trace */
        fCurrentTrace = trace;
    }


    /**
     * Handler for the TmfTraceSelectedSignal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceSelected(final TmfTraceSelectedSignal signal) {
        final ITmfTrace newTrace = signal.getTrace();
        if (!fTraces.containsKey(newTrace)) {
            throw new RuntimeException();
        }
        fCurrentTrace = newTrace;
    }

    /**
     * Signal handler for the filterApplied signal.
     *
     * @param signal
     *            The incoming signal
     * @since 2.2
     */
    @TmfSignalHandler
    public synchronized void filterApplied(TmfEventFilterAppliedSignal signal) {
        final ITmfTrace newTrace = signal.getTrace();
        TmfTraceContext context = fTraces.get(newTrace);
        if (context == null) {
            throw new RuntimeException();
        }
        fTraces.put(newTrace, new TmfTraceContext(context, signal.getEventFilter()));
    }

    /**
     * Signal handler for the traceClosed signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        fTraces.remove(signal.getTrace());
        if (fTraces.size() == 0) {
            fCurrentTrace = null;
            /*
             * In other cases, we should receive a traceSelected signal that
             * will indicate which trace is the new one.
             */
        }
    }

    /**
     * Signal handler for the TmfTimeSynchSignal signal.
     *
     * The current time of *all* traces whose range contains the requested new
     * selection time range will be updated.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeUpdated(final TmfTimeSynchSignal signal) {
        final ITmfTimestamp beginTs = signal.getBeginTime();
        final ITmfTimestamp endTs = signal.getEndTime();

        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            if (beginTs.intersects(getValidTimeRange(trace)) || endTs.intersects(getValidTimeRange(trace))) {
                TmfTraceContext prevCtx = entry.getValue();
                TmfTraceContext newCtx = new TmfTraceContext(prevCtx, beginTs, endTs);
                entry.setValue(newCtx);
            }
        }
    }

    /**
     * Signal handler for the TmfRangeSynchSignal signal.
     *
     * The current window time range of *all* valid traces will be updated
     * to the new requested times.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeRangeUpdated(final TmfRangeSynchSignal signal) {
        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            final TmfTraceContext curCtx = entry.getValue();

            final TmfTimeRange validTr = getValidTimeRange(trace);

            /* Determine the new time range */
            TmfTimeRange targetTr = signal.getCurrentRange().getIntersection(validTr);
            TmfTimeRange newTr = (targetTr == null ? curCtx.getWindowRange() : targetTr);

            /* Update the values */
            TmfTraceContext newCtx = new TmfTraceContext(curCtx, newTr);
            entry.setValue(newCtx);
        }
    }

    // ------------------------------------------------------------------------
    // Private utility methods
    // ------------------------------------------------------------------------

    /**
     * Return the valid time range of a trace (not the current window time
     * range, but the range of all possible valid timestamps).
     *
     * For a real trace this is the whole range of the trace. For an experiment,
     * it goes from the start time of the earliest trace to the end time of the
     * latest one.
     *
     * @param trace
     *            The trace to check for
     * @return The valid time span, or 'null' if the trace is not valid
     */
    private TmfTimeRange getValidTimeRange(ITmfTrace trace) {
        if (!fTraces.containsKey(trace)) {
            /* Trace is not part of the currently opened traces */
            return null;
        }
        if (!(trace instanceof TmfExperiment)) {
            /* "trace" is a single trace, return its time range directly */
            return trace.getTimeRange();
        }
        final ITmfTrace[] traces = ((TmfExperiment) trace).getTraces();
        if (traces.length == 0) {
            /* We are being trolled */
            return null;
        }
        if (traces.length == 1) {
            /* Trace is an experiment with only 1 trace */
            return traces[0].getTimeRange();
        }
        /*
         * Trace is an experiment with 2+ traces, so get the earliest start and
         * the latest end.
         */
        ITmfTimestamp start = traces[0].getStartTime();
        ITmfTimestamp end = traces[0].getEndTime();
        for (int i = 1; i < traces.length; i++) {
            ITmfTrace curTrace = traces[i];
            if (curTrace.getStartTime().compareTo(start) < 0) {
                start = curTrace.getStartTime();
            }
            if (curTrace.getEndTime().compareTo(end) > 0) {
                end = curTrace.getEndTime();
            }
        }
        return new TmfTimeRange(start, end);
    }

    /**
     * Get a temporary directory based on a trace's name. We will create the
     * directory if it doesn't exist, so that it's ready to be used.
     */
    private static String getTemporaryDir(ITmfTrace trace) {
        String pathName = System.getProperty("java.io.tmpdir") + //$NON-NLS-1$
            File.separator +
            trace.getName() +
            File.separator;
        File dir = new File(pathName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return pathName;
    }
}
