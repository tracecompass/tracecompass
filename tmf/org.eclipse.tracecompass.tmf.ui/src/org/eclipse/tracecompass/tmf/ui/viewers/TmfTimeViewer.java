/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation in TmfXYChartViewer
 *   Geneviève Bastien - Moved methods from TmfXYChartViewer to this interface
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.ITmfPinnable;

/**
 * Abstract class that extends {@link TmfViewer} that adds methods to
 * synchronize with a trace's time information.
 *
 * This class will be extended by viewers who require time information to update
 * their content.
 *
 * <pre>
 * It provides three times of data:
 *   - start and end time of the trace (available)
 *   - start, end and duration of the current time window, ie the visible time range
 *   - start and end of the time range selected
 * </pre>
 *
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 */
public abstract class TmfTimeViewer extends TmfViewer implements ITmfTimeProvider, ITmfPinnable {

    /** Start time of trace */
    private long fStartTime;
    /** End time of trace */
    private long fEndTime;
    /** Start time of current time range */
    private long fWindowStartTime;
    /** End time of current time range */
    private long fWindowEndTime;
    /** Current begin time of selection range */
    private long fSelectionBeginTime;
    /** Current end of selection range */
    private long fSelectionEndTime;
    /** The trace that is displayed by this viewer */
    private ITmfTrace fTrace;
    /** A signal throttler for range updates */
    private final TmfSignalThrottler fTimeRangeSyncThrottle = new TmfSignalThrottler(this, 200);

    /**
     * Default constructor.
     */
    public TmfTimeViewer() {
        super();
    }

    /**
     * Constructor that initializes the parent of the viewer
     *
     * @param parent
     *            The parent composite that holds this viewer
     */
    public TmfTimeViewer(Composite parent) {
        this(parent, ""); //$NON-NLS-1$
    }

    /**
     * Constructor that initializes the parent of the viewer and that sets the
     * name of the viewer
     *
     * @param parent
     *            The parent composite that holds this viewer
     * @param name
     *            The name of the viewer
     */
    public TmfTimeViewer(Composite parent, String name) {
        init(parent, name);
    }

    // ------------------------------------------------------------------------
    // Getter/Setters
    // ------------------------------------------------------------------------

    /**
     * Sets the start time of the trace
     *
     * @param startTime
     *            The start time to set
     */
    protected void setStartTime(long startTime) {
        fStartTime = startTime;
    }

    /**
     * Sets the end time of the trace
     *
     * @param endTime
     *            The start time to set
     */
    protected void setEndTime(long endTime) {
        fEndTime = endTime;
    }

    /**
     * Sets the start time and end of the current time range window (visible
     * range)
     *
     * @param windowStartTime
     *            The start time to set
     * @param windowEndTime
     *            The start time to set
     * @since 1.0
     */
    protected void setWindowRange(long windowStartTime, long windowEndTime) {
        fWindowStartTime = windowStartTime;
        fWindowEndTime = windowEndTime;
    }

    /**
     * Sets the begin and end time of the selected range without sending the
     * {@link TmfSelectionRangeUpdatedSignal} signal.
     *
     * @param selectionBeginTime
     *            The begin time to set
     * @param selectionEndTime
     *            The end time to set
     *
     * @since 1.0
     */
    protected void setSelectionRange(long selectionBeginTime, long selectionEndTime) {
        fSelectionBeginTime = selectionBeginTime;
        fSelectionEndTime = selectionEndTime;
    }

    /**
     * Sets the trace that is displayed by this viewer.
     *
     * @param trace
     *            The trace to set
     */
    protected void setTrace(ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * Gets the trace that is displayed by this viewer.
     *
     * @return the trace
     * @since 3.3
     */
    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // ITmfTimeProvider
    // ------------------------------------------------------------------------

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public long getWindowStartTime() {
        return fWindowStartTime;
    }

    @Override
    public long getWindowEndTime() {
        return fWindowEndTime;
    }

    @Override
    public long getWindowDuration() {
        return getWindowEndTime() - getWindowStartTime();
    }

    @Override
    public long getSelectionBeginTime() {
        return fSelectionBeginTime;
    }

    @Override
    public long getSelectionEndTime() {
        return fSelectionEndTime;
    }

    @Override
    public void updateSelectionRange(final long currentBeginTime, final long currentEndTime) {
        if (fTrace != null) {
            setSelectionRange(currentBeginTime, currentEndTime);

            final ITmfTimestamp startTimestamp = TmfTimestamp.fromNanos(getSelectionBeginTime());
            final ITmfTimestamp endTimestamp = TmfTimestamp.fromNanos(getSelectionEndTime());

            TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(this, startTimestamp, endTimestamp, getTrace());
            broadcast(signal);
        }
    }

    @Override
    public void updateWindow(long windowStartTime, long windowEndTime) {

        setWindowRange(windowStartTime, windowEndTime);

        // Build the new time range; keep the current time
        TmfTimeRange timeRange = new TmfTimeRange(
                TmfTimestamp.fromNanos(getWindowStartTime()),
                TmfTimestamp.fromNanos(getWindowEndTime()));

        // Send the signal
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, timeRange, getTrace());
        fTimeRangeSyncThrottle.queue(signal);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * A Method to load a trace into the viewer.
     *
     * @param trace
     *            A trace to apply in the viewer
     */
    public void loadTrace(ITmfTrace trace) {
        fTrace = trace;
        if (trace == null) {
            return;
        }
        TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
        long selectionStart = ctx.getSelectionRange().getStartTime().toNanos();
        long selectionEnd = ctx.getSelectionRange().getEndTime().toNanos();
        TmfTimeRange windowRange = ctx.getWindowRange();

        long windowStartTime = windowRange.getStartTime().toNanos();
        long windowEndTime = windowRange.getEndTime().toNanos();
        long startTime = fTrace.getStartTime().toNanos();
        long endTime = fTrace.getEndTime().toNanos();

        setSelectionRange(selectionStart, selectionEnd);

        setStartTime(startTime);
        setWindowRange(windowStartTime, windowEndTime);
        setEndTime(endTime);
    }

    /**
     * Resets the content of the viewer
     */
    public void reset() {
        // Reset the internal data
        setSelectionRange(0, 0);
        setStartTime(0);
        setWindowRange(0, 0);
        setEndTime(0);
        setTrace(null);
    }

    @Override
    public synchronized void setPinned(ITmfTrace trace) {
        if (trace != null) {
            /* Ignore relevant inbound signals */
            TmfSignalManager.addIgnoredInboundSignal(this, TmfTraceOpenedSignal.class);
            TmfSignalManager.addIgnoredInboundSignal(this, TmfTraceSelectedSignal.class);

            if (!trace.equals(fTrace)) {
                loadTrace(trace);
            }
        } else {
            /* Handle relevant inbound signals */
            TmfSignalManager.removeIgnoredInboundSignal(this, TmfTraceOpenedSignal.class);
            TmfSignalManager.removeIgnoredInboundSignal(this, TmfTraceSelectedSignal.class);

            ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
            if (activeTrace != null && !activeTrace.equals(fTrace)) {
                loadTrace(activeTrace);
            } else {
                refresh();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Signal Handler
    // ------------------------------------------------------------------------

    /**
     * Signal handler for handling of the trace opened signal.
     *
     * @param signal
     *            The trace opened signal {@link TmfTraceOpenedSignal}
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace(getTrace());
    }

    /**
     * Signal handler for handling of the trace selected signal.
     *
     * @param signal
     *            The trace selected signal {@link TmfTraceSelectedSignal}
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace(getTrace());
        }
    }

    /**
     * Signal handler for handling of the trace closed signal.
     *
     * @param signal
     *            The trace closed signal {@link TmfTraceClosedSignal}
     */
    @TmfSignalHandler
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {

        if (signal == null || signal.getTrace() != fTrace) {
            return;
        }

        // Reset the internal data
        fTrace = null;
        reset();
    }

    /**
     * Signal handler for handling of the selected range signal.
     *
     * @param signal
     *            The {@link TmfSelectionRangeUpdatedSignal}
     * @since 1.0
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(@Nullable TmfSelectionRangeUpdatedSignal signal) {
        final ITmfTrace trace = fTrace;
        if (signal != null && (signal.getSource() != this) && (trace != null)) {
            TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
            long selectedTime = ctx.getSelectionRange().getStartTime().toNanos();
            long selectedEndTime = ctx.getSelectionRange().getEndTime().toNanos();
            setSelectionRange(selectedTime, selectedEndTime);
        }
    }

    /**
     * Signal handler for handling of the window range signal.
     *
     * @param signal
     *            The {@link TmfWindowRangeUpdatedSignal}
     * @since 1.0
     */
    @TmfSignalHandler
    public void windowRangeUpdated(@Nullable TmfWindowRangeUpdatedSignal signal) {
        final ITmfTrace trace = fTrace;
        if (signal != null && trace != null) {
            TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);

            // Validate the time range
            TmfTimeRange range = ctx.getWindowRange().getIntersection(trace.getTimeRange());
            if (range == null) {
                return;
            }

            if (signal.getSource() != this) {
                // Update the time range
                long windowStartTime = range.getStartTime().toNanos();
                long windowEndTime = range.getEndTime().toNanos();

                setWindowRange(windowStartTime, windowEndTime);
            }
        }
    }

    /**
     * Signal handler for handling of the trace range updated signal.
     *
     * @param signal
     *            The trace range signal {@link TmfTraceRangeUpdatedSignal}
     */
    @TmfSignalHandler
    public void traceRangeUpdated(@Nullable TmfTraceRangeUpdatedSignal signal) {
        if (signal == null || signal.getTrace() != fTrace) {
            return;
        }

        TmfTimeRange fullRange = signal.getRange();

        long traceStartTime = fullRange.getStartTime().toNanos();
        long traceEndTime = fullRange.getEndTime().toNanos();

        setStartTime(traceStartTime);
        setEndTime(traceEndTime);
    }

    /**
     * Signal handler for handling of the trace updated signal.
     *
     * @param signal
     *            The trace updated signal {@link TmfTraceUpdatedSignal}
     */
    @TmfSignalHandler
    public void traceUpdated(@Nullable TmfTraceUpdatedSignal signal) {
        if (signal == null || signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();

        long traceStartTime = fullRange.getStartTime().toNanos();
        long traceEndTime = fullRange.getEndTime().toNanos();

        setStartTime(traceStartTime);
        setEndTime(traceEndTime);
    }

}
