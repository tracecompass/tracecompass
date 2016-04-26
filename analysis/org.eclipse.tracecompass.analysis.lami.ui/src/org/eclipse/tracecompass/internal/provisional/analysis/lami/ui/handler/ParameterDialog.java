/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Adaptation of {@link InputDialog}, to show the command in a grayed out,
 * read-only input field, and a editable input field for the user to add extra
 * parameters.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault({})
class ParameterDialog extends Dialog {

    private static final Color GRAY_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);

    private String fTitle;
    private String fMessage;
    private String fValue = "";//$NON-NLS-1$
    private IInputValidator fValidator;
    private Button fOkButton;
    private Text fText;
    private Text fErrorMessageText;
    private String fErrorMessage;

    private Text fBaseCommandText;
    private final String fBaseCommand;

    public ParameterDialog(Shell parentShell,
            String dialogTitle,
            String dialogMessage,
            String baseCommand,
            IInputValidator validator) {
        super(parentShell);
        fTitle = dialogTitle;
        fMessage = dialogMessage;
        fValue = "";//$NON-NLS-1$
        fValidator = validator;
        fBaseCommand = baseCommand;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            fValue = fText.getText();
        } else {
            fValue = null;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (fTitle != null) {
            shell.setText(fTitle);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        fOkButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        fText.setFocus();
        if (fValue != null) {
            fText.setText(fValue);
            fText.selectAll();
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);

        Label label = new Label(composite, SWT.WRAP);
        label.setText(Messages.ParameterDialog_BaseCommand + ':');

        fBaseCommandText = new Text(composite, getInputTextStyle() | SWT.READ_ONLY);
        fBaseCommandText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fBaseCommandText.setText(fBaseCommand);
        fBaseCommandText.setForeground(GRAY_COLOR);

        // create message
        if (fMessage != null) {
            label = new Label(composite, SWT.WRAP);
            label.setText(fMessage);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }



        fText = new Text(composite, getInputTextStyle());
        fText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fText.addModifyListener(e -> validateInput());
        fErrorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        fErrorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fErrorMessageText.setBackground(fErrorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        setErrorMessage(fErrorMessage);

        applyDialogFont(composite);
        return composite;
    }

    /**
     * Returns the ok button.
     *
     * @return the ok button
     */
    protected Button getOkButton() {
        return fOkButton;
    }

    /**
     * Returns the string typed into this input dialog.
     *
     * @return the input string
     */
    public String getValue() {
        return fValue;
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        String errMsg = null;
        if (fValidator != null) {
            errMsg = fValidator.isValid(fText.getText());
        }
        setErrorMessage(errMsg);
    }

    /**
     * Sets or clears the error message.
     * If not <code>null</code>, the OK button is disabled.
     *
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     */
    private void setErrorMessage(String errorMessage) {
        this.fErrorMessage = errorMessage;
        if (fErrorMessageText != null && !fErrorMessageText.isDisposed()) {
            fErrorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
            boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
            fErrorMessageText.setEnabled(hasError);
            fErrorMessageText.setVisible(hasError);
            fErrorMessageText.getParent().update();
            Control button = getButton(IDialogConstants.OK_ID);
            if (button != null) {
                button.setEnabled(errorMessage == null);
            }
        }
    }

    /**
     * Returns the style bits that should be used for the input text field.
     * Defaults to a single line entry. Subclasses may override.
     *
     * @return the integer style bits that should be used when creating the
     *         input text
     */
    protected int getInputTextStyle() {
        return SWT.SINGLE | SWT.BORDER;
    }
}
