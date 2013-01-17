/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
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
            return false;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        final ArrayList<TmfTraceElement> tl = new ArrayList<TmfTraceElement>();
        final ArrayList<TmfExperimentElement> uiexperiment = new ArrayList<TmfExperimentElement>();
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

        if ((uiexperiment.size() == 1) && (tl.size() > 1)) {

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
                            trace.initTrace(tl.get(i).getResource(), tl.get(i).getLocation().getPath(), traceEvent.getClass());
                        } catch (TmfTraceException e) {
                            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.OpenTraceHandler_InitError + CR + CR + e);
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

                    try {
                        final SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);

                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                /*
                                 * For each trace in the experiment, if there is
                                 * a transform equation, copy the original
                                 * trace, so that a new state system will be
                                 * generated with sync time.
                                 */
                                for (int i = 0; i < tl.size(); i++) {
                                    TmfTraceElement traceel = tl.get(i);
                                    try {
                                        if (syncAlgo.isTraceSynced(traceel.getName())) {

                                            /* Find the original trace */
                                            TmfTraceElement origtrace = null;
                                            for (ITmfProjectModelElement el : traceel.getProject().getTracesFolder().getTraces()) {
                                                if (el.getName().equals(traceel.getName())) {
                                                    origtrace = (TmfTraceElement) el;
                                                }
                                            }

                                            if (origtrace != null) {
                                                /*
                                                 * Make sure a trace with the
                                                 * new name does not exist
                                                 */
                                                String newname = traceel.getName();
                                                boolean traceexists;
                                                do {
                                                    traceexists = false;
                                                    newname += "_"; //$NON-NLS-1$
                                                    for (ITmfProjectModelElement el : traceel.getProject().getTracesFolder().getTraces()) {
                                                        if (el.getName().equals(newname)) {
                                                            traceexists = true;
                                                        }
                                                    }
                                                } while (traceexists);

                                                /* Copy the original trace */
                                                TmfTraceElement newtrace = origtrace.copy(newname);

                                                if (newtrace != null) {

                                                    syncAlgo.renameTrace(origtrace.getName(), newtrace.getName());

                                                    /*
                                                     * Instantiate the new trace
                                                     * and set its sync formula
                                                     */
                                                    ITmfTrace trace = newtrace.instantiateTrace();
                                                    ITmfEvent traceEvent = newtrace.instantiateEvent();

                                                    trace.initTrace(newtrace.getResource(), newtrace.getLocation().getPath(), traceEvent.getClass());
                                                    trace.setTimestampTransform(syncAlgo.getTimestampTransform(trace));

                                                    /*
                                                     * Add the new trace to the
                                                     * experiment
                                                     */
                                                    exp.addTrace(newtrace);

                                                    /*
                                                     * Delete the original trace
                                                     * element
                                                     */
                                                    exp.removeTrace(traceel);
                                                } else {
                                                    TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + String.format(Messages.SynchronizeTracesHandler_CopyProblem, origtrace.getName()));
                                                }
                                            }
                                        }
                                    } catch (CoreException e) {
                                        Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), traceel.getName()), e);
                                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e.getMessage());
                                    } catch (TmfTraceException e) {
                                        Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), traceel.getName()), e);
                                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e.getMessage());
                                    }
                                }
                            }
                        });

                    } catch (TmfTraceException e) {
                        Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingExperiment, exp.getName()), e);
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.OpenExperimentHandler_Error + CR + CR + e.getMessage());
                    }
                }
            };
            thread.start();

        } else {
            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongTraceNumber);
        }

        return null;
    }

}
