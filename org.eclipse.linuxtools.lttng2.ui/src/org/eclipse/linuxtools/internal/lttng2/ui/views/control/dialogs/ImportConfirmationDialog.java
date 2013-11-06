/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * Dialog box for collecting session creation information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportConfirmationDialog extends Dialog implements IImportConfirmationDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String IMPORT_ICON_FILE = "icons/elcl16/import_trace.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The radio button for selecting the overwrite action
     */
    private Button fOverwriteButton = null;
    /**
     * The radio button for selecting the renaming action
     */
    private Button fRenameButton = null;
    /**
     * The text widget for the session name
     */
    private Text fNewTraceNameText = null;
    /**
     * The trace name which already exists in the project
     */
    private String fTraceName = null;
    /**
     * The session name string.
     */
    private String fNewTraceName = null;
    /**
     * Flag whether default location (path) shall be used or not
     */
    private boolean fIsOverride = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public ImportConfirmationDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public void setTraceName(String name) {
        fTraceName = name;
    }

    @Override
    public String getNewTraceName() {
        return fNewTraceName;
    }

    @Override
    public boolean isOverwrite() {
        return fIsOverride;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_ImportDialogConfirmationTitle);
        newShell.setImage(Activator.getDefault().loadIcon(IMPORT_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
       Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label sessionNameLabel = new Label(dialogComposite, SWT.RIGHT);
        sessionNameLabel.setText(Messages.TraceControl_ImportDialogTraceAlreadyExistError + ": " + fTraceName); //$NON-NLS-1$

        fOverwriteButton = new Button(dialogComposite, SWT.RADIO);
        fOverwriteButton.setText(Messages.TraceControl_ImportDialogConfirmationOverwriteLabel);

        fOverwriteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fNewTraceNameText.setEnabled(false);
                fNewTraceNameText.setText(fTraceName);
            }
        });

        fRenameButton = new Button(dialogComposite, SWT.RADIO);
        fRenameButton.setText(Messages.TraceControl_ImportDialogConfirmationRenameLabel);

        fRenameButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fNewTraceNameText.setEnabled(true);
            }
        });

        fNewTraceNameText = new Text(dialogComposite, SWT.NONE);
        fNewTraceNameText.setToolTipText(Messages.TraceControl_ImportDialogConfirmationNewNameLabel);
        fNewTraceNameText.setText(fTraceName);

        // Default
        fOverwriteButton.setSelection(true);
        fNewTraceNameText.setEnabled(false);


        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);

        fNewTraceNameText.setLayoutData(data);

        getShell().setMinimumSize(new Point(300, 150));

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {

        fIsOverride = fOverwriteButton.getSelection();

        if (fIsOverride) {
            // new name is old name
            fNewTraceName = fTraceName;
        } else {
            fNewTraceName = fNewTraceNameText.getText();
        }

        // Check for invalid names
        if (!fNewTraceName.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_ImportDialogConfirmationTitle,
                    Messages.TraceControl_InvalidTraceNameError + " (" + fNewTraceName + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }
}
