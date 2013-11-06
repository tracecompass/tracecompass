/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * Dialog box for connection information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class NewConnectionDialog extends Dialog implements INewConnectionDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/target_add.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The host combo box.
     */
    private CCombo fExistingHostsCombo = null;
    /**
     * The check box button for enabling/disabling the text input.
     */
    private Button fButton = null;
    /**
     * The text widget for the node name (alias)
     */
    private Text fConnectionNameText = null;
    /**
     * The text widget for the node address (IP or DNS name)
     */
    private Text fHostNameText = null;
    /**
     * The text widget for the IP port
     */
    private Text fPortText = null;
    /**
     * The parent where the new node should be added.
     */
    private ITraceControlComponent fParent;
    /**
     * The node name (alias) string.
     */
    private String fConnectionName = null;
    /**
     * The node address (IP or DNS name) string.
     */
    private String fHostName = null;
    /**
     * The IP port of the connection.
     */
    private int fPort = IRemoteSystemProxy.INVALID_PORT_NUMBER;
    /**
     * Input list of existing RSE hosts available for selection.
     */
    private IHost[] fExistingHosts = new IHost[0];

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            The shell
     */
    public NewConnectionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getConnectionName() {
        return fConnectionName;
    }

    @Override
    public String getHostName() {
        return fHostName;
    }

    @Override
    public int getPort() {
        return fPort;
    }

    @Override
    public void setTraceControlParent(ITraceControlComponent parent) {
        fParent = parent;
    }

    @Override
    public void setHosts(IHost[] hosts) {
        if (hosts != null) {
            fExistingHosts = Arrays.copyOf(hosts, hosts.length);
        }
    }

    @Override
    public void setPort(int port) {
        fPort = port;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_NewDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Existing connections group
        Group comboGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        comboGroup.setText(Messages.TraceControl_NewNodeExistingConnectionGroupName);
        layout = new GridLayout(2, true);
        comboGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        comboGroup.setLayoutData(data);

        fExistingHostsCombo = new CCombo(comboGroup, SWT.READ_ONLY);
        fExistingHostsCombo.setToolTipText(Messages.TraceControl_NewNodeComboToolTip);
        fExistingHostsCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        String items[] = new String[fExistingHosts.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(fExistingHosts[i].getAliasName() + " - " + fExistingHosts[i].getHostName()); //$NON-NLS-1$
        }

        fExistingHostsCombo.setItems(items);
        fExistingHostsCombo.setEnabled(fExistingHosts.length > 0);

        // Node information grop
        Group textGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        layout = new GridLayout(3, true);
        textGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textGroup.setLayoutData(data);

        fButton = new Button(textGroup, SWT.CHECK);
        fButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        fButton.setText(Messages.TraceControl_NewNodeEditButtonName);
        fButton.setEnabled(fExistingHosts.length > 0);

        Label connectionNameLabel = new Label(textGroup, SWT.RIGHT);
        connectionNameLabel.setText(Messages.TraceControl_NewNodeConnectionNameLabel);
        fConnectionNameText = new Text(textGroup, SWT.NONE);
        fConnectionNameText.setToolTipText(Messages.TraceControl_NewNodeConnectionNameTooltip);
        fConnectionNameText.setEnabled(fExistingHosts.length == 0);

        Label hostNameLabel = new Label(textGroup, SWT.RIGHT);
        hostNameLabel.setText(Messages.TraceControl_NewNodeHostNameLabel);
        fHostNameText = new Text(textGroup, SWT.NONE);
        fHostNameText.setToolTipText(Messages.TraceControl_NewNodeHostNameTooltip);
        fHostNameText.setEnabled(fExistingHosts.length == 0);

        Label portLabel = new Label(textGroup, SWT.RIGHT);
        portLabel.setText(Messages.TraceControl_NewNodePortLabel);
        fPortText = new Text(textGroup, SWT.NONE);
        fPortText.setToolTipText(Messages.TraceControl_NewNodePortTooltip);
        fPortText.setEnabled(fExistingHosts.length == 0);
        fPortText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // only numbers are allowed.
                e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
            }
        });

        fButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fButton.getSelection()) {
                    fExistingHostsCombo.deselectAll();
                    fExistingHostsCombo.setEnabled(false);
                    fConnectionNameText.setEnabled(true);
                    fHostNameText.setEnabled(true);
                    fPortText.setEnabled(true);
                } else {
                    fExistingHostsCombo.setEnabled(true);
                    fConnectionNameText.setEnabled(false);
                    fHostNameText.setEnabled(false);
                    fPortText.setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fExistingHostsCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = fExistingHostsCombo.getSelectionIndex();
                fConnectionNameText.setText(fExistingHosts[index].getAliasName());
                fHostNameText.setText(fExistingHosts[index].getHostName());
                fPortText.setText(""); //$NON-NLS-1$
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // layout widgets
        data = new GridData(GridData.FILL_HORIZONTAL);
        fHostNameText.setText("666.666.666.666"); //$NON-NLS-1$
        Point minSize = fHostNameText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        int widthHint = minSize.x + 5;
        data.widthHint = widthHint;
        data.horizontalSpan = 2;
        fConnectionNameText.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = widthHint;
        data.horizontalSpan = 2;
        fHostNameText.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = widthHint;
        data.horizontalSpan = 2;
        fPortText.setLayoutData(data);

        fHostNameText.setText(""); //$NON-NLS-1$

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        // Validate input data
        fConnectionName = fConnectionNameText.getText();
        fHostName = fHostNameText.getText();
        fPort = (fPortText.getText().length() > 0) ? Integer.parseInt(fPortText.getText()) : IRemoteSystemProxy.INVALID_PORT_NUMBER;

        if (!"".equals(fHostName)) { //$NON-NLS-1$
            // If no node name is specified use the node address as name
            if ("".equals(fConnectionName)) { //$NON-NLS-1$
                fConnectionName = fHostName;
            }
            // Check if node with name already exists in parent
            if(fParent.containsChild(fConnectionName)) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_NewDialogTitle,
                        Messages.TraceControl_AlreadyExistsError + " (" + fConnectionName + ")");  //$NON-NLS-1$//$NON-NLS-2$
                return;
            }
        }
        else {
            return;
        }
        // validation successful -> call super.okPressed()
        super.okPressed();
    }
}
