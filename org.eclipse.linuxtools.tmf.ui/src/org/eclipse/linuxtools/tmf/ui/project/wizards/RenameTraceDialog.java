/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
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
 * Implementation of a dialog box to rename a trace.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class RenameTraceDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfTraceElement fTrace;
    private Text fNewTraceNameText;
    private String fNewTraceName;
    private final IContainer fTraceFolder;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell The parent shell
     * @param trace The trace element to rename
     */
    public RenameTraceDialog(Shell shell, TmfTraceElement trace) {
        super(shell);
        fTrace = trace;
        TmfTraceFolder folder = (TmfTraceFolder) trace.getParent();
        fTraceFolder = folder.getResource();
        setTitle(Messages.RenameTraceDialog_DialogTitle);
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

        createNewTraceNameGroup(composite);
        return composite;
    }

    private void createNewTraceNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Old trace name label
        Label oldTraceLabel = new Label(folderGroup, SWT.NONE);
        oldTraceLabel.setFont(font);
        oldTraceLabel.setText(Messages.RenameTraceDialog_TraceName);

        // Old trace name field
        Text oldTraceName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        oldTraceName.setLayoutData(data);
        oldTraceName.setFont(font);
        oldTraceName.setText(fTrace.getName());
        oldTraceName.setEnabled(false);

        // New trace name label
        Label newTaceLabel = new Label(folderGroup, SWT.NONE);
        newTaceLabel.setFont(font);
        newTaceLabel.setText(Messages.RenameTraceDialog_TraceNewName);

        // New trace name entry field
        fNewTraceNameText = new Text(folderGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewTraceNameText.setLayoutData(data);
        fNewTraceNameText.setFont(font);
        fNewTraceNameText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateNewTraceName();
            }
        });
    }

    /**
     * Returns the new trace name
     * @return the new trace name
     */
    public String getNewTraceName() {
        return fNewTraceName;
    }

    private void validateNewTraceName() {

        fNewTraceName = fNewTraceNameText.getText();
        IWorkspace workspace = fTraceFolder.getWorkspace();
        IStatus nameStatus = workspace.validateName(fNewTraceName, IResource.FOLDER);

        if ("".equals(fNewTraceName)) { //$NON-NLS-1$
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_EmptyNameError, null));
            return;
        }

        if (!nameStatus.isOK()) {
            updateStatus(nameStatus);
            return;
        }

        IPath path = new Path(fNewTraceName);
        if (fTraceFolder.getFolder(path).exists() || fTraceFolder.getFile(path).exists()) {
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_ExistingNameError, null));
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
        IResource trace = renameTrace(fNewTraceNameText.getText());
        if (trace == null) {
            return;
        }
        setSelectionResult(new IResource[] { trace });
        super.okPressed();
    }

    private IResource renameTrace(final String newName) {

        final IPath oldPath = fTrace.getResource().getFullPath();
        final IPath newPath = oldPath.append("../" + newName); //$NON-NLS-1$

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    // Close the trace if open
                    fTrace.closeEditors();

                    if (fTrace.getResource() instanceof IFolder) {
                        IFolder folder = (IFolder) fTrace.getResource();
                        IFile bookmarksFile = fTrace.getBookmarksFile();
                        IFile newBookmarksFile = folder.getFile(bookmarksFile.getName().replace(fTrace.getName(), newName));
                        if (bookmarksFile.exists()) {
                            if (!newBookmarksFile.exists()) {
                                IPath newBookmarksPath = newBookmarksFile.getFullPath();
                                bookmarksFile.move(newBookmarksPath, IResource.FORCE | IResource.SHALLOW, null);
                            }
                        }
                    }

                    fTrace.renameSupplementaryFolder(newName);
                    fTrace.getResource().move(newPath, IResource.FORCE | IResource.SHALLOW, null);
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

        return fTrace.getResource();
    }

}
