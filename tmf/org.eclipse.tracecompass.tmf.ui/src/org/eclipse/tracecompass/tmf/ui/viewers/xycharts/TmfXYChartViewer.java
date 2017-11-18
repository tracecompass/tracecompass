/**********************************************************************
 * Copyright (c) 2013, 2016 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Moved some methods to TmfTimeViewer
 *   Patrick Tasse - Fix setFocus
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xycharts.TmfXYChartTimeAdapter;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfTimeViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphScale;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import com.google.common.annotations.VisibleForTesting;

/**
 * Base class for a XY-Chart based on SWT chart. It provides a methods to define
 * zoom, selection and tool tip providers. It also provides call backs to be
 * notified by any changes caused by selection and zoom.
 *
 * @author Bernd Hufmann
 */
public abstract class TmfXYChartViewer extends TmfTimeViewer implements ITmfChartTimeProvider {

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

    private final ITimeDataProvider fDataProvider;
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
            public boolean setFocus() {
                return fSwtChart.getPlotArea().setFocus();
            }
        };
        fSwtChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        ((IPlotArea) fSwtChart.getPlotArea()).addCustomPaintListener(new ICustomPaintListener() {

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
        fSwtChart.setForeground(fColorScheme.getColor(TimeGraphColorScheme.FOREGROUND));
        fTimeScaleCtrl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        fTimeScaleCtrl.setHeight(DEFAULT_SCALE_HEIGHT);
        fSwtChart.getPlotArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                fSwtChart.getPlotArea().setFocus();
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
     * @since 3.2
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
     * @since 2.2
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
     * @since 2.0
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
     * @since 1.0
     */
    public int getPointAreaOffset() {

        int pixelCoordinate = 0;
        IAxis[] xAxes = getSwtChart().getAxisSet().getXAxes();
        if (xAxes.length > 0) {
            IAxis axis = xAxes[0];
            pixelCoordinate = axis.getPixelCoordinate(axis.getRange().lower);
        }
        return getSwtChart().toControl(getSwtChart().getPlotArea().toDisplay(pixelCoordinate, 0)).x;
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
     * @since 1.0
     */
    public int getPointAreaWidth() {
        IAxis[] xAxes = getSwtChart().getAxisSet().getXAxes();
        if (xAxes.length > 0) {
            IAxis axis = xAxes[0];
            int x1 = getPointAreaOffset();
            int x2 = axis.getPixelCoordinate(axis.getRange().upper);
            x2 = getSwtChart().toControl(getSwtChart().getPlotArea().toDisplay(x2, 0)).x;
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
     * @since 1.0
     */
    public void setSendTimeAlignSignals(boolean sendTimeAlignSignals) {
        fSendTimeAlignSignals = sendTimeAlignSignals;
    }

    /**
     * Returns whether or not to send time alignment signals.
     *
     * @return whether or not to send time alignment signals.
     * @since 1.0
     */
    public boolean isSendTimeAlignSignals() {
        return fSendTimeAlignSignals;
    }
}
