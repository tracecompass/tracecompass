/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.handlers;

import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>OpenExperimentHandler</u></b>
 * <p>
 */
public class OpenExperimentHandler extends AbstractHandler {

    private TmfExperimentElement fExperiment = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null)
            return false;
        ISelection selection = selectionProvider.getSelection();

        // Make sure there is only one selection and that it is an experiment
        fExperiment = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfExperimentElement) {
                fExperiment = (TmfExperimentElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return (fExperiment != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        // Close the current experiment, if any
        TmfExperiment<?> currentExperiment = TmfExperiment.getCurrentExperiment();
        if (currentExperiment != null) {
            currentExperiment.dispose();
        }

        // Instantiate the experiment's traces
        List<TmfTraceElement> traceEntries = fExperiment.getTraces();
        int nbTraces = traceEntries.size();
        int cacheSize = Integer.MAX_VALUE;
        ITmfTrace<?>[] traces = new ITmfTrace[nbTraces];
        for (int i = 0; i < nbTraces; i++) {
            TmfTraceElement element = traceEntries.get(i);
            ITmfTrace trace = element.instantiateTrace();
            TmfEvent traceEvent = element.instantiateEvent();
            try {
                trace.initTrace(element.getLocation().getPath(), traceEvent.getClass(), false);
            } catch (FileNotFoundException e) {
                displayErrorMsg(""); //$NON-NLS-1$
            }
            cacheSize = Math.min(cacheSize, trace.getCacheSize());
            traces[i] = trace;
        }

        // Create the experiment and signal
        TmfExperiment experiment = new TmfExperiment(TmfEvent.class, fExperiment.getName(), traces, cacheSize);
        TmfExperiment.setCurrentExperiment(experiment);
        TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal(this, experiment));

        return null;
    }

    private void displayErrorMsg(String errorMsg) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(Messages.OpenTraceHandler_Title);
        mb.setMessage(errorMsg);
        mb.open();
    }

}
