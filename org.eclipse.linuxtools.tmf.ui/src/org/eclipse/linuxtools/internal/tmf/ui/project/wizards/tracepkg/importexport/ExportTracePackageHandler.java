/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for exporting a trace package
 *
 * @author Marc-Andre Laperle
 */
public class ExportTracePackageHandler extends AbstractHandler {

    private boolean fEnabled = false;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IStructuredSelection sec = StructuredSelection.EMPTY;
        List<TmfTraceElement> selectedTraces = new ArrayList<>();
        if (currentSelection instanceof IStructuredSelection) {
            sec = (IStructuredSelection) currentSelection;
            Object[] selectedElements = sec.toArray();
            for (Object selectedElement : selectedElements) {
                if (selectedElement instanceof TmfTraceElement) {
                    TmfTraceElement tmfTraceElement = (TmfTraceElement) selectedElement;
                    selectedTraces.add(tmfTraceElement.getElementUnderTraceFolder());
                } else if (selectedElement instanceof TmfTraceFolder) {
                    TmfTraceFolder tmfTraceFolder = (TmfTraceFolder) selectedElement;
                    selectedTraces = tmfTraceFolder.getTraces();
                }
            }
        }

        ExportTracePackageWizard w = new ExportTracePackageWizard(selectedTraces);

        w.init(PlatformUI.getWorkbench(), sec);
        WizardDialog dialog = new WizardDialog(window.getShell(), w);
        dialog.open();
        return null;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && fEnabled;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        super.setEnabled(evaluationContext);

        fEnabled = true;

        Object s = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_MENU_SELECTION_NAME);
        if (s instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) s;
            // If we have traces selected, make sure they are all from the same
            // project, disable handler otherwise
            Object[] selectedElements = selection.toArray();
            TmfProjectElement firstProject = null;
            for (Object selectedElement : selectedElements) {
                if (selectedElement instanceof TmfTraceElement) {
                    TmfTraceElement tmfTraceElement = (TmfTraceElement) selectedElement;
                    TmfProjectElement project = tmfTraceElement.getProject();
                    if (firstProject != null && !project.equals(firstProject)) {
                        fEnabled =  false;
                    }

                    firstProject = project;
                }
            }
        }
    }
}
