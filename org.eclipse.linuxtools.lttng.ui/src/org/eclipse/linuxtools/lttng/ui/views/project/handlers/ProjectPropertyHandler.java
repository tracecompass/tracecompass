/*******************************************************************************
 * Copyright (c) 2011 MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yufen Kuo (ykuo@mvista.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.project.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.linuxtools.lttng.ui.views.project.ProjectView;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * <b><u>ProjectPropertyHandler</u></b>
 * <p>
 * 
 */
public class ProjectPropertyHandler implements IHandler {

    private IProject project = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return false;
        project = null;
        // Check if we can find the project model root node
        ISelection selection = window.getActivePage().getSelection(
                ProjectView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection)
                    .getFirstElement();
            if (element instanceof LTTngProjectNode) {
                LTTngProjectNode node = (LTTngProjectNode) element;
                if (node != null) {
                    project = node.getProject();
                }

            } 
        }

        return (project != null);
    }

    // Handled if we are in the ProjectView
    @Override
    public boolean isHandled() {
        return true;
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (project != null) {
            PropertyDialogAction propertyAction = new PropertyDialogAction(
                    new IShellProvider() {
                        public Shell getShell() {
                            return PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow().getShell();
                        }
                    }, new ISelectionProvider() {
                        public void addSelectionChangedListener(
                                ISelectionChangedListener listener) {
                        }

                        public ISelection getSelection() {
                            return new StructuredSelection(project);
                        }

                        public void removeSelectionChangedListener(
                                ISelectionChangedListener listener) {
                        }

                        public void setSelection(ISelection selection) {
                        }
                    });
            propertyAction
                    .setActionDefinitionId(IWorkbenchCommandConstants.FILE_PROPERTIES);
            propertyAction.run();
        }
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    // ------------------------------------------------------------------------
    // IHandlerListener
    // ------------------------------------------------------------------------

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
        // TODO Auto-generated method stub
    }

}
