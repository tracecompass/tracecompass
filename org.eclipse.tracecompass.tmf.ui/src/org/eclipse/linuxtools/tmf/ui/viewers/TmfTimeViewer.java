/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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

package org.eclipse.linuxtools.tmf.ui.viewers;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.swt.widgets.Composite;

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
 * @since 3.0
 */
public abstract class TmfTimeViewer extends TmfViewer implements ITmfTimeProvider {

    /** Start time of trace */
    private long fStartTime;
    /** End time of trace */
    private long fEndTime;
    /** Start time of current time range */
    private long fWindowStartTime;
    /** End time of current time range */
    private long fWindowEndTime;
    /** Duration of current time range */
    private long fWindowDuration;
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
     * Sets the start time of the current time range window (visible range)
     *
     * @param windowStartTime
     *            The start time to set
     */
    protected void setWindowStartTime(long windowStartTime) {
        fWindowStartTime = windowStartTime;
    }

    /**
     * Sets the end time of the current time range window (visible range)
     *
     * @param windowEndTime
     *            The start time to set
     */
    protected void setWindowEndTime(long windowEndTime) {
        fWindowEndTime = windowEndTime;
    }

    /**
     * Sets the duration of the current time range window (visible range)
     *
     * @param windowDuration
     *            The window duration
     */
    protected void setWindowDuration(long windowDuration) {
        fWindowDuration = windowDuration;
    }

    /**
     * Sets the begin time of the selected range.
     *
     * @param selectionBeginTime
     *            The begin time to set
     */
    protected void setSelectionBeginTime(long selectionBeginTime) {
        fSelectionBeginTime = selectionBeginTime;
    }

    /**
     * Sets the end time of the selected range.
     *
     * @param selectionEndTime
     *            The end time to set
     */
    protected void setSelectionEndTime(long selectionEndTime) {
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
     */
    protected ITmfTrace getTrace() {
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
        return fWindowDuration;
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
            setSelectionBeginTime(currentBeginTime);
            setSelectionEndTime(currentEndTime);

            final ITmfTimestamp startTimestamp = new TmfTimestamp(getSelectionBeginTime(), ITmfTimestamp.NANOSECOND_SCALE);
            final ITmfTimestamp endTimestamp = new TmfTimestamp(getSelectionEndTime(), ITmfTimestamp.NANOSECOND_SCALE);

            TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, startTimestamp, endTimestamp);
            broadcast(signal);
        }
    }

    @Override
    public void updateWindow(long windowStartTime, long windowEndTime) {

        setWindowStartTime(windowStartTime);
        setWindowEndTime(windowEndTime);
        setWindowDuration(windowEndTime - windowStartTime);

        // Build the new time range; keep the current time
        TmfTimeRange timeRange = new TmfTimeRange(
                new TmfTimestamp(getWindowStartTime(), ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(getWindowEndTime(), ITmfTimestamp.NANOSECOND_SCALE));

        // Send the signal
        TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, timeRange);
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

        long timestamp = TmfTraceManager.getInstance().getSelectionBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        TmfTimeRange currentRange = TmfTraceManager.getInstance().getCurrentRange();
        long windowStartTime = currentRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long windowEndTime = currentRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long windowDuration = windowEndTime - windowStartTime;
        long startTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long endTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        setSelectionBeginTime(timestamp);
        setSelectionEndTime(timestamp);
        setStartTime(startTime);
        setWindowStartTime(windowStartTime);
        setWindowEndTime(windowEndTime);
        setWindowDuration(windowDuration);
        setEndTime(endTime);
    }

    /**
     * Resets the content of the viewer
     */
    public void reset() {
        // Reset the internal data
        setSelectionBeginTime(0);
        setSelectionEndTime(0);
        setStartTime(0);
        setWindowStartTime(0);
        setWindowDuration(0);
        setEndTime(0);
        setWindowEndTime(0);
        setTrace(null);
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
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Reset the internal data
        fTrace = null;
        reset();
    }

    /**
     * Signal handler for handling of the time synch signal, ie the selected range.
     *
     * @param signal
     *            The time synch signal {@link TmfTimeSynchSignal}
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(TmfTimeSynchSignal signal) {
        if ((signal.getSource() != this) && (fTrace != null)) {
            ITmfTimestamp selectedTime = signal.getBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
            ITmfTimestamp selectedEndTime = signal.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
            setSelectionBeginTime(selectedTime.getValue());
            setSelectionEndTime(selectedEndTime.getValue());
        }
    }

    /**
     * Signal handler for handling of the time range synch signal, ie the visible range.
     *
     * @param signal
     *            The time range synch signal {@link TmfRangeSynchSignal}
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {

        if (fTrace != null) {
            // Validate the time range
            TmfTimeRange range = signal.getCurrentRange().getIntersection(fTrace.getTimeRange());
            if (range == null) {
                return;
            }

            if (signal.getSource() != this) {
                // Update the time range
                long windowStartTime = range.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long windowEndTime = range.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long windowDuration = windowEndTime - windowStartTime;

                setWindowStartTime(windowStartTime);
                setWindowEndTime(windowEndTime);
                setWindowDuration(windowDuration);
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
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        TmfTimeRange fullRange = signal.getRange();

        long traceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long traceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

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
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();
        long traceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long traceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        setStartTime(traceStartTime);
        setEndTime(traceEndTime);
    }

}
