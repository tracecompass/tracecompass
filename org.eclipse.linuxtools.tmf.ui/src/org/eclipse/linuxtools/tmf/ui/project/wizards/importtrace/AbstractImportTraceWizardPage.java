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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardResourceImportPage;

/**
 * The abstract import trace wizard page, the base for the import trace wizard
 * pages.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
abstract class AbstractImportTraceWizardPage extends WizardResourceImportPage {

    /**
     * Import String
     */
    protected static final String BATCH_IMPORT_WIZARD_PAGE = "BatchImportWizardPage"; //$NON-NLS-1$

    /**
     * The trace folder, something like "/<project name>/Traces/"
     */
    protected IFolder fTargetFolder;

    /**
     * The project "/<project name>"
     */
    protected IProject fProject;

    /**
     * The batch import trace wizard (parent)
     */
    private BatchImportTraceWizard fBatchImportTraceWizard;

    /**
     * @param name
     *            the name of the page
     * @param selection
     *            The current selection
     */
    protected AbstractImportTraceWizardPage(String name, IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Constructor
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public AbstractImportTraceWizardPage(IWorkbench workbench, IStructuredSelection selection) {
        this(BATCH_IMPORT_WIZARD_PAGE, selection);
        setTitle(Messages.ImportTraceWizard_FileSystemTitle);
        setDescription(Messages.ImportTraceWizard_DialogTitle);

        // Locate the target trace folder
        IFolder traceFolder = null;
        Object element = selection.getFirstElement();

        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder tmfTraceFolder = (TmfTraceFolder) element;
            fProject = (tmfTraceFolder.getProject().getResource());
            traceFolder = tmfTraceFolder.getResource();
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    traceFolder = (IFolder) project.findMember(TmfTraceFolder.TRACE_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = (traceFolder);
            String path = traceFolder.getFullPath().toOSString();
            setContainerFieldValue(path);
        }

    }

    /**
     * The Batch Import Wizard
     *
     * @return the Batch Import Wizard
     */
    public BatchImportTraceWizard getBatchWizard() {
        return fBatchImportTraceWizard;
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setFont(parent.getFont());
        // arbitrary size
        final GridData layoutData = new GridData();
        parent.getShell().setLayoutData(layoutData);
        parent.getShell().redraw();
        this.setControl(composite);

        // arbitrary sizes
        parent.getShell().setMinimumSize(new Point(525, 400));
        fBatchImportTraceWizard = (BatchImportTraceWizard) getWizard();
    }

    // the following methods are stubbed out on purpose.

    @Override
    protected void createSourceGroup(Composite parent) {
        // do nothing
    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        // do nothing
        return null;
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        // do nothing
        return null;
    }

}
