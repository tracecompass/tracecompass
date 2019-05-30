/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of a dialog box to rename a folder.
 */
public class RenameFolderDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfTraceFolder fFolder;
    private Text fNewFolderNameText;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell The parent shell
     * @param folder The trace element to rename
     */
    public RenameFolderDialog(Shell shell, TmfTraceFolder folder) {
        super(shell);
        fFolder = folder;
        setTitle(Messages.RenameFolderDialog_DialogTitle);
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

        String name = fFolder.getName();

        // New trace name label
        Label newTaceLabel = new Label(folderGroup, SWT.NONE);
        newTaceLabel.setFont(font);
        newTaceLabel.setText(Messages.RenameFolderDialog_FolderNewName);

        // New trace name entry field
        fNewFolderNameText = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewFolderNameText.setLayoutData(data);
        fNewFolderNameText.setFont(font);
        fNewFolderNameText.setFocus();
        fNewFolderNameText.setText(name);
        fNewFolderNameText.setSelection(0, name.length());
        fNewFolderNameText.addListener(SWT.Modify, event -> validateNewFolderName());
    }

    private void validateNewFolderName() {

        String newFolderName = fNewFolderNameText.getText();
        IWorkspace workspace = fFolder.getResource().getWorkspace();
        IStatus nameStatus = workspace.validateName(newFolderName, IResource.FOLDER);

        if ("".equals(newFolderName)) { //$NON-NLS-1$
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_EmptyNameError, null));
            return;
        }

        if (!nameStatus.isOK()) {
            updateStatus(nameStatus);
            return;
        }

        IContainer parentFolder = fFolder.getResource().getParent();
        if (parentFolder.findMember(newFolderName) != null) {
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
        setSelectionResult(new String[] { fNewFolderNameText.getText() });
        super.okPressed();
    }

}
