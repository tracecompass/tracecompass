/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson, École Polytechnique de Montréal, Draeger
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Moved some methods to TmfTimeViewer
 *   Patrick Tasse - Fix setFocus
 *   Ivan Grinenko - Add ability to set fixed range for Y axis.
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ICustomPaintListener;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.ITitle;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXYChartTimeAdapter;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXyUiUtils;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.XYAxis;
import org.eclipse.tracecompass.internal.tmf.ui.views.ITmfTimeNavigationProvider;
import org.eclipse.tracecompass.internal.tmf.ui.views.ITmfTimeZoomProvider;
import org.eclipse.tracecompass.internal.tmf.ui.views.ITmfZoomToSelectionProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.IImageSave;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfTimeViewer;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphScale;

import com.google.common.annotations.VisibleForTesting;

/**
 * Base class for a XY-Chart based on SWT chart. It provides a methods to define
 * zoom, selection and tool tip providers. It also provides call backs to be
 * notified by any changes caused by selection and zoom.
 *
 * @author Bernd Hufmann
 * @since 6.0
 */
public abstract class TmfXYChartViewer extends TmfTimeViewer implements ITmfChartTimeProvider, IImageSave, IAdaptable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static final int DEFAULT_SCALE_HEIGHT = 22;

    /** The color scheme for the chart */
    private @NonNull TimeGraphColorScheme fColorScheme = new TimeGraphColorScheme();
    /** The SWT Chart reference */
    private Chart fSwtChart;
    /** The X axis for the chart */
    private TimeGraphScale fTimeScaleCtrl;
    /** The time format */
    private @Nullable TimeFormat fTimeFormat = null;
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
    /**
     * Whether or not to send time alignment signals. This should be set to true for
     * viewers that are part of an aligned view.
     */
    private boolean fSendTimeAlignSignals = false;

    private final TmfXYChartTimeAdapter fDataProvider;

    private AxisRange fFixedYRange = null;

    private IStatusLineManager fStatusLineManager;
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
        Composite commonComposite = new Composite(parent, parent.getStyle()) {
            @Override
            public void redraw() {
                fSwtChart.redraw();
                fTimeScaleCtrl.redraw();
            }
        };
        commonComposite.addDisposeListener(e -> {
            fColorScheme.dispose();
        });
        commonComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).create());
        commonComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        fSwtChart = new Chart(commonComposite, SWT.NONE) {
            @Override
            public void addFocusListener(FocusListener listener) {
                fSwtChart.getPlotArea().getControl().addFocusListener(listener);
            }
            @Override
            public void removeFocusListener(FocusListener listener) {
                fSwtChart.getPlotArea().getControl().removeFocusListener(listener);
            }
            @Override
            public boolean setFocus() {
                return fSwtChart.getPlotArea().getControl().setFocus();
            }
            @Override
            public boolean forceFocus() {
                return fSwtChart.getPlotArea().getControl().forceFocus();
            }
        };
        fSwtChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fSwtChart.getPlotArea().addCustomPaintListener(new ICustomPaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                drawGridLines(e.gc);
            }

            @Override
            public boolean drawBehindSeries() {
                return true;
            }
        });

        fSwtChart.addPaintListener(e -> {
            Rectangle bounds = fSwtChart.getPlotArea().getBounds();
            int y = fTimeScaleCtrl.getLocation().y;
            fTimeScaleCtrl.setBounds(bounds.x, y, bounds.width, DEFAULT_SCALE_HEIGHT);
            fTimeScaleCtrl.redraw();
        });

        fTimeScaleCtrl = new TimeGraphScale(commonComposite, fColorScheme, SWT.BOTTOM);
        Color backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_BACKGROUND);
        fSwtChart.setBackground(backgroundColor);
        commonComposite.setBackground(backgroundColor);
        backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.BACKGROUND);
        fSwtChart.getPlotArea().setBackground(backgroundColor);
        fSwtChart.setForeground(fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
        fTimeScaleCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fTimeScaleCtrl.setHeight(DEFAULT_SCALE_HEIGHT);
        ((Composite) fSwtChart.getPlotArea()).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ((Composite) fSwtChart.getPlotArea()).setFocus();
            }
        });

        IAxis xAxis = fSwtChart.getAxisSet().getXAxis(0);
        IAxis yAxis = fSwtChart.getAxisSet().getYAxis(0);

        /* Set the title/labels, or hide them if they are not provided */
        initTitle(title, fSwtChart.getTitle());

        initTitle(null, fSwtChart.getTitle());

        xAxis.getTick().setVisible(false);
        yAxis.getTick().setForeground(fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
        initTitle(yLabel, yAxis.getTitle());

        fMouseSelectionProvider = new TmfMouseSelectionProvider(this);
        fMouseDragZoomProvider = new TmfMouseDragZoomProvider(this);
        fMouseWheelZoomProvider = new TmfMouseWheelZoomProvider(this);
        fToolTipProvider = new TmfSimpleTooltipProvider(this);
        fMouseDragProvider = new TmfMouseDragProvider(this);

        fSwtChart.addDisposeListener((e) -> {
            internalDispose();
        });

        fDataProvider = new TmfXYChartTimeAdapter(this);
        fTimeScaleCtrl.setTimeProvider(fDataProvider);
        updateTimeFormat();
    }

    private void initTitle(String label, ITitle titleCtrl) {
        titleCtrl.setForeground(fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND));
        if (label == null) {
            titleCtrl.setVisible(false);
        } else {
            titleCtrl.setText(label);
        }
    }

    // ------------------------------------------------------------------------
    // Getter/Setters
    // ------------------------------------------------------------------------

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
    public Chart getSwtChart() {
        return fSwtChart;
    }

    /**
     * Sets a mouse selection provider. An existing provider will be disposed. Use
     * <code>null</code> to disable the mouse selection provider.
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
     * Sets a mouse drag zoom provider. An existing provider will be disposed. Use
     * <code>null</code> to disable the mouse drag zoom provider.
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
     * Sets a mouse wheel zoom provider. An existing provider will be disposed. Use
     * <code>null</code> to disable the mouse wheel zoom provider.
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
     * Sets a tooltip provider. An existing provider will be disposed. Use
     * <code>null</code> to disable the tooltip provider.
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
     * Sets a mouse drag provider. An existing provider will be disposed. Use
     * <code>null</code> to disable the mouse drag provider.
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
    public long getTimeOffset() {
        return getWindowStartTime() - 1;
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
        if (!fSwtChart.isDisposed()) {
            fSwtChart.dispose();
        }
    }

    private void internalDispose() {
        super.dispose();

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
    @Override
    public void loadTrace(ITmfTrace trace) {
        super.loadTrace(trace);
        clearContent();
        updateContent();
    }

    /**
     * Resets the content of the viewer
     */
    @Override
    public void reset() {
        super.reset();
        setStartTime(0);
        setEndTime(0);
        clearContent();
    }

    /**
     * Method to implement to update the chart content.
     */
    protected abstract void updateContent();

    /**
     * Returns whether or not this chart viewer is dirty. The viewer is considered
     * dirty if it has yet to completely update its model.
     *
     * This method is meant to be used by tests in order to know when it is safe to
     * proceed.
     *
     * @return true if the time graph view has yet to completely update its model,
     *         false otherwise
     */
    @VisibleForTesting
    public boolean isDirty() {
        if (getTrace() == null) {
            return false;
        }

        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        long startTime = ctx.getWindowRange().getStartTime().toNanos();
        long endTime = ctx.getWindowRange().getEndTime().toNanos();

        // If the chart viewer hasn't updated all the way to the end of
        // the window range then it's dirty. A refresh should happen later.
        return (getWindowStartTime() != startTime || getWindowEndTime() != endTime);
    }

    /**
     * Draw the grid lines
     *
     * @param bounds
     *            The bounds of the control
     * @param gc
     *            Graphics context
     */
    private void drawGridLines(GC gc) {
        Rectangle bounds = fSwtChart.getPlotArea().getBounds();
        Color foreground = fSwtChart.getAxisSet().getXAxis(0).getGrid().getForeground();
        gc.setForeground(foreground);
        gc.setAlpha(foreground.getAlpha());
        gc.setLineStyle(SWT.LINE_DOT);
        for (int x : fTimeScaleCtrl.getTickList()) {
            gc.drawLine(x, 0, x,  bounds.height);
        }
        gc.setAlpha(255);
    }

    /**
     * Sets the time format, or null to use the Time Format 'Date and Time
     * format' preference (default).
     *
     * @param timeFormat
     *            the {@link TimeFormat} used to display timestamps
     */
    protected void setTimeFormat(TimeFormat timeFormat) {
        fTimeFormat = timeFormat;
        if (fSwtChart != null) {
            updateTimeFormat();
            fSwtChart.getAxisSet().adjustRange();
            fSwtChart.redraw();
        }
    }

    // ------------------------------------------------------------------------
    // Signal Handler
    // ------------------------------------------------------------------------

    /**
     * Signal handler for handling of the time synch signal.
     *
     * @param signal
     *            The time synch signal {@link TmfSelectionRangeUpdatedSignal}
     */
    @Override
    @TmfSignalHandler
    public void selectionRangeUpdated(TmfSelectionRangeUpdatedSignal signal) {
        super.selectionRangeUpdated(signal);
        if (signal != null && (signal.getSource() != this) && (getTrace() != null)) {
            if (fMouseSelectionProvider != null) {
                fMouseSelectionProvider.refresh();
            }
        }
    }

    /**
     * Signal handler for handling of the window range signal.
     *
     * @param signal
     *            The {@link TmfWindowRangeUpdatedSignal}
     */
    @Override
    @TmfSignalHandler
    public void windowRangeUpdated(TmfWindowRangeUpdatedSignal signal) {
        super.windowRangeUpdated(signal);
        updateContent();
    }

    /**
     * Signal handler for handling the signal that notifies about an updated
     * timestamp format.
     *
     * @param signal
     *            The trace updated signal {@link TmfTimestampFormatUpdateSignal}
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        updateTimeFormat();
        fSwtChart.getAxisSet().adjustRange();
        fSwtChart.redraw();
    }

    /**
     * Sets new fixed range for the Y axis.
     *
     * @param yRange
     *            new fixed range for the Y axis or {@code null} to
     *            make float again. If not {@code null} then
     *            {@link IAxis#adjustRange} is not called for the Y
     *            axis on updates.
     */
    public void setFixedYRange(@Nullable AxisRange yRange) {
        fFixedYRange = yRange;
        updateContent();
    }

    /**
     * Gets current fixed range for the Y axis.
     *
     * @return Current fixed range or {@code null} if the range is not fixed.
     */
    public @Nullable AxisRange getFixedYRange() {
        return fFixedYRange;
    }

    // ------------------------------------------------------------------------
    // IAdaptable Interface
    // ------------------------------------------------------------------------
    /**
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == ITmfTimeNavigationProvider.class) {
            return (T) getTimeNavigator();
        }
        if (adapter == ITmfTimeZoomProvider.class) {
            return (T) getTimeZoomProvider();
        }
        if (adapter == ITmfZoomToSelectionProvider.class ) {
            return (T) getZoomToSelectionProvider();
        }
        return null;
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
            for (IAxis axis : fSwtChart.getAxisSet().getAxes()) {
                axis.setRange(new Range(0, 1));
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

    /**
     * Get the offset of the point area, relative to the XY chart viewer control. We
     * consider the point area to be from where the first point could be drawn to
     * where the last point could be drawn.
     *
     * @return the offset in pixels
     *
     */
    public int getPointAreaOffset() {

        int pixelCoordinate = 0;
        IAxis[] xAxes = getSwtChart().getAxisSet().getXAxes();
        if (xAxes.length > 0) {
            IAxis axis = xAxes[0];
            pixelCoordinate = axis.getPixelCoordinate(axis.getRange().lower);
        }
        return getSwtChart().toControl(((Composite) fSwtChart.getPlotArea()).toDisplay(pixelCoordinate, 0)).x;
    }

    /**
     * Get the width of the point area. We consider the point area to be from where
     * the first point could be drawn to where the last point could be drawn. The
     * point area differs from the plot area because there might be a gap between
     * where the plot area start and where the fist point is drawn. This also
     * matches the width that the use can select.
     *
     * @return the width in pixels
     *
     */
    public int getPointAreaWidth() {
        IAxis[] xAxes = getSwtChart().getAxisSet().getXAxes();
        if (xAxes.length > 0) {
            IAxis axis = xAxes[0];
            int x1 = getPointAreaOffset();
            int x2 = axis.getPixelCoordinate(axis.getRange().upper);
            x2 = getSwtChart().toControl(((Composite) fSwtChart.getPlotArea()).toDisplay(x2, 0)).x;
            int width = x2 - x1;
            return width;
        }

        return getSwtChart().getPlotArea().getSize().x;
    }

    /**
     * Sets whether or not to send time alignment signals. This should be set to
     * true for viewers that are part of an aligned view.
     *
     * @param sendTimeAlignSignals
     *            whether or not to send time alignment signals
     */
    public void setSendTimeAlignSignals(boolean sendTimeAlignSignals) {
        fSendTimeAlignSignals = sendTimeAlignSignals;
    }

    /**
     * Returns whether or not to send time alignment signals.
     *
     * @return whether or not to send time alignment signals.
     */
    public boolean isSendTimeAlignSignals() {
        return fSendTimeAlignSignals;
    }

    @Override
    public void saveImage(String filename, int format) {
        getSwtChart().save(filename, format);
    }

    /**
     * Set the status bar manager
     *
     * @param statusLineManager
     *            Status bar manager
     */
    public void setStatusLineManager(IStatusLineManager statusLineManager) {
        if (fStatusLineManager != null && statusLineManager == null) {
            fStatusLineManager.setMessage(null);
        }
        fStatusLineManager = statusLineManager;
    }

    /**
     * Update the status line to include time selection
     *
     * @param startTime
     *            Selection start time
     * @param endTime
     *            Selection end time
     * @param cursorTime
     *            Cursor time
     */
    public void updateStatusLine(long startTime, long endTime, long cursorTime) {
        TimeFormat timeFormat = fTimeScaleCtrl.getTimeProvider().getTimeFormat().convert();
        boolean isCalendar = timeFormat == TimeFormat.CALENDAR;

        StringBuilder message = new StringBuilder();
        String spaces = "     "; //$NON-NLS-1$
        if (cursorTime >= 0) {
            message.append("T: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(cursorTime + getTimeOffset()) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(cursorTime + getTimeOffset(), timeFormat, Resolution.NANOSEC));
            message.append(spaces);
        }

        if (startTime == endTime) {
            message.append("T1: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(startTime + getTimeOffset()) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(startTime + getTimeOffset(), timeFormat, Resolution.NANOSEC));
        } else {
            message.append("T1: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(startTime + getTimeOffset()) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(startTime + getTimeOffset(), timeFormat, Resolution.NANOSEC));
            message.append(spaces);
            message.append("T2: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(endTime + getTimeOffset()) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(endTime + getTimeOffset(), timeFormat, Resolution.NANOSEC));
            message.append(spaces);
            message.append("\u0394: " + FormatTimeUtils.formatDelta(endTime - startTime, timeFormat, Resolution.NANOSEC)); //$NON-NLS-1$
        }

        fStatusLineManager.setMessage(message.toString());
    }

    private ITmfTimeZoomProvider getTimeZoomProvider() {
        return (zoomIn, useMousePosition) -> {
            Chart chart = getSwtChart();
            if (chart == null) {
                return;
            }
            XYAxis xAxis = XYAxis.create(chart.getAxisSet().getXAxis(0));
            Point cursorDisplayLocation = getDisplay().getCursorLocation();
            Point cursorControlLocation = ((Composite) fSwtChart.getPlotArea()).toControl(cursorDisplayLocation);
            Point cursorParentLocation = ((Composite) fSwtChart.getPlotArea()).getParent().toControl(cursorDisplayLocation);
            Rectangle controlBounds = getSwtChart().getPlotArea().getBounds();
            // check the X axis only
            if (!controlBounds.contains(cursorParentLocation.x, controlBounds.y)) {
                return;
            }
            if (useMousePosition) {
                TmfXyUiUtils.zoom(this, xAxis, zoomIn, cursorControlLocation.x);
            } else {
                TmfXyUiUtils.zoom(this, xAxis, zoomIn);
            }
        };
    }

    private ITmfTimeNavigationProvider getTimeNavigator() {
        return left -> {
            Chart chart = getSwtChart();
            if (chart != null) {
                TmfXyUiUtils.horizontalScroll(this, XYAxis.create(chart.getAxisSet().getXAxis(0)), left);
            }
        };
    }

    private ITmfZoomToSelectionProvider getZoomToSelectionProvider() {
        return () -> {
            long selBegin = getSelectionBeginTime();
            long selEnd = getSelectionEndTime();
            if (selBegin != selEnd) {
                updateWindow(selBegin, selEnd);
            }
        };
    }

    private void updateTimeFormat() {
        if (fTimeFormat == null) {
            String datime = TmfTimePreferences.getPreferenceMap().get(ITmfTimePreferencesConstants.DATIME);
            if (ITmfTimePreferencesConstants.TIME_ELAPSED_FMT.equals(datime)) {
                fDataProvider.setTimeFormat(TimeFormat.RELATIVE);
            } else {
                fDataProvider.setTimeFormat(TimeFormat.CALENDAR);
            }
        } else {
            fDataProvider.setTimeFormat(fTimeFormat);
        }
    }
}
