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
package org.eclipse.linuxtools.lttng.ui.views.control.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
 * <b><u>NewConnectionDialog</u></b>
 * <p>
 * Dialog box for connection information.
 * </p>
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
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The Group for the host combo box.
     */
    private Group fComboGroup = null;
    /**
     * The Group for the text input.
     */
    private Group fTextGroup = null;
    /**
     * The host combo box.
     */
    private CCombo fExistingHostsCombo = null;
    /**
     * The check box button for enablling/disabling the text input.
     */
    private Button fButton = null;
    /**
     * The text widget for the node name (alias)
     */
    private Text fNodeNameText = null;
    /**
     * The text widget for the node address (IP or DNS name)
     */
    private Text fNodeAddressText = null;
    /**
     * The parent where the new node should be added.
     */
    private ITraceControlComponent fParent;
    /**
     * The node name (alias) string.
     */
    private String fNodeName = null;
    /**
     * The node address (IP or DNS name) string.
     */
    private String fNodeAddress = null;
    
    /**
     * Input list of existing RSE hosts available for selection.
     */
    private IHost[] fExistingHosts = new IHost[0];

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public NewConnectionDialog(Shell shell, ITraceControlComponent parent, IHost[] hosts) {
        super(shell);
        fParent = parent;
        if (hosts != null) {
            fExistingHosts = Arrays.copyOf(hosts, hosts.length);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.INewConnectionDialog#getNodeName()
     */
    @Override
    public String getNodeName() {
        return fNodeName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.dialogs.INewConnectionDialog#getNodeAddress()
     */
    @Override
    public String getNodeAddress() {
        return fNodeAddress;
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
        newShell.setText(Messages.TraceControl_NewDialogTitle);
        newShell.setImage(LTTngUiPlugin.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout); 

        // Existing connections group
        fComboGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fComboGroup.setText(Messages.TraceControl_NewNodeExistingConnetionsGroupName);
        layout = new GridLayout(2, true);
        fComboGroup.setLayout(layout); 
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        fComboGroup.setLayoutData(data);
        
        fExistingHostsCombo = new CCombo(fComboGroup, SWT.READ_ONLY);
        fExistingHostsCombo.setToolTipText(Messages.TraceControl_NewNodeComboToolTip);
        fExistingHostsCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 2, 1));

        String items[] = new String[fExistingHosts.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(fExistingHosts[i].getAliasName() + " - " + fExistingHosts[i].getHostName()); //$NON-NLS-1$
        }

        fExistingHostsCombo.setItems(items);
        fExistingHostsCombo.setEnabled(fExistingHosts.length > 0);

        // Node information grop
        fTextGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        layout = new GridLayout(2, true);
        fTextGroup.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        fTextGroup.setLayoutData(data);
        
        fButton = new Button(fTextGroup, SWT.CHECK);
        fButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 2, 1));
        fButton.setText(Messages.TraceControl_NewNodeEditButtonName);
        fButton.setEnabled(fExistingHosts.length > 0);
        
        Label nodeNameLabel = new Label(fTextGroup, SWT.RIGHT);
        nodeNameLabel.setText(Messages.TraceControl_NewNodeNameLabel);
        fNodeNameText = new Text(fTextGroup, SWT.NONE);
        fNodeNameText.setToolTipText(Messages.TraceControl_NewNodeNameTooltip);
        fNodeNameText.setEnabled(fExistingHosts.length == 0);
        
        Label nodeAddressLabel = new Label(fTextGroup, SWT.RIGHT);
        nodeAddressLabel.setText(Messages.TraceControl_NewNodeAddressLabel);
        fNodeAddressText = new Text(fTextGroup, SWT.NONE);
        fNodeAddressText.setToolTipText(Messages.TraceControl_NewNodeAddressTooltip);
        fNodeAddressText.setEnabled(fExistingHosts.length == 0);

        fButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fButton.getSelection()) {
                    fExistingHostsCombo.deselectAll();
                    fExistingHostsCombo.setEnabled(false);
                    fNodeNameText.setEnabled(true);
                    fNodeAddressText.setEnabled(true);
                } else {
                    fExistingHostsCombo.setEnabled(true);
                    fNodeNameText.setEnabled(false);
                    fNodeAddressText.setEnabled(false);
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
                fNodeNameText.setText(fExistingHosts[index].getAliasName());
                fNodeAddressText.setText(fExistingHosts[index].getHostName());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        // layout widgets
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        fNodeAddressText.setText("666.666.666.666"); //$NON-NLS-1$
        Point minSize = fNodeAddressText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = minSize.x + 5;
        
        fNodeNameText.setLayoutData(data);
        fNodeAddressText.setLayoutData(data);
        
        fNodeAddressText.setText(""); //$NON-NLS-1$
        
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
        fNodeName = fNodeNameText.getText();
        fNodeAddress = fNodeAddressText.getText();

        if (!"".equals(fNodeAddress)) { //$NON-NLS-1$
            // If no node name is specified use the node address as name
            if ("".equals(fNodeName)) { //$NON-NLS-1$
                fNodeName = fNodeAddress;
            }
            // Check if node with name already exists in parent
            if(fParent.containsChild(fNodeName)) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_NewDialogTitle,
                        Messages.TraceControl_AlreadyExistsError + " (" + fNodeName + ")");  //$NON-NLS-1$//$NON-NLS-2$
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
