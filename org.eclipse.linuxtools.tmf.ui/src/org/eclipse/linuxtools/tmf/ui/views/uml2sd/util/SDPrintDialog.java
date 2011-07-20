/********************************************************************** 
 * Copyright (c) 2005, 2008, 2011  IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: SDPrintDialog.java,v 1.3 2008/01/24 02:28:52 apnan Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF 
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
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
 * @author sveyrier
 */
public class SDPrintDialog extends Dialog {

    protected SDWidget view;

    protected SDPrintDialogUI dialogUI;

    protected String errorMessage = null;
    protected Label messageLabel = null;
    protected boolean isPageComplete = true;

    public SDPrintDialog(Shell s, SDWidget v) {
        super(s);
        view = v;

        dialogUI = new SDPrintDialogUI(s, view);
        dialogUI.setParentDialog(this);
    }

    @Override
    protected Control createDialogArea(Composite p) {
        p.getShell().setText(SDMessages._114);
        Composite parent = (Composite) super.createDialogArea(p);

        dialogUI.createDialogArea(parent);

        // bug 195026
        messageLabel = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = 6;
        messageLabel.setLayoutData(gridData);
        setErrorMessage(errorMessage);

        return parent;
    }

    @Override
    protected void okPressed() {

        if (dialogUI.okPressed())
            super.okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        super.createButtonsForButtonBar(parent);
        createButton(parent, IDialogConstants.CLIENT_ID, SDMessages._115, false);

        getButton(IDialogConstants.CLIENT_ID).addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                dialogUI.printButtonSelected();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

        });

        updateButtons();
    }

    public SDPrintDialogUI getDialogUI() {
        return dialogUI;
    }

    public void setErrorMessage(String message) {
        errorMessage = message;
        if (messageLabel != null) {
            if (errorMessage == null) {
                messageLabel.setText(""); //$NON-NLS-1$
            } else {
                messageLabel.setText(errorMessage);
            }
        }
    }

    public void setPageComplete(boolean complete) {
        isPageComplete = complete;
        updateButtons();
    }

    public void updateButtons() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (isPageComplete) {
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
