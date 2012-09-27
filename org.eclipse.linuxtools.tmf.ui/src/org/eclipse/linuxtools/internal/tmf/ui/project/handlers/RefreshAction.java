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
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * <b><u>RefreshAction</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class RefreshAction extends Action {

    private static final String REFRESH_COMMAND_ID = "org.eclipse.ui.file.refresh"; //$NON-NLS-1$

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
            if (element instanceof TmfTraceFolder || element instanceof TmfExperimentFolder || element instanceof TmfExperimentElement) {
                handlerService.executeCommand(REFRESH_COMMAND_ID, null);
            }
        } catch (ExecutionException e) {
            Activator.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            Activator.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotEnabledException e) {
            Activator.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotHandledException e) {
            Activator.getDefault().logError("Error refreshing resource " + element.getName(), e); //$NON-NLS-1$
        }
    }

}
