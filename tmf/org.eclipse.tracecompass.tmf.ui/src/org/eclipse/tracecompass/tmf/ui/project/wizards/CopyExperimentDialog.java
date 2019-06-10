/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *   Geneviève Bastien - Moved the actual copy code to model element's class
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of the copy experiement dialog box.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class CopyExperimentDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfExperimentElement fExperiment;
    private Text fNewExperimentName;
    private IFolder fExperimentFolder;
    private Button fDeepCopyButton;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param shell
     *            The parent shell
     * @param experiment
     *            The TMF experiment model element
     */
    public CopyExperimentDialog(Shell shell, TmfExperimentElement experiment) {
        super(shell);
        fExperiment = experiment;
        TmfExperimentFolder folder = (TmfExperimentFolder) experiment.getParent();
        fExperimentFolder = folder.getResource();
        setTitle(Messages.CopyExperimentDialog_DialogTitle);
        setStatusLineAboveButtons(true);
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createNewExperimentNameGroup(composite);
        fDeepCopyButton = new Button(composite, SWT.CHECK);
        fDeepCopyButton.setText(Messages.CopyExperimentDialog_DeepCopyButton);
        fDeepCopyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validateNewExperimentName();
            }
        });
        return composite;
    }

    private void createNewExperimentNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String name = fExperiment.getName();

        // New experiment name label
        Label newExperimentLabel = new Label(folderGroup, SWT.NONE);
        newExperimentLabel.setFont(font);
        newExperimentLabel.setText(Messages.CopyExperimentDialog_ExperimentNewName);

        // New experiment name entry field
        fNewExperimentName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewExperimentName.setLayoutData(data);
        fNewExperimentName.setFont(font);
        fNewExperimentName.setFocus();
        fNewExperimentName.setText(name);
        fNewExperimentName.setSelection(0, name.length());
        fNewExperimentName.addListener(SWT.Modify, event -> validateNewExperimentName());
    }

    private void validateNewExperimentName() {

        String name = fNewExperimentName.getText();
        IWorkspace workspace = fExperimentFolder.getWorkspace();
        IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

        if ("".equals(name)) { //$NON-NLS-1$
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_EmptyNameError, null));
            return;
        }

        if (!nameStatus.isOK()) {
            updateStatus(nameStatus);
            return;
        }

        IPath path = new Path(name);
        if (fExperimentFolder.getFolder(path).exists() || fExperimentFolder.getFile(path).exists()) {
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_ExistingNameError, null));
            return;
        }

        Status deepCopyStatus = validateDeepCopyDestination();
        if (deepCopyStatus.getSeverity() != IStatus.OK) {
            updateStatus(deepCopyStatus);
            return;
        }

        updateStatus(new Status(IStatus.OK, Activator.PLUGIN_ID, "")); //$NON-NLS-1$
    }

    private Status validateDeepCopyDestination() {
        Status status = new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
        if (fDeepCopyButton.getSelection()) {
            TmfTraceFolder tracesFolderElement = fExperiment.getProject().getTracesFolder();
            if (tracesFolderElement != null) {
                IFolder tracesFolder = tracesFolderElement.getResource();
                IPath traceDestinationPath = new Path(fNewExperimentName.getText());
                if (tracesFolder.getFolder(traceDestinationPath).exists()) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.CopyExperimentDialog_DeepCopyError, null);
                }
            }
        }
        return status;
    }

    // ------------------------------------------------------------------------
    // SelectionStatusDialog
    // ------------------------------------------------------------------------

    @Override
    protected void computeResult() {
        // Do nothing
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        boolean copyAsLink = !fDeepCopyButton.getSelection();
        IFolder folder = copyExperiment(fNewExperimentName.getText(), copyAsLink);
        if (folder == null) {
            return;
        }
        setSelectionResult(new IFolder[] { folder });
        super.okPressed();
    }

    private IFolder copyExperiment(final String newName, boolean copyAsLink) {

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }

                    fExperiment.copy(newName, true, copyAsLink);

                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException exception) {
            return null;
        } catch (InvocationTargetException exception) {
            TraceUtils.displayErrorMsg("", NLS.bind("", exception.getTargetException().getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } catch (RuntimeException exception) {
            return null;
        }

        return fExperiment.getResource();
    }

}
