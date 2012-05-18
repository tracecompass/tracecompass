/*******************************************************************************
* Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;

public class RefreshAction extends Action {

    private static final String REFRESH_TRACE_FOLDER_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.trace.refresh"; //$NON-NLS-1$
    private static final String REFRESH_EXPERIMENT_FOLDER_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.experiment_folder.refresh"; //$NON-NLS-1$
    private static final String REFRESH_EXPERIMENT_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.experiment.refresh"; //$NON-NLS-1$

    private IWorkbenchPage page;
    private ISelectionProvider selectionProvider;
    private TmfProjectModelElement element;

    /**
     * Default constructor
     * @param page the workbench page
     * @param selectionProvider the selection provider
     */
    public RefreshAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        ISelection selection = selectionProvider.getSelection();
        if (!selection.isEmpty()) {
            IStructuredSelection sSelection = (IStructuredSelection) selection;
            if (sSelection.size() == 1) {
                if (sSelection.getFirstElement() instanceof TmfTraceFolder ||
                        sSelection.getFirstElement() instanceof TmfExperimentFolder ||
                        sSelection.getFirstElement() instanceof TmfExperimentElement) {
                    element = (TmfProjectModelElement) sSelection.getFirstElement();
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            IHandlerService handlerService = (IHandlerService) page.getActivePart().getSite().getService(IHandlerService.class);
            if (element instanceof TmfTraceFolder) {
                handlerService.executeCommand(REFRESH_TRACE_FOLDER_COMMAND_ID, null);
            } else if (element instanceof TmfExperimentFolder) {
                handlerService.executeCommand(REFRESH_EXPERIMENT_FOLDER_COMMAND_ID, null);
            } else if (element instanceof TmfExperimentElement) {
                handlerService.executeCommand(REFRESH_EXPERIMENT_COMMAND_ID, null);
            }
        } catch (ExecutionException e) {
            TmfUiPlugin.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            TmfUiPlugin.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotEnabledException e) {
            TmfUiPlugin.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotHandledException e) {
            TmfUiPlugin.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        }
    }

}
