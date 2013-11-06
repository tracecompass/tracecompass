/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.handler;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.module.Messages;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.module.XmlAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Imports and validates an XML file
 *
 * @author Geneviève Bastien
 */
public class ImportXmlHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        FileDialog dlg = new FileDialog(new Shell(), SWT.OPEN);
        dlg.setFilterNames(new String[] { Messages.ImportXmlHandler_ImportXmlFile + " (*.xml)" }); //$NON-NLS-1$
        dlg.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$

        String fn = dlg.open();
        if (fn != null) {
            File file = new File(fn);
            IStatus status = XmlUtils.xmlValidate(file);
            if (status.isOK()) {
                status = XmlUtils.addXmlFile(file);
                if (status.isOK()) {
                    XmlAnalysisModuleSource.notifyModuleChange();
                    /*
                     * FIXME: It refreshes the list of analysis under a trace,
                     * but since modules are instantiated when the trace opens,
                     * the changes won't apply to an opened trace, it needs to
                     * be closed then reopened
                     */
                    refreshProject();
                } else {
                    TraceUtils.displayErrorMsg(Messages.ImportXmlHandler_ImportXmlFile, status.getMessage());
                }
            } else {
                TraceUtils.displayErrorMsg(Messages.ImportXmlHandler_ImportXmlFile, status.getMessage());
            }
        }

        return null;
    }

    /**
     * Refresh the selected project with the new XML file import
     */
    private static void refreshProject() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return;
        }
        ISelection selection = selectionProvider.getSelection();

        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfProjectModelElement) {
                ((TmfProjectModelElement) element).getProject().refresh();
            }
        }

    }

}
