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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.swt.SWT;
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
 * <b><u>CreateChannelDialog</u></b>
 * <p>
 * Dialog box for collecting channel creation information.
 * </p>
 */
public class CreateChannelDialog extends Dialog implements ICreateChannelDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String ENABLE_CHANNEL_ICON_FILE = "icons/elcl16/edit.gif"; //$NON-NLS-1$ 

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The text widget for the channel name
     */
    private Text fChannelNameText = null;
    /**
     * The overwrite mode of the channel.
     */
    private Button fOverwriteModeButton = null;
    /**
     * The sub-buffer size of the channel.
     */
    private Text fSubBufferSizeText = null;
    /**
     * The number of sub-buffers of the channel.
     */
    private Text fNumberOfSubBuffersText = null;
    /**
     * The switch timer interval of the channel.
     */
    private Text fSwitchTimerText = null;
    /**
     * The read timer interval of the channel.
     */
    private Text fReadTimerText = null;
    /**
     * Group composite for domain selection.
     */
    private Group fDomainGroup = null;
    /**
     * Radio button for selecting kernel domain.
     */
    private Button fKernelButton = null;
    /**
     * Radio button for selecting UST domain.
     */
    private Button fUstButton = null;
    /**
     * The parent domain component where the channel node should be added. 
     * Null in case of creation on session level.
     */
    private TraceDomainComponent fDomain = null;
    /**
     * Common verify listener for numeric text input.  
     */
    private VerifyListener fVerifyListener = null;
    /**
     * Output channel information.
     */
    private IChannelInfo fChannelInfo = null;
    /**
     * Output domain information. True in case of Kernel domain. False for UST.     
     */
    private boolean fIsKernel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public CreateChannelDialog(Shell shell) {
       super(shell);
       fIsKernel = true;

        // Common verify listener
        fVerifyListener = new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // only numbers are allowed.
                e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
            }
        };
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateChannelDialog#getChannelInfo()
     */
    @Override
    public IChannelInfo getChannelInfo() {
        return fChannelInfo;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateChannelDialog#setDomainComponent(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent)
     */
    @Override
    public void setDomainComponent(TraceDomainComponent domain) {
        fDomain = domain;
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = true;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateChannelDialog#isKernel()
     */
    @Override
    public boolean isKernel() {
        return fIsKernel;
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
        newShell.setText(Messages.TraceControl_EnableChannelDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ENABLE_CHANNEL_ICON_FILE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        fDialogComposite.setLayout(layout); 

        Label channelNameLabel = new Label(fDialogComposite, SWT.RIGHT);
        channelNameLabel.setText(Messages.TraceControl_EnableChannelNameLabel);
        fChannelNameText = new Text(fDialogComposite, SWT.NONE);
        fChannelNameText.setToolTipText(Messages.TraceControl_EnableChannelNameTooltip);
        
        Label subBufferSizeLabel = new Label(fDialogComposite, SWT.RIGHT);
        subBufferSizeLabel.setText(Messages.TraceControl_SubBufferSizePropertyName);
        fSubBufferSizeText = new Text(fDialogComposite, SWT.NONE);
        fSubBufferSizeText.setToolTipText(Messages.TraceControl_EnableChannelSubBufferSizeTooltip);
        fSubBufferSizeText.addVerifyListener(fVerifyListener);
        
        Label numSubBufferLabel = new Label(fDialogComposite, SWT.RIGHT);
        numSubBufferLabel.setText(Messages.TraceControl_NbSubBuffersPropertyName);
        fNumberOfSubBuffersText = new Text(fDialogComposite, SWT.NONE);
        fNumberOfSubBuffersText.setToolTipText(Messages.TraceControl_EnableChannelNbSubBuffersTooltip);
        fNumberOfSubBuffersText.addVerifyListener(fVerifyListener);

        Label switchTimerLabel = new Label(fDialogComposite, SWT.RIGHT);
        switchTimerLabel.setText(Messages.TraceControl_SwitchTimerPropertyName);
        fSwitchTimerText = new Text(fDialogComposite, SWT.NONE);
        fSwitchTimerText.setToolTipText(Messages.TraceControl_EnableChannelSwitchTimerTooltip);
        fSwitchTimerText.addVerifyListener(fVerifyListener);

        Label readTimerLabel = new Label(fDialogComposite, SWT.RIGHT);
        readTimerLabel.setText(Messages.TraceControl_ReadTimerPropertyName);
        fReadTimerText = new Text(fDialogComposite, SWT.NONE);
        fReadTimerText.setToolTipText(Messages.TraceControl_EnableChannelReadTimerTooltip);
        fReadTimerText.addVerifyListener(fVerifyListener);

        fOverwriteModeButton = new Button(fDialogComposite, SWT.CHECK);
        fOverwriteModeButton.setText(Messages.TraceControl_OverwriteModePropertyName);
        fOverwriteModeButton.setToolTipText(Messages.TraceControl_EnableChannelOverwriteModeTooltip);
        new Label(fDialogComposite, SWT.RIGHT);

        fDomainGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fDomainGroup.setText(Messages.TraceControl_DomainDisplayName);
        layout = new GridLayout(2, true);
        fDomainGroup.setLayout(layout); 
        
        fKernelButton = new Button(fDomainGroup, SWT.RADIO);
        fKernelButton.setText(Messages.TraceControl_KernelDomainDisplayName);
        fKernelButton.setSelection(fIsKernel);
        fUstButton = new Button(fDomainGroup, SWT.RADIO);
        fUstButton.setText(Messages.TraceControl_UstDisplayName);
        fUstButton.setSelection(!fIsKernel);

        if (fDomain != null) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(false);
        }

        // layout widgets
        GridData data = new GridData(GridData.FILL, GridData.CENTER, false, false, 2, 1);
        fDomainGroup.setLayoutData(data);

        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fKernelButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fUstButton.setLayoutData(data);
        
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        fSubBufferSizeText.setText("666.666.666.666"); //$NON-NLS-1$
        Point minSize = fSubBufferSizeText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = minSize.x + 5;

        fChannelNameText.setLayoutData(data);
        fSubBufferSizeText.setLayoutData(data);
        fNumberOfSubBuffersText.setLayoutData(data);
        fSwitchTimerText.setLayoutData(data);
        fReadTimerText.setLayoutData(data);

        fSubBufferSizeText.setText(""); //$NON-NLS-1$

        setDefaults();

        return fDialogComposite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.DETAILS_ID, "Default", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        // Set channel information
        fChannelInfo = new ChannelInfo(fChannelNameText.getText());
        fChannelInfo.setSubBufferSize(Long.parseLong(fSubBufferSizeText.getText()));
        fChannelInfo.setNumberOfSubBuffers(Integer.parseInt(fNumberOfSubBuffersText.getText()));
        fChannelInfo.setSwitchTimer(Long.parseLong(fSwitchTimerText.getText()));
        fChannelInfo.setReadTimer(Long.parseLong(fReadTimerText.getText()));
        fChannelInfo.setOverwriteMode(fOverwriteModeButton.getSelection());

        fIsKernel = fKernelButton.getSelection();

        // Check for invalid names
        if (!fChannelInfo.getName().matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableChannelDialogTitle,
                  Messages.TraceControl_InvalidChannelNameError + " (" + fChannelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check for duplicate names
        if (fDomain != null && fDomain.containsChild(fChannelInfo.getName())) {
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_EnableChannelDialogTitle,
                    Messages.TraceControl_ChannelAlreadyExistsError + " (" + fChannelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.DETAILS_ID) {
            setDefaults();
            return;
        }
        super.buttonPressed(buttonId);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Sets default value depending on Kernel or UST
     */
    private void setDefaults() {
        fSwitchTimerText.setText(String.valueOf(IChannelInfo.DEFAULT_SWITCH_TIMER));
        fReadTimerText.setText(String.valueOf(IChannelInfo.DEFAULT_READ_TIMER));
        fOverwriteModeButton.setSelection(IChannelInfo.DEFAULT_OVERWRITE_MODE);
        if (fKernelButton.getSelection()) {
            fSubBufferSizeText.setText(String.valueOf(IChannelInfo.DEFAULT_SUB_BUFFER_SIZE_KERNEL));
            fNumberOfSubBuffersText.setText(String.valueOf(IChannelInfo.DEFAULT_NUMBER_OF_SUB_BUFFERS_KERNEL));
        } else {
            fSubBufferSizeText.setText(String.valueOf(IChannelInfo.DEFAULT_SUB_BUFFER_SIZE_UST));
            fNumberOfSubBuffersText.setText(String.valueOf(IChannelInfo.DEFAULT_NUMBER_OF_SUB_BUFFERS_UST));
        }
    }
}
