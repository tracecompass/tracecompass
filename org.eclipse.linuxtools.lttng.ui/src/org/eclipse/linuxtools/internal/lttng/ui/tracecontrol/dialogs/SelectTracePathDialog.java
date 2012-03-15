/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>SelectTracePathDialog</u></b>
 * <p>
 * Dialog box to configure and select a trace path.
 * </p>
 */
public class SelectTracePathDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private Text fPath = null;
    private String fPathName = null;
    private String fTracePathError = null;
    private Label fErrorLabel = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * 
     * @param parentShell The paren shell
     */
    public SelectTracePathDialog(Shell parentShell) {
        super(parentShell);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SeletctTracePathDialog_Title);    
        newShell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        //Main dialog panel
        Composite composite = new Composite(parent, SWT.RESIZE);
        GridLayout mainLayout = new GridLayout(1, true);
        composite.setLayout(mainLayout);
        
        fErrorLabel = new Label(composite, SWT.LEFT);
        fErrorLabel.setLayoutData(new GridData(GridData.FILL, 
                GridData.CENTER, true, false, 6, 1));
        fErrorLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
        
        Group group = new Group(composite, SWT.SHADOW_OUT);
        group.setText(Messages.ConfigureTraceDialog_Trace_Location);
        group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        
        GridLayout groupLayout = new GridLayout(6, true);
        group.setLayout(groupLayout);

        Label tracePathLabel = new Label(group, SWT.LEFT);
        tracePathLabel.setText(Messages.ConfigureTraceDialog_Trace_Path);
        tracePathLabel.setLayoutData(new GridData(GridData.FILL, 
                GridData.CENTER, true, false, 1, 1));
        
        fPath = new Text(group, SWT.LEFT);
        fPath.setLayoutData(new GridData(GridData.FILL, 
                GridData.CENTER, true, false, 4, 1));
        
        fPath.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Button ok = getButton(IDialogConstants.OK_ID);
                ok.setEnabled(validatePathName(fPath.getText()));
            }
        });
        
        Button browseButton = new Button(group, SWT.PUSH);
        browseButton.setText(Messages.ConfigureTraceDialog_Browse + "...");  //$NON-NLS-1$
        browseButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        
        browseButton.setEnabled(true);
        browseButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                String newPath = dialog.open();
                if (newPath != null) {
                    fPath.setText(newPath);
                }
            }
        });
        return composite;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button ok = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        
        ok.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        fPathName = fPath.getText();

        File newDir = new File(fPathName);
        if (!newDir.exists()) {
            try {
                newDir.mkdirs();
            } catch (Exception e) {
                // Should not happen
            }
        }

        super.okPressed();
    }

    /*
     * Validates the trace path.
     */
    private boolean validatePathName(String path) {
        if (path.length() > 0) {
            final char c0 = path.charAt(0);
            if (c0 != '/') {
                fTracePathError = Messages.ConfigureTraceDialog_Error_Invalid_Path;
                fErrorLabel.setText(fTracePathError);
                return false;
            } else {
                String[] folders = path.split("/"); //$NON-NLS-1$
                for (int i = 0; i < folders.length; i++) {
                    final char[] chars = folders[i].toCharArray();
                    for (int x = 0; x < chars.length; x++) {
                        final char c = chars[x];
                        if ((c >= 'a') && (c <= 'z')) {
                            continue; // lowercase
                        }
                        if ((c >= 'A') && (c <= 'Z')) {
                            continue; // uppercase
                        }
                        if ((c >= '0') && (c <= '9')) {
                            continue; // numeric
                        }
                        fTracePathError = Messages.ConfigureTraceDialog_Error_Invalid_Folder;
                        fErrorLabel.setText(fTracePathError);
                        return false;
                    }
                }
                if (path.length() > 1) {
                    for (int i = 0; i < path.length() - 1; i++) {
                        if ((path.charAt(i) == '/') && (path.charAt(i + 1) == '/')) {
                            fTracePathError = Messages.ConfigureTraceDialog_Error_Multiple_Seps;
                            fErrorLabel.setText(fTracePathError);
                            return false;
                        }
                    }
                }
            }
            
            File file = new File(path);
            if (file.isFile()) {
                fTracePathError = Messages.ConfigureTraceDialog_Error_File_Exists;
                fErrorLabel.setText(fTracePathError);
                return false;
            }
            if (path.length() > 1 && !file.getParentFile().canWrite()) {
                fTracePathError = Messages.ConfigureTraceDialog_Error_Can_Not_Write;
                fErrorLabel.setText(fTracePathError);
                return false;
            }
        }
        fErrorLabel.setText(""); //$NON-NLS-1$
        fTracePathError = ""; //$NON-NLS-1$
        return true;
    }

    /**
     * Returns the trace path.
     * 
     * @return trace path
     */
    public String getTracePath() {
        return fPathName;
    }
}
