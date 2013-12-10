/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableList;

/**
 * <p>
 * Dialog box for selecting a command script. It parses the script and
 * provides a list of shell commands to be executed.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class OpenCommandScriptDialog extends Dialog implements ISelectCommandScriptDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String CREATE_SESSION_ICON_FILE = "icons/elcl16/add_button.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Dialog attributes
    private Control fControl = null;
    private Composite fDialogComposite = null;
    private Button fBrowseButton;
    private Label fFileNameLabel = null;
    private Text fFileNameText = null;

    // Output list of commands
    private List<String> fCommands = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public OpenCommandScriptDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    @NonNull public List<String> getCommands() {
        if (fCommands != null) {
            return fCommands;
        }
        return new ArrayList<>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected Control createContents(Composite parent) {
        fControl = super.createContents(parent);

        /* set the shell minimum size */
        Point clientArea = fControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Rectangle trim = getShell().computeTrim(0, 0, clientArea.x, clientArea.y);
        getShell().setMinimumSize(trim.width, trim.height);

        return fControl;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_ExecuteScriptDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(CREATE_SESSION_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group sessionGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        sessionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sessionGroup.setLayout(new GridLayout(6, true));

        fFileNameLabel = new Label(sessionGroup, SWT.RIGHT);
        fFileNameLabel.setText(Messages.TraceControl_ExecuteScriptSelectLabel);
        fFileNameText = new Text(sessionGroup, SWT.NONE);

        fBrowseButton = new Button(sessionGroup, SWT.PUSH);
        fBrowseButton.setText(Messages.TraceControl_ExecuteScriptBrowseText);
        fBrowseButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleFilePathBrowseButtonPressed(SWT.OPEN);
            }
        });

        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = false;
        fFileNameLabel.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        fFileNameText.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;

        // Initialize a empty list
        fCommands = new ArrayList<>();

        return fDialogComposite;
    }

    private void handleFilePathBrowseButtonPressed(int fileDialogStyle) {
        FileDialog dialog = new FileDialog(getShell(), fileDialogStyle | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { "*.*", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(Messages.TraceControl_ExecuteScriptDialogTitle);
        String selectedFileName = dialog.open();
        fFileNameText.setText(selectedFileName);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        // Validate input data
        String sessionPath = fFileNameText.getText();

        if (!"".equals(sessionPath)) { //$NON-NLS-1$
            ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
            try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(sessionPath, "r")) { //$NON-NLS-1$
                String line = rafile.getNextLine();
                while (line != null) {
                    builder.add(line);
                    line = rafile.getNextLine();
                }
            } catch (IOException e) {
                return;
            }
            fCommands = builder.build();
            super.okPressed();
        }
    }
}
