/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.criticalpath;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow.ControlFlowEntry;
import org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view.CriticalPathView;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngWorker;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisParamProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Class that provides parameters to the critical path analysis for lttng kernel
 * traces
 *
 * @author Geneviève Bastien
 */
public class CriticalPathParameterProvider extends TmfAbstractAnalysisParamProvider {

    private final class IPartListener2Impl implements IPartListener2 {

        private final Class<?> fType;

        IPartListener2Impl(Class<?> type) {
            fType = type;
        }

        private void toggleState(IWorkbenchPartReference partRef, boolean state) {
            if (fType.isInstance(partRef.getPart(false))) {
                toggleActive(state);
            }
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
            toggleState(partRef, true);
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {

        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            toggleState(partRef, false);
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {

        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {
            toggleState(partRef, true);
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
            toggleState(partRef, false);
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            toggleState(partRef, true);
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {

        }
    }

    private ISelectionListener fSelListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) selection).getFirstElement();
                if (element instanceof ControlFlowEntry) {
                    ControlFlowEntry entry = (ControlFlowEntry) element;
                    setCurrentThreadId(entry);
                }
            }
        }
    };

    private static final String NAME = "Critical Path Lttng kernel parameter provider"; //$NON-NLS-1$

    private final IPartListener2 fPartListener = new IPartListener2Impl(CriticalPathView.class);

    private ControlFlowEntry fCurrentEntry = null;

    private boolean fActive = false;
    private boolean fEntryChanged = false;

    /**
     * Constructor
     */
    public CriticalPathParameterProvider() {
        super();
        registerListener();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getParameter(String name) {
        if (fCurrentEntry == null) {
            return null;
        }
        if (name.equals(CriticalPathModule.PARAM_WORKER)) {
            /* Try to find the worker for the critical path */
            IAnalysisModule mod = getModule();
            if ((mod != null) && (mod instanceof CriticalPathModule)) {
                Integer threadId = fCurrentEntry.getThreadId();
                HostThread ht = new HostThread(fCurrentEntry.getTrace().getHostId(), threadId);
                LttngWorker worker = new LttngWorker(ht, "", 0); //$NON-NLS-1$
                return worker;
            }
            return fCurrentEntry;
        }
        return null;
    }

    @Override
    public boolean appliesToTrace(ITmfTrace trace) {
        return true;
    }

    private void setCurrentThreadId(ControlFlowEntry entry) {
        if (!entry.equals(fCurrentEntry)) {
            fCurrentEntry = entry;
            if (fActive) {
                this.notifyParameterChanged(CriticalPathModule.PARAM_WORKER);
            } else {
                fEntryChanged = true;
            }
        }
    }

    private void toggleActive(boolean active) {
        if (active != fActive) {
            fActive = active;
            if (fActive && fEntryChanged) {
                this.notifyParameterChanged(CriticalPathModule.PARAM_WORKER);
                fEntryChanged = false;
            }
        }
    }

    private void registerListener() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb == null) {
            return;
        }
        IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
        if (wbw == null) {
            return;
        }
        final IWorkbenchPage activePage = wbw.getActivePage();
        if (activePage == null) {
            return;
        }

        /* Activate the update if critical path view visible */
        IViewPart view = activePage.findView(CriticalPathView.ID);
        if (view != null) {
            if (activePage.isPartVisible(view)) {
                toggleActive(true);
            }
        }

        /* Add the listener to the control flow view */
        view = activePage.findView(ControlFlowView.ID);
        if (view != null) {
            view.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(fSelListener);
            view.getSite().getWorkbenchWindow().getPartService().addPartListener(fPartListener);
        }
    }

}
