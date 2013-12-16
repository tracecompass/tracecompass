/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *   Patrick Tasse - Close editors to release resources
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of a dialog box to rename an experiment.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class RenameExperimentDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfExperimentElement fExperiment;
    private Text fNewExperimentName;
    private final IContainer fExperimentFolder;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param shell
     *            The parent shell
     * @param experiment
     *            The experiment element rename
     */
    public RenameExperimentDialog(Shell shell, TmfExperimentElement experiment) {
        super(shell);
        fExperiment = experiment;
        TmfExperimentFolder folder = (TmfExperimentFolder) experiment.getParent();
        fExperimentFolder = folder.getResource();
        setTitle(Messages.RenameExperimentDialog_DialogTitle);
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
        return composite;
    }

    private void createNewExperimentNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Old experiment name label
        Label oldExperimentLabel = new Label(folderGroup, SWT.NONE);
        oldExperimentLabel.setFont(font);
        oldExperimentLabel.setText(Messages.RenameExperimentDialog_ExperimentName);

        // Old experiment name field
        Text oldExperimentName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        oldExperimentName.setLayoutData(data);
        oldExperimentName.setFont(font);
        oldExperimentName.setText(fExperiment.getName());
        oldExperimentName.setEnabled(false);

        // New experiment name label
        Label newExperimentLabel = new Label(folderGroup, SWT.NONE);
        newExperimentLabel.setFont(font);
        newExperimentLabel.setText(Messages.RenameExperimentDialog_ExperimentNewName);

        // New experiment name entry field
        fNewExperimentName = new Text(folderGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewExperimentName.setLayoutData(data);
        fNewExperimentName.setFont(font);
        fNewExperimentName.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateNewExperimentName();
            }
        });
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

        updateStatus(new Status(IStatus.OK, Activator.PLUGIN_ID, "")); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // SelectionStatusDialog
    // ------------------------------------------------------------------------

    @Override
    protected void computeResult() {
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        IFolder folder = renameExperiment(fNewExperimentName.getText());
        if (folder == null) {
            return;
        }
        setSelectionResult(new IFolder[] { folder });
        super.okPressed();
    }

    private IFolder renameExperiment(final String newName) {

        IPath oldPath = fExperiment.getResource().getFullPath();
        final IPath newPath = oldPath.append("../" + newName); //$NON-NLS-1$

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    // Close the experiment if open
                    fExperiment.closeEditors();

                    IFolder folder = fExperiment.getResource();
                    IFile bookmarksFile = fExperiment.getBookmarksFile();
                    IFile newBookmarksFile = folder.getFile(bookmarksFile.getName().replace(fExperiment.getName(), newName));
                    if (bookmarksFile.exists()) {
                        if (!newBookmarksFile.exists()) {
                            IPath newBookmarksPath = newBookmarksFile.getFullPath();
                            bookmarksFile.move(newBookmarksPath, IResource.FORCE | IResource.SHALLOW, null);
                        }
                    }

                    fExperiment.renameSupplementaryFolder(newName);
                    fExperiment.getResource().move(newPath, IResource.FORCE | IResource.SHALLOW, null);
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
            MessageDialog.openError(getShell(), "", exception.getTargetException().getMessage()); //$NON-NLS-1$
            return null;
        } catch (RuntimeException exception) {
            return null;
        }

        return fExperiment.getResource();
    }

}
