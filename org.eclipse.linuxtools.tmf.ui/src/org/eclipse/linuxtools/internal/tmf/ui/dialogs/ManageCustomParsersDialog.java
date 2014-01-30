/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards.CustomTxtParserWizard;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards.CustomXmlParserWizard;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class ManageCustomParsersDialog extends Dialog {

    private static final Image image = Activator.getDefault().getImageFromPath("/icons/etool16/customparser_wizard.gif"); //$NON-NLS-1$

    Button txtButton;
    Button xmlButton;
    List parserList;
    Button newButton;
    Button editButton;
    Button deleteButton;
    Button importButton;
    Button exportButton;

    /**
     * Constructor
     *
     * @param parent
     *            Parent shell of this dialog
     */
    public ManageCustomParsersDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.RESIZE | SWT.MAX | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.ManageCustomParsersDialog_DialogHeader);
        getShell().setImage(image);

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite listContainer = new Composite(composite, SWT.NONE);
        listContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout lcgl = new GridLayout();
        lcgl.marginHeight = 0;
        lcgl.marginWidth = 0;
        listContainer.setLayout(lcgl);

        Composite radioContainer = new Composite(listContainer, SWT.NONE);
        GridLayout rcgl = new GridLayout(2, true);
        rcgl.marginHeight = 0;
        rcgl.marginWidth = 0;
        radioContainer.setLayout(rcgl);

        txtButton = new Button(radioContainer, SWT.RADIO);
        txtButton.setText(Messages.ManageCustomParsersDialog_TextButtonLabel);
        txtButton.setSelection(true);
        txtButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                fillParserList();
            }
        });

        xmlButton = new Button(radioContainer, SWT.RADIO);
        xmlButton.setText("XML"); //$NON-NLS-1$
        xmlButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                fillParserList();
            }
        });

        parserList = new List(listContainer, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        parserList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parserList.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (parserList.getSelectionCount() == 0) {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    exportButton.setEnabled(false);
                } else {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    exportButton.setEnabled(true);
                }
            }
        });

        Composite buttonContainer = new Composite(composite, SWT.NULL);
        buttonContainer.setLayout(new GridLayout());
        buttonContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        newButton = new Button(buttonContainer, SWT.PUSH);
        newButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        newButton.setText(Messages.ManageCustomParsersDialog_NewButtonLabel);
        newButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog dialog = null;
                if (txtButton.getSelection()) {
                    dialog = new WizardDialog(getShell(), new CustomTxtParserWizard());
                } else if (xmlButton.getSelection()) {
                    dialog = new WizardDialog(getShell(), new CustomXmlParserWizard());
                }
                if (dialog != null) {
                    dialog.open();
                    if (dialog.getReturnCode() == Window.OK) {
                        fillParserList();
                    }
                }
            }
        });

        editButton = new Button(buttonContainer, SWT.PUSH);
        editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        editButton.setText(Messages.ManageCustomParsersDialog_EditButtonLabel);
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog dialog = null;
                if (txtButton.getSelection()) {
                    dialog = new WizardDialog(getShell(),
                            new CustomTxtParserWizard(CustomTxtTraceDefinition.load(parserList.getSelection()[0])));
                } else if (xmlButton.getSelection()) {
                    dialog = new WizardDialog(getShell(),
                            new CustomXmlParserWizard(CustomXmlTraceDefinition.load(parserList.getSelection()[0])));
                }
                if (dialog != null) {
                    dialog.open();
                    if (dialog.getReturnCode() == Window.OK) {
                        fillParserList();
                    }
                }
            }
        });

        deleteButton = new Button(buttonContainer, SWT.PUSH);
        deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        deleteButton.setText(Messages.ManageCustomParsersDialog_DeleteButtonLabel);
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean confirm = MessageDialog.openQuestion(
                        getShell(),
                        Messages.ManageCustomParsersDialog_DeleteParserDialogHeader,
                        Messages.ManageCustomParsersDialog_DeleteConfirmation + parserList.getSelection()[0] + "?"); //$NON-NLS-1$
                if (confirm) {
                    if (txtButton.getSelection()) {
                        CustomTxtTraceDefinition.delete(parserList.getSelection()[0]);
                    } else if (xmlButton.getSelection()) {
                        CustomXmlTraceDefinition.delete(parserList.getSelection()[0]);
                    }
                    fillParserList();
                }
            }
        });

        new Label(buttonContainer, SWT.NONE); // filler

        importButton = new Button(buttonContainer, SWT.PUSH);
        importButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        importButton.setText(Messages.ManageCustomParsersDialog_ImportButtonLabel);
        importButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
                dialog.setText(Messages.ManageCustomParsersDialog_ImportParserSelection);
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    CustomTraceDefinition[] defs = null;
                    if (txtButton.getSelection()) {
                        defs = CustomTxtTraceDefinition.loadAll(path);
                    } else if (xmlButton.getSelection()) {
                        defs = CustomXmlTraceDefinition.loadAll(path);
                    }
                    if (defs != null && defs.length > 0) {
                        for (CustomTraceDefinition def : defs) {
                            def.save();
                        }
                        fillParserList();
                    }
                }
            }
        });

        exportButton = new Button(buttonContainer, SWT.PUSH);
        exportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        exportButton.setText(Messages.ManageCustomParsersDialog_ExportButtonLabel);
        exportButton.setEnabled(false);
        exportButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setText(Messages.ManageCustomParsersDialog_ExportParserSelection + parserList.getSelection()[0]);
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    CustomTraceDefinition def = null;
                    if (txtButton.getSelection()) {
                        def = CustomTxtTraceDefinition.load(parserList.getSelection()[0]);
                    } else if (xmlButton.getSelection()) {
                        def = CustomXmlTraceDefinition.load(parserList.getSelection()[0]);
                    }
                    if (def != null) {
                        def.save(path);
                    }
                }
            }
        });

        fillParserList();

        getShell().setMinimumSize(300, 275);
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    private void fillParserList() {
        parserList.removeAll();
        if (txtButton.getSelection()) {
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                parserList.add(def.definitionName);
            }
        } else if (xmlButton.getSelection()) {
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                parserList.add(def.definitionName);
            }
        }
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        exportButton.setEnabled(false);
    }

}
