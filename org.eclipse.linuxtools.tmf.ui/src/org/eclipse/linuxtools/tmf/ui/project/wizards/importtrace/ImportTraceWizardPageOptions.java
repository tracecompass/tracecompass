/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle - Use common method to get opened tmf projects
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;

/**
 * This page selects the project to import to.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class ImportTraceWizardPageOptions extends AbstractImportTraceWizardPage {

    private List fProjects;
    private final Map<String, IProject> fProjectsMap = new LinkedHashMap<>();

    /**
     * Import page that tells where the trace will go
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPageOptions(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        IFolder originalFolder = getBatchWizard().getTargetFolder();
        IProject proj = null;
        if (originalFolder != null) {
            proj = originalFolder.getProject();
        }

        Composite optionPane = (Composite) this.getControl();
        optionPane.setLayout(new GridLayout());
        optionPane.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

        fProjects = new List(optionPane, SWT.V_SCROLL);
        fProjects.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        for (IProject project : TraceUtils.getOpenedTmfProjects()) {
            final String name = project.getName();
            fProjectsMap.put(name, project);
            fProjects.add(name);
        }

        fProjects.getSelection();
        fProjects.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWithSelection();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                updateWithSelection();
            }
        });
        if (proj != null) {
            fProjects.setSelection(fProjects.indexOf(proj.getName()));
        } else if (fProjects.getItemCount() > 0) {
            fProjects.setSelection(0);
        }
        updateWithSelection();
        setMessage(Messages.SharedSelectProject);
        this.setTitle(Messages.ImportTraceWizardPageOptionsTitle);
    }

    private void updateWithSelection() {
        final String TRACE = "Traces"; //$NON-NLS-1$
        String[] selection = fProjects.getSelection();
        if (selection.length > 0) {
            final String listItem = selection[0];
            IFolder folder = fProjectsMap.get(listItem).getFolder(TRACE);
            getBatchWizard().setTraceFolder(folder);
            ImportTraceWizardPageOptions.this.setErrorMessage(null);
        } else {
            ImportTraceWizardPageOptions.this.setErrorMessage(Messages.SharedSelectProject);
        }
    }
}
