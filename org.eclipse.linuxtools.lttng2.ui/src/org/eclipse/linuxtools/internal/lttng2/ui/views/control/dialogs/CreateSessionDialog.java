/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>CreateSessionDialog</u></b>
 * <p>
 * Dialog box for collecting session creation information.
 * </p>
 */
public class CreateSessionDialog extends Dialog implements ICreateSessionDialog {

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
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The text widget for the session name
     */
    private Text fSessionNameText = null;
    /**
     * The text widget for the session path
     */
    private Text fSessionPathText = null;
    /**
     * The parent where the new node should be added.
     */
    private TraceSessionGroup fParent = null;
    /**
     * The session name string.
     */
    private String fSessionName = null;
    /**
     * The  session path string.
     */
    private String fSessionPath = null;
    /**
     * Flag whether default location (path) shall be used or not
     */
    private boolean fIsDefaultPath = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public CreateSessionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog#getSessionName()
     */
    @Override
    public String getSessionName() {
        return fSessionName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog#getSessionPath()
     */
    @Override
    public String getSessionPath() {
        return fSessionPath;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog#isDefaultSessionPath()
     */
    @Override
    public boolean isDefaultSessionPath() {
        return fIsDefaultPath;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog#setTraceSessionGroup(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup)
     */
    @Override
    public void setTraceSessionGroup(TraceSessionGroup group) {
        fParent = group;
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
        newShell.setText(Messages.TraceControl_CreateSessionDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(CREATE_SESSION_ICON_FILE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label sessionNameLabel = new Label(fDialogComposite, SWT.RIGHT);
        sessionNameLabel.setText(Messages.TraceControl_CreateSessionNameLabel);
        fSessionNameText = new Text(fDialogComposite, SWT.NONE);
        fSessionNameText.setToolTipText(Messages.TraceControl_CreateSessionNameTooltip);
        
        Label sessionPath = new Label(fDialogComposite, SWT.RIGHT);
        sessionPath.setText(Messages.TraceControl_CreateSessionPathLabel);
        fSessionPathText = new Text(fDialogComposite, SWT.NONE);
        fSessionPathText.setToolTipText(Messages.TraceControl_CreateSessionPathTooltip);

        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        
        fSessionNameText.setLayoutData(data);
        fSessionPathText.setLayoutData(data);

        getShell().setMinimumSize(new Point(300, 150));
        
        return fDialogComposite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        // Validate input data
        fSessionName = fSessionNameText.getText();
        fSessionPath = fSessionPathText.getText();

        if (!"".equals(fSessionPath)) { //$NON-NLS-1$
            // validate sessionPath

            TargetNodeComponent node = (TargetNodeComponent)fParent.getParent();
            IRemoteSystemProxy proxy = node.getRemoteSystemProxy();
            IFileServiceSubSystem fsss = proxy.getFileServiceSubSystem();
            if (fsss != null) {
                try {
                    IRemoteFile remoteFolder = fsss.getRemoteFileObject(fSessionPath, new NullProgressMonitor());
                    if (remoteFolder.exists()) {
                        MessageDialog.openError(getShell(),
                                Messages.TraceControl_CreateSessionDialogTitle,
                                Messages.TraceControl_SessionPathAlreadyExistsError + " (" + fSessionPath + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
                        return;
                    }
                } catch (SystemMessageException e) {
                    MessageDialog.openError(getShell(),
                            Messages.TraceControl_CreateSessionDialogTitle,
                            Messages.TraceControl_FileSubSystemError + "\n" + e);  //$NON-NLS-1$
                    return;
                }    
            }
            fIsDefaultPath = false;
        }

        // If no session name is specified use default name auto
        if ("".equals(fSessionName)) { //$NON-NLS-1$
            fSessionName = "auto"; //$NON-NLS-1$
        }

        // Check for invalid names
        if (!fSessionName.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_CreateSessionDialogTitle,
                    Messages.TraceControl_InvalidSessionNameError + " (" + fSessionName + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check if node with name already exists in parent
        if(fParent.containsChild(fSessionName)) {
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_CreateSessionDialogTitle,
                    Messages.TraceControl_SessionAlreadyExistsError + " (" + fSessionName + ")");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        
        // validation successful -> call super.okPressed()
        super.okPressed();
    }
}
