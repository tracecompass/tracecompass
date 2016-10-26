/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - New request added to update the statistics from the selected time range
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views.statistics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ITmfViewer;
import org.eclipse.tracecompass.tmf.ui.views.ITmfAllowMultiple;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.tabsview.TmfViewerFolder;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
 *
 * @author Mathieu Denis
 */
public class TmfStatisticsViewImpl extends TmfView implements ITmfAllowMultiple {

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /**
     * The viewer that builds the columns to show the statistics.
     */
    protected final TmfViewerFolder fStatsViewers;

    /**
     * Stores a reference to the selected trace.
     */
    private ITmfTrace fTrace;

    /**
     * Constructor of a statistics view.
     *
     * @param viewName The name to give to the view.
     */
    public TmfStatisticsViewImpl(String viewName) {
        super(viewName);
        /*
         * Create a fake parent for initialization purpose, than set the parent
         * as soon as createPartControl is called.
         */
        Composite temporaryParent = new Shell();
        fStatsViewers = new TmfViewerFolder(temporaryParent);
    }

    /**
     * Default constructor.
     */
    public TmfStatisticsViewImpl() {
        this(TMF_STATISTICS_VIEW);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fStatsViewers.setParent(parent);
        createStatisticsViewers();

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        fStatsViewers.dispose();
    }

    /**
     * Handler called when an trace is opened.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        /*
         * Dispose the current viewer and adapt the new one to the trace
         * type of the trace opened
         */
        fStatsViewers.clear();
        // Update the current trace
        fTrace = signal.getTrace();
        createStatisticsViewers();
        fStatsViewers.layout();
    }

    /**
     * Handler called when an trace is selected. Checks if the trace
     * has changed and requests the selected trace if it has not yet been
     * cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        // Does not reload the same trace if already opened
        if (signal.getTrace() != fTrace) {
            /*
             * Dispose the current viewer and adapt the new one to the trace
             * type of the trace selected
             */
            fStatsViewers.clear();
            // Update the current trace
            fTrace = signal.getTrace();
            createStatisticsViewers();
            fStatsViewers.layout();

            TmfTraceRangeUpdatedSignal updateSignal = new TmfTraceRangeUpdatedSignal(this, fTrace, fTrace.getTimeRange());

            for (ITmfViewer viewer : fStatsViewers.getViewers()) {
                TmfStatisticsViewer statsViewer = (TmfStatisticsViewer) viewer;
                statsViewer.sendPartialRequestOnNextUpdate();
                statsViewer.traceRangeUpdated(updateSignal);
            }
        } else {
            /*
             * If the same trace is reselected, sends a notification to
             * the viewers to make sure they reload correctly their partial
             * event count.
             */
            for (ITmfViewer viewer : fStatsViewers.getViewers()) {
                TmfStatisticsViewer statsViewer = (TmfStatisticsViewer) viewer;
                // Will update the partial event count if needed.
                statsViewer.sendPartialRequestOnNextUpdate();
            }
        }
    }

    /**
     * @param signal the incoming signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }

        // Clear the internal data
        fTrace = null;

        // Clear the UI widgets
        fStatsViewers.clear();  // Also cancels ongoing requests
        createStatisticsViewers();
        fStatsViewers.layout();
    }

    @Override
    public void setFocus() {
        fStatsViewers.setFocus();
    }

    /**
     * Creates the statistics viewers for all traces in an experiment and
     * populates a viewer folder. Each viewer is placed in a different tab and
     * the first one is selected automatically.
     *
     * It uses the extension point that defines the statistics viewer to build
     * from the trace type. If no viewer is defined, another tab won't be
     * created, since the global viewer already contains all the basic
     * statistics. If there is no trace selected, a global statistics viewer will
     * still be created.
     */
    protected void createStatisticsViewers() {
        // Default style for the tabs that will be created
        int defaultStyle = SWT.NONE;

        // The folder composite that will contain the tabs
        Composite folder = fStatsViewers.getParentFolder();

        // Instantiation of the global viewer
        if (fTrace != null) {
            // Shows the name of the trace in the global tab
            TmfStatisticsViewer globalViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fTrace.getName(), fTrace); //$NON-NLS-1$
            fStatsViewers.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);

        } else {
            // There is no trace selected. Shows an empty global tab
            TmfStatisticsViewer globalViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName, fTrace);
            fStatsViewers.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        }
        // Makes the global viewer visible
        fStatsViewers.setSelection(0);
    }
}
