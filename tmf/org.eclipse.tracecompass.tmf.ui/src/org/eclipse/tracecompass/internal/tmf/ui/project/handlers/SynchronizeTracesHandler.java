/*******************************************************************************
 * Copyright (c) 2013, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *   Cédric Biancheri - Added a wizard to select the root node
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

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
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.tracecompass.tmf.ui.project.wizards.SelectRootNodeWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

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
    private TmfExperimentElement fExperiment = null;
    private TmfTraceElement fRootNode = null;
    private String fRootNodeId = null;

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
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        // Make sure selection contains only traces
        fSelection = null;
        final ArrayList<TmfTraceElement> tl = new ArrayList<>();
        final ArrayList<TmfExperimentElement> uiexperiment = new ArrayList<>();
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof TmfExperimentElement) {
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
        fExperiment = uiexperiment.get(0);
        fRootNode = null;
        fRootNodeId = null;

        // Fire the Select Root Node Wizard
        IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        Shell shell = workbenchWindow.getShell();
        SelectRootNodeWizard wizard = new SelectRootNodeWizard(fExperiment);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        int returnValue = dialog.open();
        if (returnValue == Window.CANCEL) {
            return null;
        }
        fRootNode = wizard.getRootNode();

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace[] traces = new ITmfTrace[tl.size()];
                final TmfExperimentElement exp = uiexperiment.get(0);

                for (int i = 0; i < tl.size(); i++) {
                    TmfTraceElement traceElement = tl.get(i).getElementUnderTraceFolder();
                    ITmfTrace trace = traceElement.instantiateTrace();
                    ITmfEvent traceEvent = traceElement.instantiateEvent();
                    if (trace == null) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongType + traceElement.getName());
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(traceElement.getResource(), traceElement.getResource().getLocation().toOSString(), traceEvent.getClass());
                        TmfTraceManager.refreshSupplementaryFiles(trace);
                    } catch (TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_InitError + CR + CR + e);
                        trace.dispose();
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    if (traceElement.getElementPath().equals(fRootNode.getElementPath())) {
                        fRootNodeId = trace.getHostId();
                    }
                    traces[i] = trace;
                }

                /*
                 * FIXME Unlike traces, there is no instanceExperiment, so we
                 * call this function here alone. Maybe it would be better to do
                 * this on experiment's element constructor?
                 */
                exp.refreshSupplementaryFolder();
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class,
                        exp.getName(), traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, exp.getResource());

                final SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);
                syncAlgo.setRootNode(fRootNodeId);

                TmfTraceManager.refreshSupplementaryFiles(experiment);

                Display.getDefault().asyncExec(() -> {
                    List<TmfTraceElement> tracesToAdd = new ArrayList<>();
                    List<TmfTraceElement> tracesToRemove = new ArrayList<>();
                    /*
                     * For each trace in the experiment, if there is a
                     * transform equation, copy the original trace, so that
                     * a new state system will be generated with sync time.
                     */
                    for (TmfTraceElement traceel : tl) {
                        /* Find the original trace */
                        TmfTraceElement origtrace = traceel.getElementUnderTraceFolder();

                        /*
                         * Find the trace corresponding to this element in
                         * the experiment
                         */
                        ITmfTrace expTrace = null;
                        for (ITmfTrace t : experiment.getTraces()) {
                            if (t.getResource().equals(origtrace.getResource())) {
                                expTrace = t;
                                break;
                            }
                        }
                        if ((expTrace != null) && syncAlgo.isTraceSynced(expTrace.getHostId())) {
                            /*
                             * Make sure a trace with the new name does not
                             * exist
                             */
                            StringBuilder newname = new StringBuilder(traceel.getName());
                            IContainer parentFolder = origtrace.getResource().getParent();
                            boolean traceexists;
                            do {
                                traceexists = false;
                                newname.append('_');
                                if (parentFolder.findMember(newname.toString()) != null) {
                                    traceexists = true;
                                }
                            } while (traceexists);

                            /* Copy the original trace */
                            TmfTraceElement newtrace = origtrace.copy(newname.toString());
                            if (newtrace == null) {
                                TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title,
                                        Messages.SynchronizeTracesHandler_Error + CR + CR + String.format(Messages.SynchronizeTracesHandler_CopyProblem, origtrace.getName()));
                                continue;
                            }

                            /*
                             * Instantiate the new trace and set its sync
                             * formula
                             */
                            ITmfTrace trace1 = newtrace.instantiateTrace();
                            ITmfEvent traceEvent = newtrace.instantiateEvent();

                            try {
                                trace1.initTrace(newtrace.getResource(), newtrace.getResource().getLocation().toOSString(), traceEvent.getClass());
                            } catch (TmfTraceException e1) {
                                Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), traceel.getName()), e1);
                                TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e1.getMessage());
                            }
                            trace1.setTimestampTransform(syncAlgo.getTimestampTransform(expTrace));
                            TmfTraceManager.refreshSupplementaryFiles(trace1);
                            trace1.dispose();

                            tracesToAdd.add(newtrace);
                            tracesToRemove.add(traceel);
                        }
                    }
                    experiment.dispose();

                    // Move synchronization file temporarily so that
                    // it doesn't get deleted by the experiment change
                    IFolder tmpFolder = exp.getTraceSupplementaryFolder(exp.getName() + '.' + experiment.getSynchronizationFolder(false));
                    IResource syncFile = null;
                    for (IResource resource : exp.getSupplementaryResources()) {
                        if (resource.getName().equals(experiment.getSynchronizationFolder(false))) {
                            try {
                                resource.move(tmpFolder.getFullPath(), false, null);
                                syncFile = resource;
                                break;
                            } catch (CoreException e2) {
                                Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingExperiment, exp.getName()), e2);
                            }
                        }
                    }

                    for (TmfTraceElement trace2 : tracesToRemove) {
                        try {
                            exp.removeTrace(trace2);
                        } catch (CoreException e3) {
                            Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingForTrace, exp.getName(), trace2.getName()), e3);
                            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_Error + CR + CR + e3.getMessage());
                        }
                    }
                    for (TmfTraceElement trace3 : tracesToAdd) {
                        exp.addTrace(trace3);
                    }

                    // Move synchronization file back
                    if (tmpFolder.exists() && syncFile != null) {
                        try {
                            tmpFolder.move(syncFile.getFullPath(), false, null);
                        } catch (CoreException e4) {
                            Activator.getDefault().logError(String.format(Messages.SynchronizeTracesHandler_ErrorSynchingExperiment, exp.getName()), e4);
                        }
                    }
                });
            }
        };
        thread.start();

        return null;
    }

}
