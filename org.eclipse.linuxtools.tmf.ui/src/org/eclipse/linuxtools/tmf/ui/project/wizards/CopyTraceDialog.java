/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.linuxtools.tmf.ui.project.wizards;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.osgi.util.NLS;
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
 * Implementation of the copy trace dialog box.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class CopyTraceDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfTraceElement fTrace;
    private Text fNewTraceName;
    private final IFolder fTraceFolder;
    private final TmfProjectElement fProject;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * @param shell The parent shell
     * @param trace The trace model element.
     */
    public CopyTraceDialog(Shell shell, TmfTraceElement trace) {
        super(shell);
        fTrace = trace;
        TmfTraceFolder folder = (TmfTraceFolder) trace.getParent();
        fTraceFolder = folder.getResource();
        fProject = trace.getProject();
        setTitle(Messages.CopyTraceDialog_DialogTitle);
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
        oldTraceLabel.setText(Messages.CopyTraceDialog_TraceName);

        // Old trace name field
        Text oldTraceName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        oldTraceName.setLayoutData(data);
        oldTraceName.setFont(font);
        oldTraceName.setText(fTrace.getName());
        oldTraceName.setEnabled(false);

        // New trace name label
        Label newTraceLabel = new Label(folderGroup, SWT.NONE);
        newTraceLabel.setFont(font);
        newTraceLabel.setText(Messages.CopyTraceDialog_TraceNewName);

        // New trace name entry field
        fNewTraceName = new Text(folderGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewTraceName.setLayoutData(data);
        fNewTraceName.setFont(font);
        fNewTraceName.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateNewTraceName();
            }
        });
    }

    private void validateNewTraceName() {

        String name = fNewTraceName.getText();
        IWorkspace workspace = fTraceFolder.getWorkspace();
        IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

        if ("".equals(name)) { //$NON-NLS-1$
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_EmptyNameError, null));
            return;
        }

        if (!nameStatus.isOK()) {
            updateStatus(nameStatus);
            return;
        }

        IPath path = new Path(name);
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
        IResource trace = copyTrace(fNewTraceName.getText());
        if (trace == null) {
            return;
        }
        setSelectionResult(new IResource[] { trace });
        super.okPressed();

        if (fProject != null) {
            fProject.refresh();
        }
    }

    private IResource copyTrace(final String newName) {

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    fTrace.copy(newName, true);
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
            MessageDialog.openError(getShell(), "", NLS.bind("", exception.getTargetException().getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } catch (RuntimeException exception) {
            return null;
        }

        return fTrace.getResource();

    }

}
