/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog to set a name and a command when creating a custom
 * analysis entry.
 *
 * @author Philippe Proulx
 */
@NonNullByDefault({})
class AddAnalysisDialog extends Dialog {

    private final String title;
    private String fName = ""; //$NON-NLS-1$
    private String fCommand = ""; //$NON-NLS-1$
    private final IInputValidator fNameValidator;
    private final IInputValidator fCommandValidator;
    private Button fOkButton;
    private Text fNameText;
    private Text fCommandText;
    private Label fNameErrorLabel;
    private Label fCommandErrorLabel;

    public AddAnalysisDialog(Shell parentShell,
            String dialogTitle,
            IInputValidator nameValidator,
            IInputValidator commandValidator) {
        super(parentShell);
        this.title = dialogTitle;
        fNameValidator = nameValidator;
        fCommandValidator = commandValidator;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            fName = fNameText.getText();
            fCommand = fCommandText.getText();
        } else {
            fName = null;
            fCommand = null;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        fOkButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        validateInputs();
        fNameText.setFocus();
    }

    private static void createSubtitleLabel(Composite parent, String text) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setText(text + ':');
        final FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD);
        final Font boldFont = boldDescriptor.createFont(parent.getDisplay());
        label.setFont(boldFont);
        label.addDisposeListener(event -> boldDescriptor.destroyFont(boldFont));
    }

    private static Label createErrorLabel(Composite parent) {
        final Label label = new Label(parent, SWT.WRAP);
        Color color = new Color(parent.getDisplay(), 0xe7, 0x4c, 0x3c);
        label.setForeground(color);
        final FontDescriptor fd = FontDescriptor.createFrom(parent.getFont());
        Font font = fd.createFont(parent.getDisplay());
        label.setFont(font);

        label.addDisposeListener(e -> {
            color.dispose();
            fd.destroyFont(font);
        });

        return label;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        final Composite composite = (Composite) super.createDialogArea(parent);

        // create label for name text
        createSubtitleLabel(composite, Messages.AddAnalysisDialog_Name);

        // create name text
        fNameText = new Text(composite, getInputTextStyle());
        fNameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fNameText.addModifyListener(e -> validateInputs());

        // create name error text
        fNameErrorLabel = createErrorLabel(composite);

        // spacer
        new Label(composite, SWT.WRAP);

        // create label for command text
        createSubtitleLabel(composite, Messages.AddAnalysisDialog_Command);

        // create command text
        fCommandText = new Text(composite, getInputTextStyle());
        fCommandText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        final Font mono = new Font(parent.getDisplay(), "Monospace", 9, SWT.NONE); //$NON-NLS-1$
        fCommandText.setFont(mono);
        fCommandText.addModifyListener(e -> validateInputs());
        fCommandText.addDisposeListener(e -> mono.dispose());

        // create command error text
        fCommandErrorLabel = createErrorLabel(composite);

        applyDialogFont(composite);
        return composite;
    }

    @Override
    public void create() {
        super.create();
        Shell shell = getShell();
        shell.setMinimumSize(shell.getSize());
    }

    /**
     * Returns the value of the name text.
     *
     * @return the name text's value
     */
    public String getName() {
        return fName;
    }

    /**
     * Returns the value of the command text.
     *
     * @return the command text's value
     */
    public String getCommand() {
        return fCommand;
    }

    protected boolean validateInput(IInputValidator validator, Text text, Label errorLabel) {
        final String errMsg = validator.isValid(text.getText());
        setErrorLabel(errorLabel, errMsg);

        return errMsg == null;
    }

    protected void validateInputs() {
        boolean valid = true;

        valid &= validateInput(fNameValidator, fNameText, fNameErrorLabel);
        valid &= validateInput(fCommandValidator, fCommandText, fCommandErrorLabel);
        fOkButton.setEnabled(valid);
    }

    protected void setErrorLabel(Label label, String errorMessage) {
        if (label != null && !label.isDisposed()) {
            label.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
            final boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
            label.setEnabled(hasError);
            label.setVisible(hasError);
            label.getParent().update();
            Control button = getButton(IDialogConstants.OK_ID);

            if (button != null) {
                button.setEnabled(errorMessage == null);
            }
        }
    }

    protected int getInputTextStyle() {
        return SWT.SINGLE | SWT.BORDER;
    }
}
