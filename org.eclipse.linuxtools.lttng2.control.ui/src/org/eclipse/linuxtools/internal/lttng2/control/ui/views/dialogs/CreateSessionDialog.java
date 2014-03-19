/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Marc-Andre Laperle - Support for creating a live session
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.IRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceConstants;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
 * Dialog box for collecting session creation information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class CreateSessionDialog extends TitleAreaDialog implements ICreateSessionDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String CREATE_SESSION_ICON_FILE = "icons/elcl16/add_button.gif"; //$NON-NLS-1$

    /**
     *  To indicate that the default value will be used for this field
     */
    private static final String DEFAULT_TEXT = "<" + Messages.EnableChannelDialog_DefaultMessage + ">"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Supported network protocols for streaming
     */
    private enum StreamingProtocol {
        /** Default network protocol for IPv4 (TCP)*/
        net,
        /** Default network protocol for IPv6 (TCP)*/
        net6,
        /** File */
        file,
   }

    /**
     * Supported network protocols for Live tracing
     */
    private enum LiveProtocol {
        /** Default network protocol for IPv4 (TCP)*/
        net,
        /** Default network protocol for IPv6 (TCP)*/
        net6
   }

    private enum StreamingProtocol2 {
        /** Default network protocol for IPv4 (TCP)*/
        net,
        /** Default network protocol for IPv6 (TCP)*/
        net6,
        /** TCP network protocol for IPv4*/
        tcp,
        /** TCP network protocol for IPv6*/
        tcp6 }

    /**
     * Index of last supported streaming protocol for common URL configuration.
     */
    private static final int COMMON_URL_LAST_INDEX = 1;
    /**
     *  Index of default streaming protocol.
     */
    private static final int DEFAULT_URL_INDEX = 0;

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
     * The label widget for the session path.
     */
    private Label fSessionPathLabel = null;
    /**
     * The text widget for the session path.
     */
    private Text fSessionPathText = null;
    /**
     * The button widget to select a normal session
     */
    private Button fNormalModeButton = null;
    /**
     * The button widget to select a snapshot session
     */
    private Button fSnapshotButton = null;
    /**
     * The group that contains the mutually exclusive mode buttons
     */
    private Group fModeButtonGroup = null;
    /**
     * The button widget to select a live session
     */
    private Button fLiveButton = null;

    /**
     * The text widget to set a live delay
     */
    private Text fLiveDelayText = null;
    /**
     * The Group for advanced configuration.
     */
    private Group fAdvancedGroup = null;
    /**
     * The button to show advanced options.
     */
    private Button fAdvancedButton = null;
    /**
     * The composite with streaming configuration parameter.
     */
    private Composite fStreamingComposite = null;
    /**
     * The text widget for the trace path.
     */
    private Text fTracePathText = null;
    /**
     * The button to link data protocol/Address with control protocol.
     */
    private Button fLinkDataWithControlButton = null;
    /**
     * The Combo box for channel protocol selection.
     */
     private CCombo fControlProtocolCombo = null;
    /**
     * A selection listener that copies the protocol from control to data when being linked.
     */
    private ControlProtocolSelectionListener fCopyProtocolSelectionListener;
    /**
     * A selection listener updates the control port text depending on the control protocol selected.
     */
    private ProtocolComboSelectionListener fControlProtocolSelectionListener;
    /**
     * A selection listener updates the data port text depending on the data protocol selected.
     */
    private ProtocolComboSelectionListener fDataProtocolSelectionListener;
    /**
     * The text box for the host/IP address of the control channel.
     */
    private Text fControlHostAddressText = null;
    /**
     * A key listener that copies the host address from control to data when being linked.
     */
    private CopyModifyListener fControlUrlKeyListener;
    /**
     * A modify listener that updates the enablement of the dialog.
     */
    private UpdateEnablementModifyListener fUpdateEnablementModifyListener;
    /**
     * The text box for the control port.
     */
    private Text fControlPortText = null;
    /**
     * The Combo box for data protocol selection.
     */
     private CCombo fDataProtocolCombo = null;
    /**
     * The text box for the host/IP address of the data channel.
     */
    private Text fDataHostAddressText = null;
    /**
     * The text box for the data port.
     */
    private Text fDataPortText = null;
    /**
     * The parent where the new node should be added.
     */
    private TraceSessionGroup fParent = null;
    /**
     * The session name string.
     */
    private String fSessionName = ""; //$NON-NLS-1$;
    /**
     * The  session path string.
     */
    private String fSessionPath = null;
    /**
     * Flag whether the session is snapshot or not
     */
    private boolean fIsSnapshot = false;
    /**
     * Flag whether the session is live or not
     */
    private boolean fIsLive = false;
    /**
     * The live delay
     */
    private Integer fLiveDelay = 0;
    /**
     * Flag whether default location (path) shall be used or not
     */
    private boolean fIsDefaultPath = true;
    /**
     * Flag whether the advanced options are enabled or not
     */
    private boolean fIsAdvancedEnabled = false;
    /**
     * The network URL in case control and data is configured together.
     * If set, fControlUrl and fDataUrl will be null.
     */
    private String fNetworkUrl = null;
    /**
     * The control URL in case control and data is configured separately.
     * If set, fDataUrl will be set too and fNetworkUrl will be null.
     */
    private String fControlUrl = null;
    /**
     * The data URL in case control and data is configured separately.
     * If set, fControlUrl will be set too and fNetworkUrl will be null.
     */
    private String fDataUrl = null;
    /**
     * The trace path string.
     */
    private String fTracePath = null;
    /**
     * The Group for advanced configuration of Live mode.
     */
    private Group fLiveGroup = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public CreateSessionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public void initialize(TraceSessionGroup group) {
       fParent = group;
       fStreamingComposite = null;
       fLiveGroup = null;
       fLiveButton = null;
       fIsLive = false;
       fSnapshotButton = null;
       fSessionName = ""; //$NON-NLS-1$
       fSessionPath = null;
       fIsSnapshot = false;
       fIsDefaultPath = true;
       fIsAdvancedEnabled = false;
       fNetworkUrl = null;
       fControlUrl = null;
       fDataUrl = null;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_CreateSessionDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(CREATE_SESSION_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogAreaa = (Composite) super.createDialogArea(parent);
        setTitle(Messages.TraceControl_CreateSessionDialogTitle);
        setMessage(Messages.TraceControl_CreateSessionDialogMessage);

        // Main dialog panel
        fDialogComposite = new Composite(dialogAreaa, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group sessionGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        sessionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sessionGroup.setLayout(new GridLayout(4, true));

        fUpdateEnablementModifyListener = new UpdateEnablementModifyListener();

        Label sessionNameLabel = new Label(sessionGroup, SWT.RIGHT);
        sessionNameLabel.setText(Messages.TraceControl_CreateSessionNameLabel);
        fSessionNameText = new Text(sessionGroup, SWT.NONE);
        fSessionNameText.setToolTipText(Messages.TraceControl_CreateSessionNameTooltip);
        fSessionNameText.addModifyListener(fUpdateEnablementModifyListener);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        fSessionNameText.setLayoutData(data);

        fSessionPathLabel = new Label(sessionGroup, SWT.RIGHT);
        fSessionPathLabel.setText(Messages.TraceControl_CreateSessionPathLabel);
        fSessionPathText = new Text(sessionGroup, SWT.NONE);
        fSessionPathText.setToolTipText(Messages.TraceControl_CreateSessionPathTooltip);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        fSessionPathText.setLayoutData(data);
        fSessionPathText.addModifyListener(fUpdateEnablementModifyListener);

        if (fParent.isSnapshotSupported() || fParent.isLiveSupported()) {
            fModeButtonGroup = new Group(sessionGroup, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            fModeButtonGroup.setLayoutData(data);
            fModeButtonGroup.setLayout(new GridLayout(3, true));

            SelectionAdapter modeChangedListener = new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fLiveButton != null) {
                        if (fLiveButton.getSelection()) {
                            createAdvancedLiveGroup();
                            updateSessionPathEnablement();
                            updateProtocolComboItems();
                        } else {
                            disposeLiveGroup();
                            updateSessionPathEnablement();
                            updateProtocolComboItems();
                        }
                    }
                    updateEnablement();
                }
            };

            fNormalModeButton = new Button(fModeButtonGroup, SWT.RADIO);
            fNormalModeButton.setText(Messages.TraceControl_CreateSessionNormalLabel);
            fNormalModeButton.setToolTipText(Messages.TraceControl_CreateSessionNormalTooltip);
            fNormalModeButton.setSelection(true);
            fNormalModeButton.addSelectionListener(modeChangedListener);

            if (fParent.isSnapshotSupported()) {
                fSnapshotButton = new Button(fModeButtonGroup, SWT.RADIO);
                fSnapshotButton.setText(Messages.TraceControl_CreateSessionSnapshotLabel);
                fSnapshotButton.setToolTipText(Messages.TraceControl_CreateSessionSnapshotTooltip);
                fSnapshotButton.addSelectionListener(modeChangedListener);
            }

            if (fParent.isLiveSupported()) {
                fLiveButton = new Button(fModeButtonGroup, SWT.RADIO);
                fLiveButton.setText(Messages.TraceControl_CreateSessionLiveLabel);
                fLiveButton.setToolTipText(Messages.TraceControl_CreateSessionLiveTooltip);
                fLiveButton.addSelectionListener(modeChangedListener);
            }
        }

        if (fParent.isNetworkStreamingSupported() || fParent.isLiveSupported()) {
            createAdvancedOptionsComposite();
        }

        return fDialogComposite;
    }

    private void createAdvancedOptionsComposite() {

        fAdvancedGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fAdvancedGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        fAdvancedGroup.setLayout(new GridLayout(1, true));

        fAdvancedButton = new Button(fAdvancedGroup, SWT.PUSH);
        fAdvancedButton.setText(Messages.TraceControl_CreateSessionConfigureStreamingButtonText + " >>>"); //$NON-NLS-1$
        fAdvancedButton.setToolTipText(Messages.TraceControl_CreateSessionConfigureStreamingButtonTooltip);
        fAdvancedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fIsAdvancedEnabled) {
                    fIsAdvancedEnabled = false;
                    fAdvancedButton.setText(">>> " + Messages.TraceControl_CreateSessionConfigureStreamingButtonText); //$NON-NLS-1$
                    fAdvancedButton.setToolTipText(Messages.TraceControl_CreateSessionConfigureStreamingButtonTooltip);

                    if (fParent.isNetworkStreamingSupported()) {
                        updateSessionPathEnablement();
                        disposeConfigureStreamingComposite();
                    }

                    if (fParent.isLiveSupported()) {
                        disposeLiveGroup();
                    }
                } else {
                    fIsAdvancedEnabled = true;
                    fAdvancedButton.setText("<<< " + Messages.TraceControl_CreateSessionNoStreamingButtonText); //$NON-NLS-1$
                    fAdvancedButton.setToolTipText(Messages.TraceControl_CreateSessionNoStreamingButtonTooltip);

                    if (fParent.isNetworkStreamingSupported()) {
                        updateSessionPathEnablement();
                        createConfigureStreamingComposite();
                    }
                    if (fLiveButton != null && fLiveButton.getSelection()) {
                        createAdvancedLiveGroup();
                    }
                }

                updateEnablement();
                getShell().pack();
            }
        });
    }

    private void updateSessionPathEnablement() {
        if (fIsAdvancedEnabled || fIsLive) {
            fSessionPathText.setEnabled(false);
            fSessionPathText.setText(""); //$NON-NLS-1$
            fSessionPathLabel.setText(""); //$NON-NLS-1$
        } else {
            fSessionPathText.setEnabled(true);
            fSessionPathLabel.setText(Messages.TraceControl_CreateSessionPathLabel);
        }
    }

    private void updateProtocolComboItems() {
        if (fControlProtocolCombo == null || fControlProtocolCombo.isDisposed()) {
            return;
        }

        int currentSelection = fControlProtocolCombo.getSelectionIndex() <= COMMON_URL_LAST_INDEX ?
                fControlProtocolCombo.getSelectionIndex() : DEFAULT_URL_INDEX;

        fControlProtocolCombo.removeAll();
        Enum<? extends Enum<?>>[] values;
        if (fIsLive) {
            values = LiveProtocol.values();
        } else if (fLinkDataWithControlButton.getSelection()) {
            values = StreamingProtocol.values();
        } else {
            values = StreamingProtocol2.values();
        }

        String[] controlItems = new String[values.length];
        for (int i = 0; i < controlItems.length; i++) {
            controlItems[i] = values[i].name();
        }
        fControlProtocolCombo.setItems(controlItems);
        fDataProtocolCombo.setItems(controlItems);

        // Set selection
        if (currentSelection != -1) {
            fControlProtocolCombo.select(currentSelection);
            fDataProtocolCombo.select(currentSelection);
        }
    }

    private void createConfigureStreamingComposite() {
        if (fStreamingComposite == null) {
            fStreamingComposite = new Group(fAdvancedGroup, SWT.SHADOW_NONE);
            GridLayout layout = new GridLayout(1, true);
            fStreamingComposite.setLayout(layout);
            fStreamingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            layout = new GridLayout(7, true);
            fStreamingComposite.setLayout(layout);
            fStreamingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            Label tracePathLabel = new Label(fStreamingComposite, SWT.RIGHT);
            tracePathLabel.setText(Messages.TraceControl_CreateSessionTracePathText);
            fTracePathText = new Text(fStreamingComposite, SWT.NONE);
            fTracePathText.setToolTipText(Messages.TraceControl_CreateSessionTracePathTooltip);

            // layout widgets
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 6;
            fTracePathText.setLayoutData(data);
            fTracePathText.addModifyListener(fUpdateEnablementModifyListener);

            fLinkDataWithControlButton = new Button(fStreamingComposite, SWT.CHECK);
            fLinkDataWithControlButton.setText(Messages.TraceControl_CreateSessionLinkButtonText);
            fLinkDataWithControlButton.setToolTipText(Messages.TraceControl_CreateSessionLinkButtonTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 7;
            fLinkDataWithControlButton.setLayoutData(data);
            fLinkDataWithControlButton.setSelection(true);

            Label label = new Label(fStreamingComposite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(fStreamingComposite, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionProtocolLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(fStreamingComposite, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionAddressLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            label.setLayoutData(data);

            label = new Label(fStreamingComposite, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionPortLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(fStreamingComposite, SWT.RIGHT);
            label.setText(Messages.TraceControl_CreateSessionControlUrlLabel);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            fControlProtocolCombo = new CCombo(fStreamingComposite, SWT.READ_ONLY);
            fControlProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionCommonProtocolTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fControlProtocolCombo.setLayoutData(data);
            fControlProtocolCombo.addModifyListener(fUpdateEnablementModifyListener);

            fControlHostAddressText = new Text(fStreamingComposite, SWT.NONE);
            fControlHostAddressText.setToolTipText(Messages.TraceControl_CreateSessionControlAddressTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            fControlHostAddressText.setLayoutData(data);
            fControlHostAddressText.addModifyListener(fUpdateEnablementModifyListener);

            fControlPortText = new Text(fStreamingComposite, SWT.NONE);
            fControlPortText.setToolTipText(Messages.TraceControl_CreateSessionControlPortTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fControlPortText.setLayoutData(data);
            fControlPortText.addModifyListener(fUpdateEnablementModifyListener);

            label = new Label(fStreamingComposite, SWT.RIGHT);
            label.setText(Messages.TraceControl_CreateSessionDataUrlLabel);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            fDataProtocolCombo = new CCombo(fStreamingComposite, SWT.READ_ONLY);
            fDataProtocolCombo.setEnabled(false);
            fDataProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionProtocolTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fDataProtocolCombo.setLayoutData(data);
            fDataProtocolCombo.addModifyListener(fUpdateEnablementModifyListener);

            updateProtocolComboItems();

            fDataHostAddressText = new Text(fStreamingComposite, SWT.NONE);
            fDataHostAddressText.setEnabled(false);
            fDataHostAddressText.setToolTipText(Messages.TraceControl_CreateSessionDataAddressTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            fDataHostAddressText.setLayoutData(data);
            fDataHostAddressText.addModifyListener(fUpdateEnablementModifyListener);

            fDataPortText = new Text(fStreamingComposite, SWT.NONE);
            fDataPortText.setEnabled(true);
            fDataPortText.setToolTipText(Messages.TraceControl_CreateSessionDataPortTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fDataPortText.setLayoutData(data);
            fDataPortText.addModifyListener(fUpdateEnablementModifyListener);

            fCopyProtocolSelectionListener = new ControlProtocolSelectionListener();
            fControlProtocolSelectionListener = new ProtocolComboSelectionListener(fControlProtocolCombo, fControlPortText);
            fDataProtocolSelectionListener = new ProtocolComboSelectionListener(fDataProtocolCombo, fDataPortText);

            fControlProtocolCombo.addSelectionListener(fCopyProtocolSelectionListener);

            fControlUrlKeyListener = new CopyModifyListener(fControlHostAddressText, fDataHostAddressText);
            fControlHostAddressText.addModifyListener(fControlUrlKeyListener);

            fControlProtocolCombo.select(DEFAULT_URL_INDEX);
            fDataProtocolCombo.select(DEFAULT_URL_INDEX);

            fLinkDataWithControlButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fLinkDataWithControlButton.getSelection()) {
                        // Set enablement control data channel inputs
                        fDataProtocolCombo.setEnabled(false);
                        fDataHostAddressText.setEnabled(false);
                        fControlPortText.setEnabled(true);
                        fDataPortText.setEnabled(true);

                        // Update listeners
                        fControlProtocolCombo.removeSelectionListener(fControlProtocolSelectionListener);
                        fDataProtocolCombo.removeSelectionListener(fDataProtocolSelectionListener);
                        fControlProtocolCombo.addSelectionListener(fCopyProtocolSelectionListener);
                        fControlHostAddressText.addModifyListener(fControlUrlKeyListener);

                        updateProtocolComboItems();

                        fDataHostAddressText.setText(fControlHostAddressText.getText());

                        // Update tool tips
                        fControlProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionCommonProtocolTooltip);
                    } else {
                        // Enable data channel inputs
                        fDataProtocolCombo.setEnabled(true);
                        fDataHostAddressText.setEnabled(true);

                        // Update listeners
                        fControlProtocolCombo.removeSelectionListener(fCopyProtocolSelectionListener);
                        fControlProtocolCombo.addSelectionListener(fControlProtocolSelectionListener);
                        fDataProtocolCombo.addSelectionListener(fDataProtocolSelectionListener);
                        fControlHostAddressText.removeModifyListener(fControlUrlKeyListener);

                        updateProtocolComboItems();

                        // Update tool tips
                        fDataProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionProtocolTooltip);
                        fControlProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionProtocolTooltip);

                        // Update control/data port enablement and input
                        if (fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()).equals(StreamingProtocol.net.name()) ||
                                fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()).equals(StreamingProtocol.net6.name())) {
                            fControlPortText.setText(""); //$NON-NLS-1$
                            fControlPortText.setEnabled(false);
                        } else {
                            fControlPortText.setEnabled(true);
                        }

                        if (fDataProtocolCombo.getItem(fDataProtocolCombo.getSelectionIndex()).equals(StreamingProtocol.net.name()) ||
                                fDataProtocolCombo.getItem(fDataProtocolCombo.getSelectionIndex()).equals(StreamingProtocol.net6.name())) {
                            fDataPortText.setText(""); //$NON-NLS-1$
                            fDataPortText.setEnabled(false);
                        } else {
                            fDataPortText.setEnabled(true);
                        }
                    }
                }
            });
        }
    }

    private void createAdvancedLiveGroup() {
        if (fLiveGroup == null && fIsAdvancedEnabled) {
            GridLayout layout = new GridLayout(7, true);
            fLiveGroup = new Group(fAdvancedGroup, SWT.NONE);
            fLiveGroup.setLayout(layout);
            GridData layoutData = new GridData(GridData.FILL_BOTH);
            fLiveGroup.setLayoutData(layoutData);

            Label liveDelayLabel = new Label(fLiveGroup, SWT.RIGHT);
            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            liveDelayLabel.setText(Messages.TraceControl_CreateSessionLiveDelayLabel);
            liveDelayLabel.setLayoutData(layoutData);
            fLiveDelayText = new Text(fLiveGroup, SWT.NONE);
            fLiveDelayText.setText(DEFAULT_TEXT);
            fLiveDelayText.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
            fLiveDelayText.setToolTipText(Messages.TraceControl_CreateSessionLiveDelayTooltip);
            fLiveDelayText.addVerifyListener(new VerifyListener() {
                @Override
                public void verifyText(VerifyEvent e) {
                    // only numbers and default are allowed.
                    e.doit = e.text.matches("[0-9]*") || e.text.matches(DEFAULT_TEXT); //$NON-NLS-1$
                    updateEnablement();
                }
            });
            fLiveDelayText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    updateEnablement();
                }
            });

            fLiveDelayText.addFocusListener(new FocusListener() {

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
            });

            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            layoutData.grabExcessHorizontalSpace = true;
            layoutData.horizontalSpan = 6;
            fLiveDelayText.setLayoutData(layoutData);
            getShell().pack();
        }
    }

    private void disposeLiveGroup() {
        if (fLiveGroup != null) {
            fLiveGroup.dispose();
            fLiveGroup = null;
            getShell().pack();
        }
    }

    private void disposeConfigureStreamingComposite() {
        if (fStreamingComposite != null) {
            fStreamingComposite.dispose();
            fStreamingComposite = null;
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    private void updateEnablement() {
        validate();
        getButton(IDialogConstants.OK_ID).setEnabled(getErrorMessage() == null);
    }

    private void validate() {
        // Validate input data
        fSessionName = fSessionNameText.getText();
        fSessionPath = fSessionPathText.getText();
        setErrorMessage(null);

        if (fParent.isLiveSupported() && fLiveButton != null) {
            fIsLive = fLiveButton.getSelection();
            fLiveDelay = LTTngControlServiceConstants.UNUSED_VALUE;
        }

        if (!"".equals(fSessionPath)) { //$NON-NLS-1$
            // validate sessionPath
            if (!fIsAdvancedEnabled && !fIsLive) {
                TargetNodeComponent node = (TargetNodeComponent)fParent.getParent();
                IRemoteSystemProxy proxy = node.getRemoteSystemProxy();
                IFileServiceSubSystem fsss = proxy.getFileServiceSubSystem();
                if (fsss != null) {
                    try {
                        IRemoteFile remoteFolder = fsss.getRemoteFileObject(fSessionPath, new NullProgressMonitor());

                        if (remoteFolder == null) {
                            setErrorMessage(Messages.TraceControl_InvalidSessionPathError + " (" + fSessionPath + ") \n"); //$NON-NLS-1$ //$NON-NLS-2$
                            return;
                        }

                        if (remoteFolder.exists()) {
                            setErrorMessage(Messages.TraceControl_SessionPathAlreadyExistsError + " (" + fSessionPath + ") \n"); //$NON-NLS-1$ //$NON-NLS-2$
                            return;
                        }
                    } catch (SystemMessageException e) {
                        setErrorMessage(Messages.TraceControl_FileSubSystemError + "\n" + e); //$NON-NLS-1$
                        return;
                    }
                }
            }
            fIsDefaultPath = false;
        }

        if (fParent.isSnapshotSupported()) {
            fIsSnapshot = fSnapshotButton.getSelection();
        }

        fNetworkUrl = null;
        fControlUrl = null;
        fDataUrl = null;

        if (fIsAdvancedEnabled && fStreamingComposite != null) {
            // Validate input data

            if (fIsLive && fLiveGroup != null) {
                String liveDelayText = fLiveDelayText.getText();
                try {
                    fLiveDelay = liveDelayText.equals(DEFAULT_TEXT) ? LTTngControlServiceConstants.UNUSED_VALUE : Integer.valueOf(liveDelayText);
                } catch (NumberFormatException e) {
                    setErrorMessage(Messages.TraceControl_InvalidLiveDelayError);
                    return;
                }
            }

            fTracePath = fTracePathText.getText();

            if (fControlProtocolCombo.getSelectionIndex() < 0) {
                setErrorMessage("Control Protocol Text is empty\n");  //$NON-NLS-1$
                return;
            }

            if ("".equals(fControlHostAddressText.getText())) { //$NON-NLS-1$
                setErrorMessage("Control Address Text is empty\n");  //$NON-NLS-1$
                return;
            }

            if (!fLinkDataWithControlButton.getSelection()) {
                if (fDataProtocolCombo.getSelectionIndex() < 0) {
                    setErrorMessage("Data Protocol Text is empty\n");  //$NON-NLS-1$
                    return;
                }

                if ("".equals(fDataHostAddressText.getText())) { //$NON-NLS-1$
                    setErrorMessage("Data Address Text is empty\n");  //$NON-NLS-1$
                    return;
                }

                fControlUrl = getUrlString(fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()),
                        fControlHostAddressText.getText(),
                        fControlPortText.getText(),
                        null,
                        fTracePath);

                fDataUrl = getUrlString(fDataProtocolCombo.getItem(fDataProtocolCombo.getSelectionIndex()),
                        fDataHostAddressText.getText(),
                        null,
                        fDataPortText.getText(),
                        fTracePath);
            } else {
                fNetworkUrl = getUrlString(fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()),
                        fControlHostAddressText.getText(),
                        fControlPortText.getText(),
                        fDataPortText.getText(),
                        fTracePath);
            }
        }

        if (fIsLive && fNetworkUrl == null && fControlUrl == null && fDataUrl == null) {
            fNetworkUrl = SessionInfo.DEFAULT_LIVE_NETWORK_URK;
        }

        // Check for invalid names
        if (!"".equals(fSessionName) && !fSessionName.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$ //$NON-NLS-2$
            setErrorMessage(Messages.TraceControl_InvalidSessionNameError + " (" + fSessionName + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check if node with name already exists in parent
        if(fParent.containsChild(fSessionName)) {
            setErrorMessage(Messages.TraceControl_SessionAlreadyExistsError + " (" + fSessionName + ")");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
    }

    private static String getUrlString(String proto, String host, String ctrlPort, String dataPort, String sessionPath) {
        //proto://[HOST|IP][:PORT1[:PORT2]][/TRACE_PATH]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(proto);
        stringBuilder.append("://"); //$NON-NLS-1$
        stringBuilder.append(host);

        if ((ctrlPort != null) && (!"".equals(ctrlPort))) { //$NON-NLS-1$
            stringBuilder.append(":"); //$NON-NLS-1$
            stringBuilder.append(ctrlPort);
        }

        if ((dataPort != null) && (!"".equals(dataPort))) { //$NON-NLS-1$
            stringBuilder.append(":"); //$NON-NLS-1$
            stringBuilder.append(dataPort);
        }

        if ((sessionPath != null) && (!"".equals(sessionPath))) { //$NON-NLS-1$
            stringBuilder.append("/"); //$NON-NLS-1$
            stringBuilder.append(sessionPath);
        }
        return stringBuilder.toString();
    }

    private static class CopyModifyListener implements ModifyListener {
        private Text fSource;
        private Text fDestination;

        public CopyModifyListener(Text source, Text destination) {
            fSource = source;
            fDestination = destination;
        }

        @Override
        public void modifyText(ModifyEvent e) {
            fDestination.setText(fSource.getText());
        }
    }

    private class ControlProtocolSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            fDataProtocolCombo.select(fControlProtocolCombo.getSelectionIndex());
            if (fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()).equals(StreamingProtocol.file.name())) {
                fControlPortText.setText(""); //$NON-NLS-1$
                fDataPortText.setText(""); //$NON-NLS-1$
                fControlPortText.setEnabled(false);
                fDataPortText.setEnabled(false);
            } else {
                fControlPortText.setEnabled(true);
                fDataPortText.setEnabled(true);
            }
        }
    }

    private class ProtocolComboSelectionListener extends SelectionAdapter {

        private CCombo fCombo;
        private Text fPortText;

        public ProtocolComboSelectionListener(CCombo combo, Text portText) {
            fCombo = combo;
            fPortText = portText;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (fCombo.getItem(fCombo.getSelectionIndex()).equals(StreamingProtocol.net.name()) ||
                    fCombo.getItem(fCombo.getSelectionIndex()).equals(StreamingProtocol.net6.name())) {
                fPortText.setText(""); //$NON-NLS-1$
                fPortText.setEnabled(false);
            } else {
                fPortText.setEnabled(true);
            }
        }
    }

    @Override
    public ISessionInfo getParameters() {
        ISessionInfo sessionInfo = new SessionInfo(fSessionName);

        boolean isStreaming = (fIsAdvancedEnabled && fStreamingComposite != null) || fIsLive;
        if (isStreaming) {
            sessionInfo.setNetworkUrl(fNetworkUrl);
            sessionInfo.setControlUrl(fControlUrl);
            sessionInfo.setDataUrl(fDataUrl);
            sessionInfo.setStreamedTrace(true);
        } else if (!fIsDefaultPath) {
            sessionInfo.setSessionPath(fSessionPath);
        }

        sessionInfo.setLive(fIsLive);
        sessionInfo.setLiveDelay(fLiveDelay);
        sessionInfo.setSnapshot(fIsSnapshot);

        return sessionInfo;
    }

    private final class UpdateEnablementModifyListener implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            updateEnablement();
        }
    }
}
