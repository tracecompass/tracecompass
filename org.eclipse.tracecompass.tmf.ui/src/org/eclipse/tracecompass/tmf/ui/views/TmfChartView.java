/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;

/**
 * Base class to be used with a chart viewer {@link TmfXYChartViewer}.
 * It is responsible to instantiate the viewer class and load the trace
 * into the viewer when the view is created.
 *
 * @author Bernd Hufmann
 */
public abstract class TmfChartView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The TMF XY Chart reference */
    private TmfXYChartViewer fChartViewer;
    /** The Trace reference */
    private ITmfTrace fTrace;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard Constructor
     *
     * @param viewName
     *            The view name
     */
    public TmfChartView(String viewName) {
        super(viewName);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the TMF XY chart viewer implementation.
     *
     * @return the TMF XY chart viewer {@link TmfXYChartViewer}
     */
    protected TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Sets the TMF XY chart viewer implementation.
     *
     * @param chartViewer
     *            The TMF XY chart viewer {@link TmfXYChartViewer}
     */
    protected void setChartViewer(TmfXYChartViewer chartViewer) {
        fChartViewer = chartViewer;
    }

    /**
     * Returns the ITmfTrace implementation
     *
     * @return the ITmfTrace implementation {@link ITmfTrace}
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Sets the ITmfTrace implementation
     *
     * @param trace
     *            The ITmfTrace implementation {@link ITmfTrace}
     */
    protected void setTrace(ITmfTrace trace) {
        fTrace = trace;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void createPartControl(Composite parent) {
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            setTrace(trace);
            loadTrace();
        }
    }

    @Override
    public void dispose() {
        if (fChartViewer != null) {
            fChartViewer.dispose();
        }
    }

    /**
     * Load the trace into view.
     */
    protected void loadTrace() {
        if (fChartViewer != null) {
            fChartViewer.loadTrace(fTrace);
        }
    }

}
