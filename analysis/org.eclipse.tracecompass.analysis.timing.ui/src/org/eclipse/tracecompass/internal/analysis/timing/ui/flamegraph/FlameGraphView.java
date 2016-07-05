/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Author:
 *     Sonia Farrah
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.CallGraphAnalysisUI;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.ui.IEditorPart;

/**
 * View to display the flame graph .This uses the flameGraphNode tree generated
 * by CallGraphAnalysisUI.
 *
 * @author Sonia Farrah
 */
public class FlameGraphView extends TmfView {

    /**
     *
     */
    public static final String ID = FlameGraphView.class.getPackage().getName() + ".flamegraphView"; //$NON-NLS-1$

    private TimeGraphViewer fTimeGraphViewer;

    private FlameGraphContentProvider fTimeGraphContentProvider;

    private TimeGraphPresentationProvider fPresentationProvider;

    private ITmfTrace fTrace;

    /**
     * Constructor
     */
    public FlameGraphView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);
        fTimeGraphContentProvider = new FlameGraphContentProvider();
        fPresentationProvider = new FlameGraphPresentationProvider();
        fTimeGraphViewer.setTimeGraphContentProvider(fTimeGraphContentProvider);
        fTimeGraphViewer.setTimeGraphProvider(fPresentationProvider);
        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
            if (trace != null) {
                traceSelected(new TmfTraceSelectedSignal(this, trace));
            }
        }
    }

    /**
     * Signal updated
     *
     * @param sig
     *            signal
     */
    @TmfSignalHandler
    public void selectionUpdated(TmfSelectionRangeUpdatedSignal sig) {
        fTrace = TmfTraceManager.getInstance().getActiveTrace();
        if (fTrace != null) {
            CallGraphAnalysis flamegraphModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, CallGraphAnalysis.class, CallGraphAnalysisUI.ID);
            buildFlameGraph(flamegraphModule);
        }
    }

    /**
     * Handler for the trace opened signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void TraceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        if (fTrace != null) {
            CallGraphAnalysis flamegraphModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, CallGraphAnalysis.class, CallGraphAnalysisUI.ID);
            buildFlameGraph(flamegraphModule);
        }
    }

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        fTrace = signal.getTrace();
        if (fTrace != null) {
            CallGraphAnalysis flamegraphModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, CallGraphAnalysis.class, CallGraphAnalysisUI.ID);
            buildFlameGraph(flamegraphModule);
        }
    }

    /**
     * Get the necessary data for the flame graph and display it
     *
     * @param flamegraphModule
     *            the callGraphAnalysis
     */
    private void buildFlameGraph(CallGraphAnalysis callGraphAnalysis) {
        fTimeGraphViewer.setInput(null);
        callGraphAnalysis.schedule();
        Job j = new Job(Messages.CallGraphAnalysis_Execution) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                callGraphAnalysis.waitForCompletion(monitor);
                Display.getDefault().asyncExec(() -> {
                    fTimeGraphViewer.setInput(callGraphAnalysis.getThreadNodes());
                });
                return Status.OK_STATUS;
            }
        };
        j.schedule();
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal
     *            the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        if (signal.getTrace() == fTrace) {
            fTimeGraphViewer.setInput(null);
        }
    }

    @Override
    public void setFocus() {
        fTimeGraphViewer.setFocus();
    }

}
