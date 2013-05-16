/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements a dialog box for collecting printing information.
 *
 * @version 1.0
 * @author sveyrier
 */
public class SDPrintDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence dialog widget reference.
     */
    private SDWidget fSdView;
    /**
     * Sequence dialog print dialog UI
     */
    private SDPrintDialogUI fDialogUI;
    /**
     * Error message to display.
     */
    private String fErrorMessage = null;
    /**
     * A message label.
     */
    private Label fMessageLabel = null;
    /**
     * Flag whether the page is complete or not
     */
    private boolean fIsPageComplete = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     *
     * @param shell Shell reference
     * @param viewer Sequence diagram widget reference
     */
    public SDPrintDialog(Shell shell, SDWidget viewer) {
        super(shell);
        fSdView = viewer;

        fDialogUI = new SDPrintDialogUI(shell, fSdView);
        fDialogUI.setParentDialog(this);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    protected Control createDialogArea(Composite p) {
        p.getShell().setText(Messages.SequenceDiagram_Print);
        Composite parent = (Composite) super.createDialogArea(p);

        fDialogUI.createDialogArea(parent);

        fMessageLabel = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = 6;
        fMessageLabel.setLayoutData(gridData);
        setErrorMessage(fErrorMessage);

        return parent;
    }

    @Override
    protected void okPressed() {

        if (fDialogUI.okPressed()) {
            super.okPressed();
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        super.createButtonsForButtonBar(parent);
        createButton(parent, IDialogConstants.CLIENT_ID, Messages.SequenceDiagram_Printer, false);

        getButton(IDialogConstants.CLIENT_ID).addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fDialogUI.printButtonSelected();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        updateButtons();
    }

    /**
     * @return the dialog UI
     */
    public SDPrintDialogUI getDialogUI() {
        return fDialogUI;
    }

    /**
     * Sets the error message.
     *
     * @param message error message to set
     */
    public void setErrorMessage(String message) {
        fErrorMessage = message;
        if (fMessageLabel != null) {
            if (fErrorMessage == null) {
                fMessageLabel.setText(""); //$NON-NLS-1$
            } else {
                fMessageLabel.setText(fErrorMessage);
            }
        }
    }

    /**
     * Sets the page complete flag.
     * @param complete whether page is complete or not
     */
    public void setPageComplete(boolean complete) {
        fIsPageComplete = complete;
        updateButtons();
    }

    /**
     * Udates the button enable state.
     */
    public void updateButtons() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (fIsPageComplete) {
            if (okButton != null) {
                okButton.setEnabled(true);
            }
        } else {
            if (okButton != null) {
                okButton.setEnabled(false);
            }
        }
    }

}
