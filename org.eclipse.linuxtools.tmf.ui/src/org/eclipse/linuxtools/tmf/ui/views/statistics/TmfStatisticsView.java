/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.tabsview.TmfViewerFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
 *
 * @version 2.0
 * @author Mathieu Denis
 */
public class TmfStatisticsView extends TmfView {

    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /**
     * The viewer that builds the columns to show the statistics.
     *
     * @since 2.0
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
    public TmfStatisticsView(String viewName) {
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
    public TmfStatisticsView() {
        this(TMF_STATISTICS_VIEW);
    }

    @Override
    public void createPartControl(Composite parent) {
        fStatsViewers.setParent(parent);
        createStatisticsViewers();

        ITmfTrace trace = getActiveTrace();
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
     * @since 2.0
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
     * @since 2.0
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
     * @since 2.0
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
     *
     * @since 2.0
     */
    protected void createStatisticsViewers() {
        // Default style for the tabs that will be created
        int defaultStyle = SWT.NONE;

        // The folder composite that will contain the tabs
        Composite folder = fStatsViewers.getParentFolder();

        // Instantiation of the global viewer
        TmfStatisticsViewer globalViewer = getGlobalViewer();
        if (fTrace != null) {
            if (globalViewer != null) {
                // Shows the name of the trace in the global tab
                globalViewer.init(folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fTrace.getName(), fTrace); //$NON-NLS-1$
            }
            fStatsViewers.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);

            String traceName;
            IResource traceResource;
            // Creates a statistics viewer for each trace.
            for (ITmfTrace trace : TmfTraceManager.getTraceSet(fTrace)) {
                traceName = trace.getName();
                traceResource = trace.getResource();
                TmfStatisticsViewer viewer = getStatisticsViewer(traceResource);
                /*
                 * Adds a new viewer only if there is one defined for the
                 * selected trace type, since the global tab already contains
                 * all the basic event counts for the trace(s)
                 */
                if (viewer != null) {
                    viewer.init(folder, traceName, trace);
                    fStatsViewers.addTab(viewer, viewer.getName(), defaultStyle);
                }
            }
        } else {
            if (globalViewer != null) {
                // There is no trace selected. Shows an empty global tab
                globalViewer.init(folder, Messages.TmfStatisticsView_GlobalTabName, fTrace);
            }
            fStatsViewers.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        }
        // Makes the global viewer visible
        fStatsViewers.setSelection(0);
    }

    /**
     * Retrieves and instantiates a viewer based on his plug-in definition for a
     * specific trace type. It is specific to the statistics viewer.
     *
     * It only calls the 0-parameter constructor without performing any other
     * initialization on the viewer.
     *
     * @param resource
     *            The resource where to find the information about the trace
     *            properties
     * @return a new statistics viewer based on his plug-in definition, or null
     *         if no statistics definition was found for the trace type.
     * @since 2.0
     */
    protected static TmfStatisticsViewer getStatisticsViewer(IResource resource) {
        return (TmfStatisticsViewer) TmfTraceType.getTraceTypeElement(resource, TmfTraceType.STATISTICS_VIEWER_ELEM);
    }

    /**
     * @return The class to use to instantiate the global statistics viewer
     * @since 2.0
     */
    protected TmfStatisticsViewer getGlobalViewer() {
        return new TmfStatisticsViewer();
    }
}
