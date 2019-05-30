/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of the Trim trace dialog box.
 * <p>
 *
 * @since 4.1
 */
public class TrimTraceDialog extends SelectionStatusDialog {

    private Text fNewElementName;
    private final TmfCommonProjectElement fElement;
    private String fResult;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param shell
     *            The parent shell
     * @param element
     *            The element to rename
     */
    public TrimTraceDialog(Shell shell, TmfCommonProjectElement element) {
        this(shell, element, Messages.TrimTraceDialog_ExportTrimmedTrace);
    }

    /**
     * Constructor.
     *
     * @param shell
     *            The parent shell
     * @param element
     *            The element to rename
     * @param title
     *            the title of the box
     */
    public TrimTraceDialog(Shell shell, TmfCommonProjectElement element, String title) {
        super(shell);
        setTitle(title);
        fElement = element;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createNewTraceNameGroup(composite);
        return composite;
    }

    private void createNewTraceNameGroup(Composite parent) {
        setStatusLineAboveButtons(true);
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String name = fElement.getName();

        // New trace name label
        Label newTaceLabel = new Label(folderGroup, SWT.NONE);
        newTaceLabel.setFont(font);
        newTaceLabel.setText(Messages.RenameTraceDialog_TraceNewName);

        // New trace name entry field

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        Text newElementName = new Text(folderGroup, SWT.BORDER);
        newElementName.setLayoutData(data);
        newElementName.setFont(font);
        newElementName.setFocus();
        newElementName.setText(name);
        newElementName.setSelection(0, name.length());
        newElementName.addListener(SWT.Modify, event -> validateNewTraceName());
        fNewElementName = newElementName;
        validateNewTraceName();
    }

    private void validateNewTraceName() {

        String newTraceName = fNewElementName.getText();
        TmfCommonProjectElement element = fElement;
        IWorkspace workspace = element.getResource().getWorkspace();
        IStatus nameStatus = workspace.validateName(newTraceName, IResource.FOLDER);

        if ("".equals(newTraceName)) { //$NON-NLS-1$
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_EmptyNameError, null));
            return;
        }

        if (!nameStatus.isOK()) {
            updateStatus(nameStatus);
            return;
        }

        IContainer parentFolder = element.getResource().getParent();
        if (parentFolder.findMember(newTraceName) != null) {
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                    Messages.Dialog_ExistingNameError, null));
            return;
        }
        TmfTraceFolder tracesFolderElement = element.getProject().getTracesFolder();
        if (tracesFolderElement != null) {
            IFolder tracesFolder = tracesFolderElement.getResource();
            IPath traceDestinationPath = new Path(newTraceName);
            if (tracesFolder.getFolder(traceDestinationPath).exists()) {
                updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                        Messages.Dialog_ExistingNameError, null));
                return;
            }
        }
        updateStatus(new Status(IStatus.OK, Activator.PLUGIN_ID, "")); //$NON-NLS-1$
    }

    @Override
    protected void computeResult() {
        // do nothing
    }

    @Override
    protected void okPressed() {
        fResult = fNewElementName.getText();
        super.okPressed();
    }

    @Override
    public Object[] getResult() {
        String result = fResult;
        if (result != null) {
            return new String[] { result };
        }
        return new String[0];
    }
}
