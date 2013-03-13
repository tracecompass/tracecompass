/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.ui.views.project.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.gdbtrace.ui.GdbTraceUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Dialog implementation for the Select Trace Executable command
 *
 * @author Francois Chouinard
 */
public class SelectTraceExecutableDialog extends SelectionStatusDialog {

    private final IStatus OK_STATUS = new Status(IStatus.OK, GdbTraceUIPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    private final IStatus PATH_ERROR_STATUS = new Status(IStatus.ERROR, GdbTraceUIPlugin.PLUGIN_ID, Messages.SelectTraceExecutableDialog_Message);
    private final IStatus BINARY_ERROR_STATUS = new Status(IStatus.ERROR, GdbTraceUIPlugin.PLUGIN_ID, Messages.SelectTraceExecutableDialog_BinaryError);

    private Text fExecutableNameEntry;
    private IPath fExecutablePath;

    /**
     * Creates a SelectTraceExecutableDialog
     *
     * @param parentShell parent of the new dialog
     */
    public SelectTraceExecutableDialog(Shell parentShell) {
        super(parentShell);
        setTitle(Messages.SelectTraceExecutableDialog_Title);
        setStatusLineAboveButtons(true);
        setHelpAvailable(false);
    }

    @Override
    protected void computeResult() {
        setResult(Arrays.asList(new IPath[] { fExecutablePath }));
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createFolderNameGroup(composite);

        setStatusLineAboveButtons(true);
        updateStatus(PATH_ERROR_STATUS);

        return composite;
    }

    /**
     * Creates the folder name specification controls.
     *
     * @param parent the parent composite
     */
    private void createFolderNameGroup(Composite parent) {
        final Shell shell = parent.getShell();
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Executable name label
        Label label = new Label(composite, SWT.NONE);
        label.setFont(font);
        label.setText(Messages.SelectTraceExecutableDialog_ExecutableName);

        // Executable name entry field
        fExecutableNameEntry = new Text(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fExecutableNameEntry.setLayoutData(data);
        fExecutableNameEntry.setFont(font);
        fExecutableNameEntry.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fExecutablePath = Path.fromOSString(fExecutableNameEntry.getText());
                if (! validateExecutableName()) {
                    updateStatus(PATH_ERROR_STATUS);
                } else if (! validateExecutableBinary()) {
                    updateStatus(BINARY_ERROR_STATUS);
                } else {
                    updateStatus(OK_STATUS);
                }
            }
        });

        // Browse button
        Button browseExecutableButton = new Button(composite, SWT.NONE);
        browseExecutableButton.setText(Messages.SelectTraceExecutableDialog_Browse);
        browseExecutableButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell);
                String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
                dlg.setFilterPath(workspacePath);
                dlg.setText(Messages.SelectTraceExecutableDialog_ExecutablePrompt);
                String path = dlg.open();
                if (path != null) {
                    fExecutableNameEntry.setText(path);
                }
            }
        });

    }

    /**
     * Checks whether the executable location is valid.
     */
    private boolean validateExecutableName() {
        if (fExecutablePath != null) {
            File file = new File(fExecutablePath.toOSString());
            return file.exists() && file.isFile();
        }
        return false;
    }

    /**
     * Checks whether the executable location is a recognized executable.
     */
    private boolean validateExecutableBinary() {
        try {
            IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
            IBinaryParser.IBinaryFile binary = parser.getBinary(fExecutablePath);
            if (binary instanceof IBinaryParser.IBinaryObject) {
                return true;
            }
        } catch (CoreException e) {
        } catch (IOException e) {
        }
        return false;
    }

    /**
     * Returns the selected executable path.
     * @return the executable path
     */
    public IPath getExecutablePath() {
        return fExecutablePath;
    }

}