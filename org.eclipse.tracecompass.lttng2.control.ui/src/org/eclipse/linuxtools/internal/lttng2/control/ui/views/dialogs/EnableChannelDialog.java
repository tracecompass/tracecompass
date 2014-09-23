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
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
 * Dialog box for collecting channel information when enabling a channel (which will be created).
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableChannelDialog extends Dialog implements IEnableChannelDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String ENABLE_CHANNEL_ICON_FILE = "icons/elcl16/add_button.gif"; //$NON-NLS-1$

    /**
     *  To indicate that the default value will be used for this field
     */
    private static final String DEFAULT_TEXT = "<" + Messages.EnableChannelDialog_DefaultMessage + ">"; //$NON-NLS-1$ //$NON-NLS-2$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The text widget for the channel name
     */
    private Text fChannelNameText = null;
    /**
     * The discard mode of the channel.
     */
    private Button fDiscardModeButton = null;
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
     * The target node component
     */
    private TargetNodeComponent fTargetNodeComponent = null;
    /**
     * Common verify listener for numeric text input.
     */
    private VerifyListener fVerifyListener = null;
    /**
     * Common focus listener
     */
    private FocusListener fFocusListener = null;
    /**
     * Output channel information.
     */
    private IChannelInfo fChannelInfo = null;
    /**
     * Output domain information. True in case of Kernel domain. False for UST.
     */
    private boolean fIsKernel;
    /**
     *  Flag which indicates whether Kernel domain is available or not
     */
    private boolean fHasKernel;
    /**
     * Maximum size of trace files of the channel.
     */
    private Text fMaxSizeTraceText = null;
    /**
     * Maximum number of trace files of the channel.
     */
    private Text fMaxNumberTraceText = null;
    /**
     * CheckBox for selecting shared buffers (kernel onlyu).
     */
    private Button fSharedBuffersButton = null;
    /**
     * CheckBox for selecting per UID buffers.
     */
    private Button fPIDBuffersButton = null;
    /**
     * CheckBox for selecting per UID buffers.
     */
    private Button fUIDBuffersButton = null;
    /**
     * CheckBox to configure metadata channel
     */
    private Button fMetadataChannelButton = null;
    /**
     * Previous channel name
     */
    private String fPreviousChannelName = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public EnableChannelDialog(Shell shell) {
       super(shell);
       fIsKernel = true;

        // Common verify listener
        fVerifyListener = new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // only numbers and default are allowed.
                e.doit = e.text.matches("[0-9]*") || e.text.matches(DEFAULT_TEXT); //$NON-NLS-1$
            }
        };

        // Common focus listener
        fFocusListener = new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                Text focusLostWidget = (Text) e.widget;
                if (focusLostWidget.getText().isEmpty()) {
                    focusLostWidget.setText(DEFAULT_TEXT);
                    focusLostWidget.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                Text focusGainedWidget = (Text) e.widget;
                if (focusGainedWidget.getText().equals(DEFAULT_TEXT)) {
                    focusGainedWidget.setText(""); //$NON-NLS-1$
                    focusGainedWidget.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
                }
            }
        };

        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public IChannelInfo getChannelInfo() {
        return fChannelInfo;
    }

    @Override
    public void setDomainComponent(TraceDomainComponent domain) {
        fDomain = domain;
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = true;
        }
    }

    @Override
    public boolean isKernel() {
        return fIsKernel;
    }

    @Override
    public void setHasKernel(boolean hasKernel) {
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = hasKernel;
        }

        fHasKernel = hasKernel;
    }

    @Override
    public void setTargetNodeComponent(TargetNodeComponent node) {
        fTargetNodeComponent = node;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_EnableChannelDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ENABLE_CHANNEL_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        int numColumn = 2;
        if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
            numColumn = 3;
        }

        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);

        Composite commonModeGroup = new Composite(dialogComposite, SWT.NONE);
        layout = new GridLayout(3, true);
        commonModeGroup.setLayout(layout);

        Label channelNameLabel = new Label(commonModeGroup, SWT.RIGHT);
        channelNameLabel.setText(Messages.TraceControl_EnableChannelNameLabel);
        fChannelNameText = new Text(commonModeGroup, SWT.NONE);
        fChannelNameText.setToolTipText(Messages.TraceControl_EnableChannelNameTooltip);

        Label subBufferSizeLabel = new Label(commonModeGroup, SWT.RIGHT);
        subBufferSizeLabel.setText(Messages.TraceControl_SubBufferSizePropertyName);
        fSubBufferSizeText = new Text(commonModeGroup, SWT.NONE);
        fSubBufferSizeText.setToolTipText(Messages.TraceControl_EnableChannelSubBufferSizeTooltip);
        fSubBufferSizeText.addVerifyListener(fVerifyListener);
        fSubBufferSizeText.addFocusListener(fFocusListener);
        fSubBufferSizeText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));

        Label numSubBufferLabel = new Label(commonModeGroup, SWT.RIGHT);
        numSubBufferLabel.setText(Messages.TraceControl_NbSubBuffersPropertyName);
        fNumberOfSubBuffersText = new Text(commonModeGroup, SWT.NONE);
        fNumberOfSubBuffersText.setToolTipText(Messages.TraceControl_EnableChannelNbSubBuffersTooltip);
        fNumberOfSubBuffersText.addVerifyListener(fVerifyListener);
        fNumberOfSubBuffersText.addFocusListener(fFocusListener);

        Label switchTimerLabel = new Label(commonModeGroup, SWT.RIGHT);
        switchTimerLabel.setText(Messages.TraceControl_SwitchTimerPropertyName);
        fSwitchTimerText = new Text(commonModeGroup, SWT.NONE);
        fSwitchTimerText.setToolTipText(Messages.TraceControl_EnableChannelSwitchTimerTooltip);
        fSwitchTimerText.addVerifyListener(fVerifyListener);
        fSwitchTimerText.addFocusListener(fFocusListener);

        Label readTimerLabel = new Label(commonModeGroup, SWT.RIGHT);
        readTimerLabel.setText(Messages.TraceControl_ReadTimerPropertyName);
        fReadTimerText = new Text(commonModeGroup, SWT.NONE);
        fReadTimerText.setToolTipText(Messages.TraceControl_EnableChannelReadTimerTooltip);
        fReadTimerText.addVerifyListener(fVerifyListener);
        fReadTimerText.addFocusListener(fFocusListener);

        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            Label maxSizeTraceFilesLabel = new Label(commonModeGroup, SWT.RIGHT);
            maxSizeTraceFilesLabel.setText(Messages.TraceControl_MaxSizeTraceFilesPropertyName);
            fMaxSizeTraceText = new Text(commonModeGroup, SWT.NONE);
            fMaxSizeTraceText.setToolTipText(Messages.TraceControl_EnbleChannelMaxSizeTraceFilesTooltip);
            fMaxSizeTraceText.addVerifyListener(fVerifyListener);
            fMaxSizeTraceText.addFocusListener(fFocusListener);

            Label maxNumTraceFilesLabel = new Label(commonModeGroup, SWT.RIGHT);
            maxNumTraceFilesLabel.setText(Messages.TraceControl_MaxNumTraceFilesPropertyName);
            fMaxNumberTraceText = new Text(commonModeGroup, SWT.NONE);
            fMaxNumberTraceText.setToolTipText(Messages.TraceControl_EnbleChannelMaxNumTraceFilesTooltip);
            fMaxNumberTraceText.addVerifyListener(fVerifyListener);
            fMaxNumberTraceText.addFocusListener(fFocusListener);
        }

        if (fTargetNodeComponent.isPeriodicalMetadataFlushSupported()) {
            fMetadataChannelButton = new Button(commonModeGroup, SWT.CHECK);
            fMetadataChannelButton.setText(Messages.TraceControl_ConfigureMetadataChannelName);
            fMetadataChannelButton.setSelection(false);

            fMetadataChannelButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fMetadataChannelButton.getSelection()) {
                        fPreviousChannelName = fChannelNameText.getText();
                        fChannelNameText.setText("metadata"); //$NON-NLS-1$
                        fChannelNameText.setEnabled(false);
                    } else {
                        fChannelNameText.setText(fPreviousChannelName);
                        fChannelNameText.setEnabled(true);
                    }
                }
            });
        }
        Group discardModeGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        discardModeGroup.setText(Messages.TraceControl_EnableChannelDiscardModeGroupName);
        layout = new GridLayout(numColumn, true);
        discardModeGroup.setLayout(layout);

        fDiscardModeButton = new  Button(discardModeGroup, SWT.RADIO);
        fDiscardModeButton.setText(Messages.TraceControl_EnableChannelDiscardModeLabel);
        fDiscardModeButton.setToolTipText(Messages.TraceControl_EnableChannelDiscardModeTooltip);
        fDiscardModeButton.setSelection(true);

        fOverwriteModeButton = new Button(discardModeGroup, SWT.RADIO);
        fOverwriteModeButton.setText(Messages.TraceControl_EnableChannelOverwriteModeLabel);
        fOverwriteModeButton.setToolTipText(Messages.TraceControl_EnableChannelOverwriteModeTooltip);
        fOverwriteModeButton.setSelection(false);

        Group domainGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        domainGroup.setText(Messages.TraceControl_DomainDisplayName);
        layout = new GridLayout(numColumn, true);
        domainGroup.setLayout(layout);

        fKernelButton = new Button(domainGroup, SWT.RADIO);
        fKernelButton.setText(Messages.TraceControl_KernelDomainDisplayName);
        fKernelButton.setSelection(fIsKernel);
        fUstButton = new Button(domainGroup, SWT.RADIO);
        fUstButton.setText(Messages.TraceControl_UstDisplayName);
        fUstButton.setSelection(!fIsKernel);

        if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
            Group bufferTypeGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
            bufferTypeGroup.setText(Messages.TraceControl_BufferTypeDisplayName);
            layout = new GridLayout(numColumn, true);
            bufferTypeGroup.setLayout(layout);

            GridData data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
            data.horizontalSpan = 3;
            bufferTypeGroup.setLayoutData(data);

            fSharedBuffersButton = new Button(bufferTypeGroup, SWT.RADIO);
            fSharedBuffersButton.setText(Messages.TraceControl_SharedBuffersDisplayName);
            fSharedBuffersButton.setSelection(fIsKernel);
            fSharedBuffersButton.setEnabled(false);

            fPIDBuffersButton = new Button(bufferTypeGroup, SWT.RADIO);
            fPIDBuffersButton.setText(Messages.TraceControl_PerPidBuffersDisplayName);
            fPIDBuffersButton.setToolTipText(Messages.TraceControl_PerPidBuffersTooltip);
            fPIDBuffersButton.setSelection(false);

            fUIDBuffersButton = new Button(bufferTypeGroup, SWT.RADIO);
            fUIDBuffersButton.setText(Messages.TraceControl_PerUidBuffersDisplayName);
            fUIDBuffersButton.setToolTipText(Messages.TraceControl_PerPidBuffersTooltip);
            fUIDBuffersButton.setSelection(false);

            fUIDBuffersButton.setEnabled(!fIsKernel);
            fPIDBuffersButton.setEnabled(!fIsKernel);

            // Update buffers type buttons depending on UST or Kernel
            fUstButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fUstButton.getSelection()) {
                        fSharedBuffersButton.setSelection(false);
                        fPIDBuffersButton.setSelection(false);
                        fUIDBuffersButton.setSelection(false);
                        fPIDBuffersButton.setEnabled(true);
                        fUIDBuffersButton.setEnabled(true);
                    } else {
                        fSharedBuffersButton.setSelection(true);
                        fPIDBuffersButton.setSelection(false);
                        fUIDBuffersButton.setSelection(false);
                        fPIDBuffersButton.setEnabled(false);
                        fUIDBuffersButton.setEnabled(false);
                    }
                }
            });
        }

        if ((fDomain != null) || (!fHasKernel)) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(false);

            if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
                fSharedBuffersButton.setEnabled(false);
                fUIDBuffersButton.setEnabled(!fHasKernel);
                fPIDBuffersButton.setEnabled(!fHasKernel);
                setBufferTypeButtonSelection();
            }
        }

        // layout widgets
        GridData data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        data.horizontalSpan = 3;
        discardModeGroup.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fDiscardModeButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fOverwriteModeButton.setLayoutData(data);

        data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        data.horizontalSpan = 3;
        domainGroup.setLayoutData(data);

        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fKernelButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fUstButton.setLayoutData(data);
        if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            fSharedBuffersButton.setLayoutData(data);
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            fPIDBuffersButton.setLayoutData(data);
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            fUIDBuffersButton.setLayoutData(data);
        }

        if (fTargetNodeComponent.isPeriodicalMetadataFlushSupported()) {
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            data.horizontalSpan = numColumn;
            fMetadataChannelButton.setLayoutData(data);
        }

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;

        fChannelNameText.setLayoutData(data);
        fSubBufferSizeText.setLayoutData(data);
        fNumberOfSubBuffersText.setLayoutData(data);
        fSwitchTimerText.setLayoutData(data);
        fReadTimerText.setLayoutData(data);
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            fMaxNumberTraceText.setLayoutData(data);
            fMaxSizeTraceText.setLayoutData(data);
        }

        setDefaults();

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.DETAILS_ID, "&Default", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        // Set channel information
        ChannelInfo channelInfo = new ChannelInfo(fChannelNameText.getText());
        channelInfo.setSubBufferSize(fSubBufferSizeText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Long.parseLong(fSubBufferSizeText.getText()));
        channelInfo.setNumberOfSubBuffers(fNumberOfSubBuffersText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Integer.parseInt(fNumberOfSubBuffersText.getText()));
        channelInfo.setSwitchTimer(fSwitchTimerText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Long.parseLong(fSwitchTimerText.getText()));
        channelInfo.setReadTimer(fReadTimerText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Long.parseLong(fReadTimerText.getText()));
        channelInfo.setOverwriteMode(fOverwriteModeButton.getSelection());
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            channelInfo.setMaxSizeTraceFiles(fMaxSizeTraceText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Integer.parseInt(fMaxSizeTraceText.getText()));
            channelInfo.setMaxNumberTraceFiles(fMaxNumberTraceText.getText().equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Integer.parseInt(fMaxNumberTraceText.getText()));
        }
        if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
            if (fSharedBuffersButton.getSelection()) {
                channelInfo.setBufferType(BufferType.BUFFER_SHARED);
            } else if (fPIDBuffersButton.getSelection()) {
                channelInfo.setBufferType(BufferType.BUFFER_PER_PID);
            } else if (fUIDBuffersButton.getSelection()) {
                channelInfo.setBufferType(BufferType.BUFFER_PER_UID);
            } else {
                channelInfo.setBufferType(BufferType.BUFFER_TYPE_UNKNOWN);
            }
        }

        fIsKernel = fKernelButton.getSelection();

        // Check for invalid names
        if (!channelInfo.getName().matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableChannelDialogTitle,
                  Messages.TraceControl_InvalidChannelNameError + " (" + channelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check for duplicate names
        if (fDomain != null && fDomain.containsChild(channelInfo.getName())) {
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_EnableChannelDialogTitle,
                    Messages.TraceControl_ChannelAlreadyExistsError + " (" + channelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        fChannelInfo = channelInfo;

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

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
        fSwitchTimerText.setText(DEFAULT_TEXT);
        fSwitchTimerText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        fReadTimerText.setText(DEFAULT_TEXT);
        fReadTimerText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        fOverwriteModeButton.setSelection(IChannelInfo.DEFAULT_OVERWRITE_MODE);
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            fMaxSizeTraceText.setText(DEFAULT_TEXT);
            fMaxSizeTraceText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
            fMaxNumberTraceText.setText(DEFAULT_TEXT);
            fMaxNumberTraceText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        }
        fSubBufferSizeText.setText(DEFAULT_TEXT);
        fSubBufferSizeText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        fNumberOfSubBuffersText.setText(DEFAULT_TEXT);
        fNumberOfSubBuffersText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
        if (fTargetNodeComponent.isBufferTypeConfigSupported()) {
            setBufferTypeButtonSelection();
        }
    }

    private void setBufferTypeButtonSelection() {
        if ((fDomain != null) && fDomain.getBufferType() != null) {
            switch (fDomain.getBufferType()) {
            case BUFFER_PER_PID:
                fPIDBuffersButton.setSelection(true);
                break;
            case BUFFER_PER_UID:
                fUIDBuffersButton.setSelection(true);
                break;
            case BUFFER_SHARED:
                fSharedBuffersButton.setSelection(true);
                break;
                //$CASES-OMITTED$
            default:
                break;
            }
        }
    }

}
