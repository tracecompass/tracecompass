/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.util.LinkedHashMap;
import java.util.Map;

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
 * experiment, as well as the currently-selected timestamps and time ranges for
 * each one of those.
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
        fTraces = new LinkedHashMap<ITmfTrace, TmfTraceContext>();
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
     * Return the current selected time.
     *
     * @return the current time stamp
     */
    public synchronized ITmfTimestamp getCurrentTime() {
        return getCurrentTraceContext().getTimestamp();
    }

    /**
     * Return the current selected range.
     *
     * @return the current time range
     */
    public synchronized TmfTimeRange getCurrentRange() {
        return getCurrentTraceContext().getTimerange();
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
     * Get the currently active trace set. For a 'normal' trace, this is simply
     * an array with only that trace in it. For trace experiments, this will be
     * an array containing the 'real' child traces in the experiment.
     *
     * @return The active trace set
     */
    public synchronized ITmfTrace[] getActiveTraceSet() {
        final ITmfTrace trace = fCurrentTrace;
        if (trace instanceof TmfExperiment) {
            final TmfExperiment exp = (TmfExperiment) trace;
            return exp.getTraces();
        }
        return new ITmfTrace[] { trace };
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
        final ITmfTimestamp startTs = trace.getStartTime();

        /* Calculate the initial time range */
        final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;
        long offset = trace.getInitialRangeOffset().normalize(0, SCALE).getValue();
        long endTime = startTs.normalize(0, SCALE).getValue() + offset;
        final TmfTimeRange startTr = new TmfTimeRange(startTs, new TmfTimestamp(endTime, SCALE));

        final TmfTraceContext startCtx = new TmfTraceContext(startTs, startTr);

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
     * time will be updated.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeUpdated(final TmfTimeSynchSignal signal) {
        final ITmfTimestamp ts = signal.getCurrentTime();

        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            if (ts.intersects(getValidTimeRange(trace))) {
                TmfTraceContext prevCtx = entry.getValue();
                TmfTraceContext newCtx = new TmfTraceContext(prevCtx, ts);
                entry.setValue(newCtx);
            }
        }
    }

    /**
     * Signal handler for the TmfRangeSynchSignal signal.
     *
     * The current timestamp and timerange of *all* valid traces will be updated
     * to the new requested times.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeRangeUpdated(final TmfRangeSynchSignal signal) {
        final ITmfTimestamp signalTs = signal.getCurrentTime();

        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            final TmfTraceContext curCtx = entry.getValue();

            final TmfTimeRange validTr = getValidTimeRange(trace);

            /* Determine the new time stamp */
            ITmfTimestamp newTs;
            if (signalTs != null && signalTs.intersects(validTr)) {
                newTs = signalTs;
            } else {
                newTs = curCtx.getTimestamp();
            }

            /* Determine the new time range */
            TmfTimeRange targetTr = signal.getCurrentRange().getIntersection(validTr);
            TmfTimeRange newTr = (targetTr == null ? curCtx.getTimerange() : targetTr);

            /* Update the values */
            TmfTraceContext newCtx = new TmfTraceContext(newTs, newTr);
            entry.setValue(newCtx);
        }
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    /**
     * Return the valid time range of a trace (not the "current time range", but
     * the range of all possible valid timestamps).
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
}
