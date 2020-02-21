/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *   Geneviève Bastien - Add support of experiment types
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.NewExperimentOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of new experiment dialog that creates the experiment element.
 * <p>
 *
 * @author Francois Chouinard
 */
public class NewExperimentDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private Text fExperimentName;
    private final IContainer fExperimentFolder;
    private final TmfExperimentFolder fExperimentFolderRoot;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            The parent shell
     * @param experimentFolder
     *            The parent experiment folder element
     */
    public NewExperimentDialog(Shell shell, TmfExperimentFolder experimentFolder) {
        super(shell);
        fExperimentFolderRoot = experimentFolder;
        fExperimentFolder = experimentFolder.getResource();
        setTitle(Messages.NewExperimentDialog_DialogTitle);
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

        createExperimentNameGroup(composite);
        return composite;
    }

    private void createExperimentNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // New experiment label
        Label experimentLabel = new Label(folderGroup, SWT.NONE);
        experimentLabel.setFont(font);
        experimentLabel.setText(Messages.NewExperimentDialog_ExperimentName);

        // New experiment name entry field
        fExperimentName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fExperimentName.setLayoutData(data);
        fExperimentName.setFont(font);
        fExperimentName.addListener(SWT.Modify, event -> validateNewExperimentName());
    }

    private void validateNewExperimentName() {

        String name = fExperimentName.getText();
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

        updateStatus(new Status(IStatus.OK, Activator.PLUGIN_ID, "")); //$NON-NLS-1$
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
        String experimentName = fExperimentName.getText();
        if (experimentName == null) {
            return;
        }
        IFolder folder = createNewExperiment(experimentName);
        if (folder == null) {
            return;
        }
        setSelectionResult(new IFolder[] { folder });
        super.okPressed();
    }

    private IFolder createNewExperiment(@NonNull String experimentName) {
        final IFolder[] experimentFolders = new IFolder[1];
        final TmfExperimentFolder root = fExperimentFolderRoot;
        if (root == null) {
            return null;
        }
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
                NewExperimentOperation createOperation = new NewExperimentOperation(root, experimentName);
                createOperation.run(monitor);
                IStatus status = createOperation.getStatus();
                if (!status.isOK()) {
                    if (status.getSeverity() == IStatus.CANCEL) {
                        throw new OperationCanceledException();
                    }
                    Throwable exception = status.getException();
                    if (exception != null) {
                        throw new InvocationTargetException(exception);
                    }
                    return;
                }
                experimentFolders[0] = createOperation.getExperimentFolder();
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException | RuntimeException exception) {
            return null;
        } catch (InvocationTargetException exception) {
            TraceUtils.displayErrorMsg("", NLS.bind("", exception.getTargetException().getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        return experimentFolders[0];
    }

}
