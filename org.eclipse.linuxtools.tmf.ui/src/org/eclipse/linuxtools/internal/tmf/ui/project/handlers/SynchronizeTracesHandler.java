/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handles the synchronization of an experiment, when the user selects this
 * option in the menu
 */
public class SynchronizeTracesHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TreeSelection fSelection = null;
    private static final String CR = System.getProperty("line.separator"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return Boolean.FALSE;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return Boolean.FALSE;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        final ArrayList<TmfTraceElement> tl = new ArrayList<>();
        final ArrayList<TmfExperimentElement> uiexperiment = new ArrayList<>();
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof TmfTraceElement) {
                    tl.add((TmfTraceElement) element);
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    uiexperiment.add(exp);
                    for (TmfTraceElement trace : exp.getTraces()) {
                        tl.add(trace);
                    }
                }
            }
        }

        if ((uiexperiment.size() != 1) || (tl.size() < 2)) {
            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongTraceNumber);
            return null;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace[] traces = new ITmfTrace[tl.size()];
                final TmfExperimentElement exp = uiexperiment.get(0);

                for (int i = 0; i < tl.size(); i++) {
                    ITmfTrace trace = tl.get(i).instantiateTrace();
                    ITmfEvent traceEvent = tl.get(i).instantiateEvent();
                    if (trace == null) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongType + tl.get(i).getName());
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(tl.get(i).getResource(), tl.get(i).getResource().getLocation().toOSString(), traceEvent.getClass());
                        TmfTraceManager.refreshSupplementaryFiles(trace);
                    } catch (TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_InitError + CR + CR + e);
                        trace.dispose();
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    traces[i] = trace;
                }

                /*
                 * FIXME Unlike traces, there is no instanceExperiment, so
                 * we call this function here alone. Maybe it would be
                 * better to do this on experiment's element constructor?
                 */
                exp.refreshSupplementaryFolder();
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, exp.getName(), traces, exp.getResource());

                final SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);
                TmfTraceManager.refreshSupplementaryFiles(experiment);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        List<TmfTraceElement> tracesToAdd = new ArrayList<>();
                        List<TmfTraceElement> tracesToRemove = new ArrayList<>();
                        /*
                         * For each trace in the experiment, if there is
                         * a transform equation, copy the original
                         * trace, so that a new state system will be
                         * generated with sync time.
                         */
                        for (TmfTraceElement traceel : tl) {
                            /*
                             * Find the trace corresponding to this
                             * element in the experiment
                             */
                            ITmfTrace expTrace = null;
                            for (ITmfTrace t : experiment.getTraces()) {
                                if (t.getResource().equals(traceel.getResource())) {
                                    expTrace = t;
                                    break;
                                }
                            }
                            if ((expTrace != null) && syncAlgo.isTraceSynced(expTrace.getHostId())) {

                                /* Find the original trace */
                                TmfTraceElement origtrace = traceel.getElementUnderTraceFolder();

                                /*
                                 * Make sure a trace with the
                                 * new name does not exist
                                 */
                                String newname = traceel.getName();
                                IContainer parentFolder = origtrace.getResource().getParent();
                                boolean traceexists;
                                do {
                                    traceexists = false;
                                    newname += "_"; //$NON-NLS-1$
                                    if (parentFolder.findMember(newname) != null) {
                                        traceexists = true;
                                    }
                                } while (traceexists);

                                /* Copy the original trace */
                                TmfTraceElement newtrace = origtrace.copy(newname);
                                if (newtrace == null) {
                                    TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title,
                                            Messages.SynchronizeTracesHandler_Error + CR + CR + String.format(Messages.SynchronizeTracesHandler_CopyProblem, origtrace.getName()));
                                    continue;
                                }

                                /*
                                 * Instantiate the new trace
                                 * and set its sync formula
                                 */
                                ITmfTrace trace = newtrace.instantiateTrace();
                                ITmfEvent traceEvent = newtrace.instantiateEvent();

                                try {
                                    trace.initTrace(newtrace.getResource(), newtrace.getResource().getLocation().toOSString(), traceEvent.getClass());
                                } catch (TmfTraceException e) {
                                    Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), traceel.getName()), e);
                                    TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e.getMessage());
                                }
                                trace.setTimestampTransform(syncAlgo.getTimestampTransform(trace));
                                TmfTraceManager.refreshSupplementaryFiles(trace);
                                trace.dispose();

                                tracesToAdd.add(newtrace);
                                tracesToRemove.add(traceel);
                            }
                        }
                        experiment.dispose();

                        // Move synchronization file temporarily so that
                        // it doesn't get deleted by the experiment change
                        IFolder tmpFolder = exp.getTraceSupplementaryFolder(exp.getName() + '.' + TmfExperiment.SYNCHRONIZATION_DIRECTORY);
                        IResource syncFile = null;
                        for (IResource resource : exp.getSupplementaryResources()) {
                            if (resource.getName().equals(TmfExperiment.SYNCHRONIZATION_DIRECTORY)) {
                                try {
                                    resource.move(tmpFolder.getFullPath(), false, null);
                                    syncFile = resource;
                                    break;
                                } catch (CoreException e) {
                                    Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingExperiment, exp.getName()), e);
                                }
                            }
                        }

                        for (TmfTraceElement trace : tracesToRemove) {
                            try {
                                exp.removeTrace(trace);
                            } catch (CoreException e) {
                                Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), trace.getName()), e);
                                TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e.getMessage());
                            }
                        }
                        for (TmfTraceElement trace : tracesToAdd) {
                            exp.addTrace(trace);
                        }

                        // Move synchronization file back
                        if (tmpFolder.exists() && syncFile != null) {
                            try {
                                tmpFolder.move(syncFile.getFullPath(), false, null);
                            } catch (CoreException e) {
                                Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingExperiment, exp.getName()), e);
                            }
                        }
                    }
                });
            }
        };
        thread.start();

        return null;
    }

}
