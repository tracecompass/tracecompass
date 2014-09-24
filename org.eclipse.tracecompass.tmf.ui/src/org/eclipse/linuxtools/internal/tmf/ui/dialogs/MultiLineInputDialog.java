/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple input dialog for soliciting an input string from the user.
 *
 * Overrides InputDialog to support multiple line text input.
 *
 * @author Patrick Tassé
 */
public class MultiLineInputDialog extends InputDialog {

    private final String dialogMessage;

    /* flag to indicate if CR can be used to submit the dialog */
    private boolean submitOnCR = true;

    /**
     * Creates a multi line input dialog.
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param initialValue
     *            the initial input value, or <code>null</code> if none (equivalent to the empty string)
     * @param validator
     *            an input validator, or <code>null</code> if none
     */
    public MultiLineInputDialog(Shell parentShell, String dialogTitle,
            String dialogMessage, String initialValue, IInputValidator validator) {
        super(parentShell, dialogTitle, null, initialValue, validator);
        this.dialogMessage = dialogMessage;
    }

    /**
     * Creates a multi line input dialog with a not-empty text validator.
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param initialValue
     *            the initial input value, or <code>null</code> if none (equivalent to the empty string)
     */
    public MultiLineInputDialog(Shell parentShell, String dialogTitle,
            String dialogMessage, String initialValue) {
        super(parentShell, dialogTitle, null, initialValue, new NotEmptyValidator());
        this.dialogMessage = dialogMessage;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        final Text text = getText();

        /* create dialog message label here instead because default implementation uses GRAB_VERTICAL */
        if (dialogMessage != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(dialogMessage);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());
            label.moveAbove(text);
        }

        /* modify text layout data here because default implementation doesn't fill vertically */
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = convertHorizontalDLUsToPixels(250);
        gridData.heightHint = convertHeightInCharsToPixels(3);
        text.setLayoutData(gridData);

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.CR) {
                    if (submitOnCR) {
                        /* submit the dialog */
                        e.doit = false;
                        okPressed();
                        return;
                    }
                } else if (e.character == SWT.TAB) {
                    /* don't insert a tab character in the text */
                    e.doit = false;
                    text.traverse(SWT.TRAVERSE_TAB_NEXT);
                }
                /* don't allow CR to submit anymore */
                submitOnCR = false;
            }
        });

        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                /* don't allow CR to submit anymore */
                submitOnCR = false;
            }
        });

        return composite;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);

        /* set the shell minimum size */
        Point clientArea = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Rectangle trim = getShell().computeTrim(0, 0, clientArea.x, clientArea.y);
        getShell().setMinimumSize(trim.width, trim.height);

        return control;
    }

    @Override
    protected int getInputTextStyle() {
        return SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private static class NotEmptyValidator implements IInputValidator {
        @Override
        public String isValid(String newText) {
            return (newText == null || newText.trim().length() == 0) ? " " : null; //$NON-NLS-1$
        }
    }

}
