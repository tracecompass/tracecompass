/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
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
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;

/**
 * Base class for a XY-Chart based on SWT chart. It provides a methods to define
 * zoom, selection and tool tip providers. It also provides call backs to be
 * notified by any changes caused by selection and zoom.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public abstract class TmfXYChartViewer extends TmfViewer implements ITmfChartTimeProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The offset to apply to any x position. This offset ensures better
     * precision when converting long to double and back.
     */
    private long fTimeOffset;
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
    /** The SWT Chart reference */
    private Chart fSwtChart;
    /** A signal throttler for range updates */
    private final TmfSignalThrottler fTimeRangeSyncThrottle = new TmfSignalThrottler(this, 200);
    /** The mouse selection provider */
    private TmfBaseProvider fMouseSelectionProvider;
    /** The mouse drag zoom provider */
    private TmfBaseProvider fMouseDragZoomProvider;
    /** The mouse wheel zoom provider */
    private TmfBaseProvider fMouseWheelZoomProvider;
    /** The tooltip provider */
    private TmfBaseProvider fToolTipProvider;
    /** The middle mouse drag provider */
    private TmfBaseProvider fMouseDragProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TmfXYChartViewer.
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The title of the viewer
     * @param xLabel
     *            The label of the xAxis
     * @param yLabel
     *            The label of the yAXIS
     */
    public TmfXYChartViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title);
        fSwtChart = new Chart(parent, SWT.NONE);

        IAxis xAxis = fSwtChart.getAxisSet().getXAxis(0);
        IAxis yAxis = fSwtChart.getAxisSet().getYAxis(0);

        /* Set the title/labels, or hide them if they are not provided */
        if (title == null) {
            fSwtChart.getTitle().setVisible(false);
        } else {
            fSwtChart.getTitle().setText(title);
        }
        if (xLabel == null) {
            xAxis.getTitle().setVisible(false);
        } else {
            xAxis.getTitle().setText(xLabel);
        }
        if (yLabel == null) {
            yAxis.getTitle().setVisible(false);
        } else {
            yAxis.getTitle().setText(yLabel);
        }

        fMouseSelectionProvider = new TmfMouseSelectionProvider(this);
        fMouseDragZoomProvider = new TmfMouseDragZoomProvider(this);
        fMouseWheelZoomProvider = new TmfMouseWheelZoomProvider(this);
        fToolTipProvider = new TmfSimpleTooltipProvider(this);
        fMouseDragProvider = new TmfMouseDragProvider(this);
    }

    // ------------------------------------------------------------------------
    // Getter/Setters
    // ------------------------------------------------------------------------
    /**
     * Sets the time offset to apply.
     * @see ITmfChartTimeProvider#getTimeOffset()
     *
     * @param timeOffset
     *            The time offset to apply
     */
    protected void setTimeOffset(long timeOffset) {
        fTimeOffset = timeOffset;
    }

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
     * Sets the start time of the current time range window
     *
     * @param windowStartTime
     *            The start time to set
     */
    protected void setWindowStartTime(long windowStartTime) {
        fWindowStartTime = windowStartTime;
    }

    /**
     * Sets the end time of the current time range window
     *
     * @param windowEndTime
     *            The start time to set
     */
    protected void setWindowEndTime(long windowEndTime) {
        fWindowEndTime = windowEndTime;
    }

    /**
     * Sets the start time of the current time range window
     *
     * @param windowDuration
     *            The start time to set
     */
    protected void setWindowDuration(long windowDuration) {
        fWindowDuration = windowDuration;
    }

    /**
     * Sets the begin time of the selection range.
     *
     * @param selectionBeginTime
     *            The begin time to set
     */
    protected void setSelectionBeginTime(long selectionBeginTime) {
        fSelectionBeginTime = selectionBeginTime;
    }

    /**
     * Sets the end time of the selection range.
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

    /**
     * Sets the SWT Chart reference
     *
     * @param chart
     *            The SWT chart to set.
     */
    protected void setSwtChart(Chart chart) {
        fSwtChart = chart;
    }

    /**
     * Gets the SWT Chart reference
     *
     * @return the SWT chart to set.
     */
    protected Chart getSwtChart() {
        return fSwtChart;
    }

    /**
     * Sets a mouse selection provider. An existing provider will be
     * disposed. Use <code>null</code> to disable the mouse selection provider.
     *
     * @param provider
     *            The selection provider to set
     */
    public void setSelectionProvider(TmfBaseProvider provider) {
        if (fMouseSelectionProvider != null) {
            fMouseSelectionProvider.dispose();
        }
        fMouseSelectionProvider = provider;
    }

    /**
     * Sets a mouse drag zoom provider. An existing provider will be
     * disposed. Use <code>null</code> to disable the mouse drag zoom provider.
     *
     * @param provider
     *            The mouse drag zoom provider to set
     */
    public void setMouseDragZoomProvider(TmfBaseProvider provider) {
        if (fMouseDragZoomProvider != null) {
            fMouseDragZoomProvider.dispose();
        }
        fMouseDragZoomProvider = provider;
    }

    /**
     * Sets a mouse wheel zoom provider. An existing provider will be
     * disposed. Use <code>null</code> to disable the mouse wheel zoom
     * provider.
     *
     * @param provider
     *            The mouse wheel zoom provider to set
     */
    public void setMouseWheelZoomProvider(TmfBaseProvider provider) {
        if (fMouseWheelZoomProvider != null) {
            fMouseWheelZoomProvider.dispose();
        }
        fMouseWheelZoomProvider = provider;
    }

    /**
     * Sets a tooltip provider. An existing provider will be
     * disposed. Use <code>null</code> to disable the tooltip provider.
     *
     * @param provider
     *            The tooltip provider to set
     */
    public void setTooltipProvider(TmfBaseProvider provider) {
        if (fToolTipProvider != null) {
            fToolTipProvider.dispose();
        }
        fToolTipProvider = provider;
    }

    /**
     * Sets a mouse drag provider. An existing provider will be
     * disposed. Use <code>null</code> to disable the mouse drag provider.
     *
     * @param provider
     *            The mouse drag provider to set
     */
    public void setMouseDrageProvider(TmfBaseProvider provider) {
        if (fMouseDragProvider != null) {
            fMouseDragProvider.dispose();
        }
        fMouseDragProvider = provider;
    }

    // ------------------------------------------------------------------------
    // ITmfChartTimeProvider
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
    public long getTimeOffset() {
        return fTimeOffset;
    }

    @Override
    public void updateSelectionRange(final long currentBeginTime, final long currentEndTime) {
        if (fTrace != null) {
            setSelectionBeginTime(currentBeginTime);
            setSelectionEndTime(currentEndTime);

            final ITmfTimestamp startTimestamp = new TmfTimestamp(fSelectionBeginTime, ITmfTimestamp.NANOSECOND_SCALE);
            final ITmfTimestamp endTimestamp = new TmfTimestamp(fSelectionEndTime, ITmfTimestamp.NANOSECOND_SCALE);

            TmfTimeSynchSignal signal = new TmfTimeSynchSignal(TmfXYChartViewer.this, startTimestamp, endTimestamp);
            broadcast(signal);
        }
    }

    @Override
    public void updateWindow(long windowStartTime, long windowEndTime) {

        setWindowStartTime(windowStartTime);
        setWindowEndTime(windowEndTime);
        fWindowDuration = windowEndTime - windowStartTime;

        // Build the new time range; keep the current time
        TmfTimeRange timeRange = new TmfTimeRange(
                new TmfTimestamp(fWindowStartTime, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(fWindowEndTime, ITmfTimestamp.NANOSECOND_SCALE));

        // Send the  signal
        TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, timeRange);
        fTimeRangeSyncThrottle.queue(signal);
    }

    // ------------------------------------------------------------------------
    // ITmfViewer interface
    // ------------------------------------------------------------------------
    @Override
    public Control getControl() {
        return fSwtChart;
    }

    @Override
    public void refresh() {
        fSwtChart.redraw();
    }

    // ------------------------------------------------------------------------
    // TmfComponent
    // ------------------------------------------------------------------------
    @Override
    public void dispose() {
        super.dispose();
        fSwtChart.dispose();

        if (fMouseSelectionProvider != null) {
            fMouseSelectionProvider.dispose();
        }

        if (fMouseDragZoomProvider != null) {
            fMouseDragZoomProvider.dispose();
        }

        if (fMouseWheelZoomProvider != null) {
            fMouseWheelZoomProvider.dispose();
        }

        if (fToolTipProvider != null) {
            fToolTipProvider.dispose();
        }

        if (fMouseDragProvider != null) {
            fMouseDragProvider.dispose();
        }
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
        long windowStartTime = TmfTraceManager.getInstance().getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long startTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long endTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        setSelectionBeginTime(timestamp);
        setSelectionEndTime(timestamp);
        setStartTime(startTime);
        setWindowStartTime(windowStartTime);
        setWindowDuration(fTrace.getInitialRangeOffset().getValue());
        setEndTime(endTime);
        setWindowEndTime(windowStartTime + getWindowDuration());
        clearContent();
        updateContent();
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
        clearContent();
    }

    /**
     * Method to implement to update the chart content.
     */
    protected abstract void updateContent();

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
     * Signal handler for handling of the time synch signal.
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
            if (fMouseSelectionProvider != null) {
                fMouseSelectionProvider.refresh();
            }
        }
    }

    /**
     * Signal handler for handling of the time range synch signal.
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
        updateContent();
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

    /**
     * Signal handler for handling the signal that notifies about an updated
     * timestamp format.
     *
     * @param signal
     *            The trace updated signal
     *            {@link TmfTimestampFormatUpdateSignal}
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        fSwtChart.getAxisSet().adjustRange();
        fSwtChart.redraw();
    }

    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------

    /**
     * Clears the view content.
     */
    protected void clearContent() {
        if (!fSwtChart.isDisposed()) {
            ISeriesSet set = fSwtChart.getSeriesSet();
            ISeries[] series = set.getSeries();
            for (int i = 0; i < series.length; i++) {
                set.deleteSeries(series[i].getId());
            }
            fSwtChart.redraw();
        }
    }

    /**
     * Returns the current or default display.
     *
     * @return the current or default display
     */
    protected static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

}
