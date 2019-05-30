/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.tracecompass.internal.lttng2.control.core.LttngProfileManager;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.tmf.ui.dialog.DirectoryDialogFactory;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * LTTng control remote profile preferences page.
 *
 * @author Bernd Hufmann
 */
public class ControlRemoteProfilesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /** Preference page ID */
    public static final String ID = "org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences.ControlRemoteProfilesPreferencePage"; //$NON-NLS-1$

    private CheckboxTreeViewer fFolderViewer;

    private Button fDeleteButton = null;
    private Button fImportButton = null;
    private Button fExportButton = null;

    @Override
    public void init(IWorkbench workbench) {
        // Do nothing
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite;
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));

        final FilteredTree filteredTree = new FilteredTree(composite,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aParent, int style) {
                fFolderViewer = LTTngProfileViewer.createLTTngProfileViewer(aParent, style);
                return fFolderViewer;
            }
        };

        filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttonComposite = createVerticalButtonBar(composite);
        buttonComposite.setLayout(new GridLayout());
        buttonComposite.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, false, false));

        fFolderViewer.addCheckStateListener(event -> enableButtons());

        return composite;
    }

    private Composite createVerticalButtonBar(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);

        fDeleteButton = createVerticalButton(buttonComposite, Messages.TraceControl_DeleteButtonText);
        fDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] checkedElements = fFolderViewer.getCheckedElements();
                StringBuffer files = new StringBuffer();
                for (Object element : checkedElements) {
                    if (element instanceof File) {
                        files.append(((File) element).toString()).append("\n"); //$NON-NLS-1$
                    }
                }

                boolean delete = MessageDialog.openConfirm(getShell(),
                        Messages.TraceControl_DeleteProfileTitle,
                        Messages.TraceControl_DeleteQuery+ "\n" + files.toString()); //$NON-NLS-1$

                if (!delete) {
                    return;
                }

                for (Object element : checkedElements) {
                    if (element instanceof File) {
                        File sourceFile = (File) element;
                        Path source = FileSystems.getDefault().getPath(sourceFile.getAbsolutePath());
                        try {
                            Files.delete(source);
                        } catch (IOException e1) {
                            MessageDialog.openError(getShell(),
                                    Messages.TraceControl_DeleteProfileTitle,
                                    "Error deleting profile:\n" + e1.toString());  //$NON-NLS-1$
                        }
                    }
                }
                fFolderViewer.setInput(LTTngProfileViewer.getViewerInput());
                enableButtons();
            }
        });

        fImportButton = createVerticalButton(buttonComposite, Messages.TraceControl_ImportButtonText);
        fExportButton = createVerticalButton(buttonComposite, Messages.TraceControl_ExportButtonText);

        fImportButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.OPEN);
                dialog.setText(Messages.TraceControl_ImportProfileTitle);
                dialog.setFilterExtensions(new String[] { "*.lttng", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String sourceFile = dialog.open();
                if (sourceFile != null) {
                    Path source = FileSystems.getDefault().getPath(sourceFile);
                    Path destPath = FileSystems.getDefault().getPath(LttngProfileManager.getProfilePath().toFile().toString());
                    copyProfileFile(source, destPath, Messages.TraceControl_ImportProfileTitle);
                    fFolderViewer.setInput(LTTngProfileViewer.getViewerInput());
                }
            }
        });

        fExportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = DirectoryDialogFactory.create(Display.getCurrent().getActiveShell());
                dialog.setText(Messages.TraceControl_ExportProfileTitle);
                String path = dialog.open();
                if (path != null) {
                    Object[] checkedElements = fFolderViewer.getCheckedElements();
                    for (Object element : checkedElements) {
                        if (element instanceof File) {
                            File sourceFile = (File) element;
                            Path source = FileSystems.getDefault().getPath(sourceFile.getAbsolutePath());
                            Path destPath = FileSystems.getDefault().getPath(path);
                            copyProfileFile(source, destPath, Messages.TraceControl_ExportProfileTitle);
                        }
                    }
                }
            }
        });

        enableButtons();
        return buttonComposite;
    }

    private static Button createVerticalButton(Composite parent, String text) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button.setText(text);
        return button;
    }

    private void enableButtons() {
        Object[] checked = fFolderViewer.getCheckedElements();
        boolean enabled = (checked != null) && (checked.length > 0);
        fDeleteButton.setEnabled(enabled);
        fExportButton.setEnabled(enabled);
        fImportButton.setEnabled(true);
    }

    private void copyProfileFile(Path source, Path destPath, String errorTitle) {
        Path destFile = destPath.resolve(source.getFileName());
        if (destFile.toFile().exists()) {
            boolean overwrite = MessageDialog.openConfirm(getShell(),
                    Messages.TraceControl_ProfileAlreadyExists,
                    NLS.bind(Messages.TraceControl_OverwriteQuery, destFile.getFileName()));

            if (!overwrite) {
                return;
            }
        }
        try {
            Files.copy(source, destFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            MessageDialog.openError(getShell(),
                    errorTitle,
                    "Error copying profile:\n" + e1.toString()); //$NON-NLS-1$
        }
    }

}
