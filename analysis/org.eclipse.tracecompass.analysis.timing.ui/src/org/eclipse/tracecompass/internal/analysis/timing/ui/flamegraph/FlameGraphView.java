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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.CallGraphAnalysisUI;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

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

    private final @NonNull MenuManager fEventMenuManager = new MenuManager();

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
        getSite().setSelectionProvider(fTimeGraphViewer.getSelectionProvider());
        createTimeEventContextMenu();
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

    private void createTimeEventContextMenu() {
        fEventMenuManager.setRemoveAllWhenShown(true);
        TimeGraphControl timeGraphControl = fTimeGraphViewer.getTimeGraphControl();
        final Menu timeEventMenu = fEventMenuManager.createContextMenu(timeGraphControl);

        timeGraphControl.addTimeGraphEntryMenuListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent event) {
                /*
                 * The TimeGraphControl will call the TimeGraphEntryMenuListener
                 * before the TimeEventMenuListener. We need to clear the menu
                 * for the case the selection was done on the namespace where
                 * the time event listener below won't be called afterwards.
                 */
                timeGraphControl.setMenu(null);
                event.doit = false;
            }
        });
        timeGraphControl.addTimeEventMenuListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent event) {
                Menu menu = timeEventMenu;
                if (event.data instanceof FlamegraphEvent) {
                    timeGraphControl.setMenu(menu);
                    return;
                }
                timeGraphControl.setMenu(null);
                event.doit = false;
            }
        });

        fEventMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillTimeEventContextMenu(fEventMenuManager);
                fEventMenuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        getSite().registerContextMenu(fEventMenuManager, fTimeGraphViewer.getSelectionProvider());
    }

    /**
     * Fill context menu
     *
     * @param menuManager
     *          a menuManager to fill
     */
    protected void fillTimeEventContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            for (Object object : ((IStructuredSelection) selection).toList()) {
                if (object instanceof FlamegraphEvent) {
                    final FlamegraphEvent flamegraphEvent = (FlamegraphEvent) object;
                    menuManager.add(new Action(Messages.FlameGraphView_GotoMaxDuration) {
                        @Override
                        public void run() {
                            ISegment maxSeg = flamegraphEvent.getStatistics().getMaxSegment();
                            TmfSelectionRangeUpdatedSignal sig = new TmfSelectionRangeUpdatedSignal(this, TmfTimestamp.fromNanos(maxSeg.getStart()), TmfTimestamp.fromNanos(maxSeg.getEnd()));
                            broadcast(sig);
                        }
                    });

                    menuManager.add(new Action(Messages.FlameGraphView_GotoMinDuration) {
                        @Override
                        public void run() {
                            ISegment minSeg = flamegraphEvent.getStatistics().getMinSegment();
                            TmfSelectionRangeUpdatedSignal sig = new TmfSelectionRangeUpdatedSignal(this, TmfTimestamp.fromNanos(minSeg.getStart()), TmfTimestamp.fromNanos(minSeg.getEnd()));
                            broadcast(sig);
                        }
                    });
                }
            }
        }
    }
}
