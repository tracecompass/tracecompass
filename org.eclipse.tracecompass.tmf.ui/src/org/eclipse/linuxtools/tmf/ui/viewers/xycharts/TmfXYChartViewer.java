/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Moved some methods to TmfTimeViewer
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfTimeViewer;
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
public abstract class TmfXYChartViewer extends TmfTimeViewer implements ITmfChartTimeProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The offset to apply to any x position. This offset ensures better
     * precision when converting long to double and back.
     */
    private long fTimeOffset;
    /** The SWT Chart reference */
    private Chart fSwtChart;
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
    public long getTimeOffset() {
        return fTimeOffset;
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
     * Signal handler for handling of the time synch signal.
     *
     * @param signal
     *            The time synch signal {@link TmfTimeSynchSignal}
     */
    @Override
    @TmfSignalHandler
    public void selectionRangeUpdated(TmfTimeSynchSignal signal) {
        super.selectionRangeUpdated(signal);
        if ((signal.getSource() != this) && (getTrace() != null)) {
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
    @Override
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        super.timeRangeUpdated(signal);
        updateContent();
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
