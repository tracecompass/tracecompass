/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;

/**
 * Dialog box for collecting parameter for loading a session.
 *
 * @author Bernd Hufmann
 */
public class SaveDialog extends Dialog implements ISaveDialog {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** The icon file for this dialog box. */
    public static final String EXPORT_ICON_FILE = "icons/elcl16/export_button.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;

    private Button fForceButton = null;

    private boolean fIsForce = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            - a shell for the display of the dialog
     */
    public SaveDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    public boolean isForce() {
        return fIsForce;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_SaveDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(EXPORT_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createOptionComposite();
        return fDialogComposite;
    }

    private void createOptionComposite() {
        Composite group = new Composite(fDialogComposite, SWT.BORDER);
        group.setLayout(new GridLayout(1, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fForceButton = new Button(group, SWT.CHECK);
        fForceButton.setSelection(true);
        fForceButton.setText(Messages.TraceControl_ForceButtonText);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        fIsForce = fForceButton.getSelection();
        super.okPressed();
    }

}
