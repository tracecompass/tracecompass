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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
public class CreateSessionDialog extends Dialog implements ICreateSessionDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String CREATE_SESSION_ICON_FILE = "icons/elcl16/add_button.gif"; //$NON-NLS-1$

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
    private final static int COMMON_URL_LAST_INDEX = 1;
    /**
     *  Index of default streaming protocol.
     */
    private final static int DEFAULT_URL_INDEX = 0;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private Control fControl = null;
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
     * The Group for stream configuration.
     */
    private Group fMainStreamingGroup = null;
    /**
     * The button to show streaming options.
     */
    private Button fConfigureStreamingButton = null;
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
    private String fSessionName = null;
    /**
     * The  session path string.
     */
    private String fSessionPath = null;
    /**
     * Flag whether default location (path) shall be used or not
     */
    private boolean fIsDefaultPath = true;
    /**
     * Flag whether the trace is streamed or not
     */
    private boolean fIsStreamedTrace = false;
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

    @Override
    public String getSessionName() {
        return fSessionName;
    }

    @Override
    public String getSessionPath() {
        return fSessionPath;
    }

    @Override
    public boolean isDefaultSessionPath() {
        return fIsDefaultPath;
    }

    @Override
    public void initialize(TraceSessionGroup group) {
       fParent = group;
       fStreamingComposite = null;
       fSessionName = null;
       fSessionPath = null;
       fIsDefaultPath = true;
       fIsStreamedTrace = false;
       fNetworkUrl = null;
       fControlUrl = null;
       fDataUrl = null;
    }

    @Override
    public boolean isStreamedTrace() {
        return fIsStreamedTrace;
    }
    @Override
    public String getNetworkUrl() {
        return fNetworkUrl;
    }
    @Override
    public String getControlUrl() {
        return fControlUrl;
    }
    @Override
    public String getDataUrl() {
        return fDataUrl;
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
        newShell.setText(Messages.TraceControl_CreateSessionDialogTitle);
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
        sessionGroup.setLayout(new GridLayout(4, true));

        Label sessionNameLabel = new Label(sessionGroup, SWT.RIGHT);
        sessionNameLabel.setText(Messages.TraceControl_CreateSessionNameLabel);
        fSessionNameText = new Text(sessionGroup, SWT.NONE);
        fSessionNameText.setToolTipText(Messages.TraceControl_CreateSessionNameTooltip);

        fSessionPathLabel = new Label(sessionGroup, SWT.RIGHT);
        fSessionPathLabel.setText(Messages.TraceControl_CreateSessionPathLabel);
        fSessionPathText = new Text(sessionGroup, SWT.NONE);
        fSessionPathText.setToolTipText(Messages.TraceControl_CreateSessionPathTooltip);

        // layout widgets
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;

        fSessionNameText.setLayoutData(data);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        fSessionPathText.setLayoutData(data);

        if (fParent.isNetworkStreamingSupported()) {
            createAdvancedOptionsComposite();
        }

        return fDialogComposite;
    }

    private void createAdvancedOptionsComposite() {

        fMainStreamingGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fMainStreamingGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        fMainStreamingGroup.setLayout(new GridLayout(1, true));

        fConfigureStreamingButton = new Button(fMainStreamingGroup, SWT.PUSH);
        fConfigureStreamingButton.setText(Messages.TraceControl_CreateSessionConfigureStreamingButtonText + " >>>"); //$NON-NLS-1$
        fConfigureStreamingButton.setToolTipText(Messages.TraceControl_CreateSessionConfigureStreamingButtonTooltip);
        fConfigureStreamingButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fIsStreamedTrace) {
                    fIsStreamedTrace = false;
                    fConfigureStreamingButton.setText(">>> " + Messages.TraceControl_CreateSessionConfigureStreamingButtonText); //$NON-NLS-1$
                    fConfigureStreamingButton.setToolTipText(Messages.TraceControl_CreateSessionConfigureStreamingButtonTooltip);
                    fSessionPathText.setEnabled(true);
                    fSessionPathLabel.setText(Messages.TraceControl_CreateSessionPathLabel);
                    disposeConfigureStreamingComposite();
                } else {
                    fIsStreamedTrace = true;
                    fConfigureStreamingButton.setText("<<< " + Messages.TraceControl_CreateSessionNoStreamingButtonText); //$NON-NLS-1$
                    fConfigureStreamingButton.setToolTipText(Messages.TraceControl_CreateSessionNoStreamingButtonTooltip);
                    fSessionPathText.setEnabled(false);
                    fSessionPathText.setText(""); //$NON-NLS-1$
                    fSessionPathLabel.setText(""); //$NON-NLS-1$
                    createConfigureStreamingComposite();
                }

                fDialogComposite.layout();

                Point clientArea = fControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                Rectangle trim = getShell().computeTrim(0, 0, clientArea.x, clientArea.y);
                getShell().setSize(trim.width, trim.height);
            }
        });
    }

    private void createConfigureStreamingComposite() {
        if (fStreamingComposite == null) {
            fStreamingComposite = new Composite(fMainStreamingGroup, SWT.NONE);
            GridLayout layout = new GridLayout(1, true);
            fStreamingComposite.setLayout(layout);
            fStreamingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            Group urlGroup = new Group(fStreamingComposite, SWT.SHADOW_NONE);
            layout = new GridLayout(7, true);
            urlGroup.setLayout(layout);
            urlGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

            Label tracePathLabel = new Label(urlGroup, SWT.RIGHT);
            tracePathLabel.setText(Messages.TraceControl_CreateSessionTracePathText);
            fTracePathText = new Text(urlGroup, SWT.NONE);
            fTracePathText.setToolTipText(Messages.TraceControl_CreateSessionTracePathTooltip);

            // layout widgets
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 6;
            fTracePathText.setLayoutData(data);

            fLinkDataWithControlButton = new Button(urlGroup, SWT.CHECK);
            fLinkDataWithControlButton.setText(Messages.TraceControl_CreateSessionLinkButtonText);
            fLinkDataWithControlButton.setToolTipText(Messages.TraceControl_CreateSessionLinkButtonTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 7;
            fLinkDataWithControlButton.setLayoutData(data);
            fLinkDataWithControlButton.setSelection(true);

            Label label = new Label(urlGroup, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(urlGroup, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionProtocolLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(urlGroup, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionAddressLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            label.setLayoutData(data);

            label = new Label(urlGroup, SWT.NONE);
            label.setText(Messages.TraceControl_CreateSessionPortLabelText);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            label = new Label(urlGroup, SWT.RIGHT);
            label.setText(Messages.TraceControl_CreateSessionControlUrlLabel);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            fControlProtocolCombo = new CCombo(urlGroup, SWT.READ_ONLY);
            fControlProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionCommonProtocolTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fControlProtocolCombo.setLayoutData(data);

            fControlHostAddressText = new Text(urlGroup, SWT.NONE);
            fControlHostAddressText.setToolTipText(Messages.TraceControl_CreateSessionControlAddressTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            fControlHostAddressText.setLayoutData(data);

            fControlPortText = new Text(urlGroup, SWT.NONE);
            fControlPortText.setToolTipText(Messages.TraceControl_CreateSessionControlPortTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fControlPortText.setLayoutData(data);

            label = new Label(urlGroup, SWT.RIGHT);
            label.setText(Messages.TraceControl_CreateSessionDataUrlLabel);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            label.setLayoutData(data);

            fDataProtocolCombo = new CCombo(urlGroup, SWT.READ_ONLY);
            fDataProtocolCombo.setEnabled(false);
            fDataProtocolCombo.setToolTipText(Messages.TraceControl_CreateSessionProtocolTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fDataProtocolCombo.setLayoutData(data);

            String items[] = new String[StreamingProtocol.values().length];
            for (int i = 0; i < items.length; i++) {
                items[i] = StreamingProtocol.values()[i].name();
            }
            fControlProtocolCombo.setItems(items);
            fDataProtocolCombo.setItems(items);

            fDataHostAddressText = new Text(urlGroup, SWT.NONE);
            fDataHostAddressText.setEnabled(false);
            fDataHostAddressText.setToolTipText(Messages.TraceControl_CreateSessionDataAddressTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            fDataHostAddressText.setLayoutData(data);

            fDataPortText = new Text(urlGroup, SWT.NONE);
            fDataPortText.setEnabled(true);
            fDataPortText.setToolTipText(Messages.TraceControl_CreateSessionDataPortTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 1;
            fDataPortText.setLayoutData(data);

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

                        // Get previous selection and validate
                        int currentSelection = fControlProtocolCombo.getSelectionIndex() <= COMMON_URL_LAST_INDEX ?
                                fControlProtocolCombo.getSelectionIndex() : DEFAULT_URL_INDEX;

                        // Update combo box items
                        fControlProtocolCombo.removeAll();
                        String[] controlItems = new String[StreamingProtocol.values().length];
                        for (int i = 0; i < controlItems.length; i++) {
                            controlItems[i] = StreamingProtocol.values()[i].name();
                        }
                        fControlProtocolCombo.setItems(controlItems);
                        fDataProtocolCombo.setItems(controlItems);

                        // Set selection
                        fControlProtocolCombo.select(currentSelection);
                        fDataProtocolCombo.select(currentSelection);
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

                        // Update combo box items
                        int currentSelection = fControlProtocolCombo.getSelectionIndex();
                        fControlProtocolCombo.removeAll();
                        String[] controlItems = new String[StreamingProtocol2.values().length];
                        for (int i = 0; i < controlItems.length; i++) {
                            controlItems[i] = StreamingProtocol2.values()[i].name();
                        }
                        fControlProtocolCombo.setItems(controlItems);
                        fDataProtocolCombo.setItems(controlItems);

                        // Set selection
                        fControlProtocolCombo.select(currentSelection);
                        fDataProtocolCombo.select(currentSelection);

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

    @Override
    protected void okPressed() {
        // Validate input data
        fSessionName = fSessionNameText.getText();
        fSessionPath = fSessionPathText.getText();

        if (!"".equals(fSessionPath)) { //$NON-NLS-1$
            // validate sessionPath
            if (!fIsStreamedTrace) {
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
            }
            fIsDefaultPath = false;
        }

        fNetworkUrl = null;
        fControlUrl = null;
        fDataUrl = null;

        if (fIsStreamedTrace) {
            // Validate input data
            fTracePath = fTracePathText.getText();

            if (fControlProtocolCombo.getSelectionIndex() < 0) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_CreateSessionDialogTitle,
                        "Control Protocol Text is empty\n");  //$NON-NLS-1$
                return;
            }

            if ("".equals(fControlHostAddressText.getText())) { //$NON-NLS-1$
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_CreateSessionDialogTitle,
                        "Control Address Text is empty\n");  //$NON-NLS-1$
                return;
            }

            if(!fLinkDataWithControlButton.getSelection()) {
                if (fDataProtocolCombo.getSelectionIndex() < 0) {
                    MessageDialog.openError(getShell(),
                            Messages.TraceControl_CreateSessionDialogTitle,
                            "Control Protocol Text is empty\n");  //$NON-NLS-1$
                    return;
                }

                if ("".equals(fDataHostAddressText.getText())) { //$NON-NLS-1$
                    MessageDialog.openError(getShell(),
                            Messages.TraceControl_CreateSessionDialogTitle,
                            "Control Address Text is empty\n");  //$NON-NLS-1$
                    return;
                }

                fControlUrl = getUrlString(fControlProtocolCombo.getItem(fControlProtocolCombo.getSelectionIndex()),
                        fControlHostAddressText.getText(),
                        fControlPortText.getText(),
                        null,
                        fTracePath);

                fDataUrl = getUrlString(fControlProtocolCombo.getItem(fDataProtocolCombo.getSelectionIndex()),
                        fDataHostAddressText.getText(),
                        null,
                        fDataPortText.getText(),
                        fTracePath);
            } else {
                fNetworkUrl = getUrlString(fControlProtocolCombo.getItem(fDataProtocolCombo.getSelectionIndex()),
                        fControlHostAddressText.getText(),
                        fControlPortText.getText(),
                        fDataPortText.getText(),
                        fTracePath);
            }
        }

        // Check for invalid names
        if (!"".equals(fSessionName) && !fSessionName.matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$ //$NON-NLS-2$
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

}
