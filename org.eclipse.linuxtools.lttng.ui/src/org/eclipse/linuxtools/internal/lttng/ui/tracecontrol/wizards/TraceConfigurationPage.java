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
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>UstTraceChannelConfigurationPage</u></b>
 * <p>
 *  Wizard page implementation to configure a trace (Kernel and UST).
 * </p>
 */
public class TraceConfigurationPage extends WizardPage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private ConfigureTraceWizard fWizard;
    private String fTraceName;
    private String fTraceTransport;
    private String fTracePath;
    private int fMode;
    private int fNumChannel;
    private boolean fIsAppend;
    private boolean fIsNetwork;
    private Text fNameText;
    private Text fTransportText;
    private Text fPathText;
    private Text fNumLttdThreadsText;
    private Button fHostButton;
    private Button fTargetButton;
    private Button fIsAppendButton;
    private Button fFlightRecorderButton;
    private Button fNormalButton;
    private Display fDisplay;
    private String fTraceNameError;
    private String fTracePathError;
    private Button fBrowseButton;
    
    private TraceResource fTraceResource;
    private TraceConfig   fOldTraceConfig;
    private TraceSubSystem fSubSystem;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructore
     * 
     * @param wizard
     */
    public TraceConfigurationPage(ConfigureTraceWizard wizard) {
        super("TraceConfigurationPage"); //$NON-NLS-1$
        setTitle(Messages.ConfigureTraceDialog_Title);
//        setDescription("set description..."); 
        this.fWizard = wizard;
        setPageComplete(false);
        fTraceNameError = ""; //$NON-NLS-1$
        fTracePathError = ""; //$NON-NLS-1$
        fTraceResource = this.fWizard.getSelectedTrace();
        fOldTraceConfig = fTraceResource.getTraceConfig();
        fSubSystem = (TraceSubSystem)this.fWizard.getSelectedTrace().getSubSystem();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createControl(Composite parent) {
        GridData griddata = new GridData();

        griddata = new GridData();
        Composite composite1 = new Composite(parent, SWT.NONE);
        GridLayout compositeLayout1 = new GridLayout(4, false);
        composite1.setSize(520, 300);
        composite1.setLayout(compositeLayout1);
        griddata.horizontalSpan = 3;
        griddata.widthHint = 520;
        griddata.minimumWidth = 520;
        composite1.setLayoutData(griddata);
        fDisplay = this.getShell().getDisplay();
        setControl(composite1);
        Label nameLabel = new Label(composite1, SWT.NULL);
        nameLabel.setText(Messages.NewTraceDialog_TraceName + ":"); //$NON-NLS-1$
        griddata = new GridData();
        griddata.verticalIndent = 20;
        nameLabel.setLayoutData(griddata);

        fNameText = new Text(composite1, SWT.SINGLE | SWT.BORDER);
        if (fTraceResource.isUst()) {
            fNameText.setText(TraceControlConstants.Lttng_Ust_TraceName);
            fNameText.setEnabled(false);
        }

        griddata = new GridData();
        griddata.horizontalAlignment = SWT.FILL;
        griddata.grabExcessHorizontalSpace = true;
        griddata.verticalIndent = 20;
        griddata.horizontalSpan = 3;
        fNameText.setLayoutData(griddata);
        fNameText.setText(fTraceResource.getName());
        fNameText.setEnabled(false);

        Label transportLabel = new Label(composite1, SWT.NULL);
        transportLabel.setText(Messages.ConfigureTraceDialog_Trace_Transport + ":"); //$NON-NLS-1$
        griddata = new GridData();
        transportLabel.setLayoutData(griddata);

        fTransportText = new Text(composite1, SWT.SINGLE | SWT.BORDER);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.FILL;
        griddata.grabExcessHorizontalSpace = true;
        griddata.horizontalSpan = 3;
        fTransportText.setLayoutData(griddata);
        fTransportText.setText(TraceControlConstants.Lttng_Trace_Transport_Relay);
        fTransportText.setEnabled(false); // relay is the only allowed value
        if (fOldTraceConfig != null) {
            fTransportText.setText(fOldTraceConfig.getTraceTransport());
        }

        griddata = new GridData();
        Group composite21 = new Group(composite1, SWT.SHADOW_OUT);
        composite21.setSize(300, 300);
        composite21.setText(Messages.ConfigureTraceDialog_Trace_Location);
        griddata.horizontalAlignment = SWT.FILL;
        griddata.horizontalSpan = 4;
        griddata.verticalIndent = 10;
        griddata.widthHint = 300;
        griddata.minimumWidth = 300;
        composite21.setLayoutData(griddata);
        GridLayout compositeLayout21 = new GridLayout(4, false);
        composite21.setLayout(compositeLayout21);
        fTargetButton = new Button(composite21, SWT.RADIO);
        fTargetButton.setText(Messages.ConfigureTraceDialog_Target);
        fTargetButton.setSelection(true);
        fIsNetwork = false;
        fHostButton = new Button(composite21, SWT.RADIO);
        fHostButton.setText(Messages.ConfigureTraceDialog_Host);
        griddata = new GridData();
        griddata.horizontalSpan = 3;
        fHostButton.setLayoutData(griddata);
        fHostButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fHostButton.getSelection()) {
                    fBrowseButton.setEnabled(true);
                } else {
                    fBrowseButton.setEnabled(false);
                }
                fIsNetwork = fHostButton.getSelection();
                validatePathName(fPathText.getText());
                validate();
            }
        });
        
        Label pathLabel = new Label(composite21, SWT.NULL);
        pathLabel.setText(Messages.ConfigureTraceDialog_Trace_Path);
        griddata = new GridData();
        griddata.verticalIndent = 10;
        pathLabel.setLayoutData(griddata);

        fPathText = new Text(composite21, SWT.SINGLE | SWT.BORDER);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.FILL;
        griddata.grabExcessHorizontalSpace = true;
        griddata.verticalIndent = 10;
        fPathText.setLayoutData(griddata);
        fPathText.setData(""); //$NON-NLS-1$

        fBrowseButton = new Button(composite21, SWT.PUSH);
        fBrowseButton.setText(Messages.ConfigureTraceDialog_Browse + "...");  //$NON-NLS-1$
        griddata = new GridData();
        griddata.grabExcessHorizontalSpace = false;
        griddata.widthHint = 100;
        griddata.verticalIndent = 10;
        fBrowseButton.setLayoutData(griddata);
        fBrowseButton.setEnabled(false);
        fBrowseButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(fDisplay.getActiveShell());
                String newPath = dialog.open();
                if (newPath != null) {
                    fPathText.setText(newPath);
                }
            }
        });

        fNameText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateTraceName(fNameText.getText());
                validate();
            }
        });

        fTransportText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validate();
            }
        });

        fPathText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (!fPathText.isEnabled()) {
                    return;
                }
                validatePathName(fPathText.getText());
                validate();
            }
        });

        griddata = new GridData();
        Composite composite2 = new Composite(composite1, SWT.NONE);
        GridLayout compositeLayout2 = new GridLayout(2, false);
        composite2.setLayout(compositeLayout2);
        griddata.horizontalSpan = 4;
        griddata.widthHint = 500;
        griddata.minimumWidth = 500;
        composite2.setLayoutData(griddata);

        Label numLttdThreadsLabel = new Label(composite2, SWT.NULL);
        numLttdThreadsLabel.setText(Messages.ConfigureTraceDialog_Num_Lttd_Threads + ":"); //$NON-NLS-1$);
        griddata = new GridData();
        griddata.verticalIndent = 10;
        numLttdThreadsLabel.setLayoutData(griddata);

        fNumLttdThreadsText = new Text(composite2, SWT.SINGLE | SWT.BORDER);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.BEGINNING;
        griddata.verticalIndent = 10;
        griddata.widthHint = 50;
        griddata.minimumWidth = 50;
        fNumLttdThreadsText.setLayoutData(griddata);
        if (fTraceResource.isUst()) {
            fNumLttdThreadsText.setText("1"); //$NON-NLS-1$
            fNumLttdThreadsText.setEnabled(false);
        } else {
            fNumLttdThreadsText.setText("2"); //$NON-NLS-1$
        }

        fNumLttdThreadsText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                e.doit = e.text.matches("[0-9]*"); //$NON-NLS-1$
            }
        });

        fNumLttdThreadsText.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validate();
            }
        });

        fIsAppendButton = new Button(composite1, SWT.CHECK);
        fIsAppendButton.setText(Messages.ConfigureTraceDialog_Append);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.BEGINNING;
        griddata.horizontalSpan = 4;
        griddata.verticalIndent = 10;
        fIsAppendButton.setLayoutData(griddata);
        fIsAppend = false;
        fIsAppendButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                fIsAppend = fIsAppendButton.getSelection();
            }
        });
        if (fTraceResource.isUst()) {
            fIsAppendButton.setEnabled(false);
        }

        griddata = new GridData();
        Group composite22 = new Group(composite1, SWT.SHADOW_OUT);
        composite22.setText(Messages.ConfigureTraceDialog_Trace_Mode);
        griddata.horizontalSpan = 4;
        griddata.verticalIndent = 10;
        composite22.setLayoutData(griddata);
        GridLayout compositeLayout22 = new GridLayout(2, false);
        composite22.setLayout(compositeLayout22);
        fNormalButton = new Button(composite22, SWT.RADIO);
        fNormalButton.setText(Messages.ConfigureTraceDialog_Mode_Normal);
        fFlightRecorderButton = new Button(composite22, SWT.RADIO);
        fFlightRecorderButton.setText(Messages.ConfigureTraceDialog_Mode_Flight_Recorder);
        fMode = TraceConfig.NORMAL_MODE;
        fNormalButton.setSelection(true);
        fNormalButton.addListener(SWT.Selection, new Listener() {
        	@Override
        	public void handleEvent(Event e) {
        		if (fNormalButton.getSelection()) {
        			fMode = TraceConfig.NORMAL_MODE;
        		}
        	}
        });
        fFlightRecorderButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fFlightRecorderButton.getSelection()) {
                    fMode = TraceConfig.FLIGHT_RECORDER_MODE;
                }
            }
        });
        if (fTraceResource.isUst()) {
            fFlightRecorderButton.setEnabled(false);
            fNormalButton.setEnabled(false);
        }

        if(fOldTraceConfig != null) {
            fPathText.setText(fOldTraceConfig.getTracePath());
            fTargetButton.setSelection(!fOldTraceConfig.isNetworkTrace());
            fHostButton.setSelection(fOldTraceConfig.isNetworkTrace());
            fIsNetwork = fOldTraceConfig.isNetworkTrace();
            fBrowseButton.setEnabled(true);
            fNumLttdThreadsText.setText(String.valueOf(fOldTraceConfig.getNumChannel()));
            fIsAppendButton.setSelection(fOldTraceConfig.getIsAppend());
            fIsAppend = fOldTraceConfig.getIsAppend();
            fFlightRecorderButton.setSelection(fOldTraceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE);
            fNormalButton.setSelection(fOldTraceConfig.getMode() == TraceConfig.NORMAL_MODE);
        }

        // Depending on the state disable fields, it's only informational then
        if ((fTraceResource.getTraceState() == TraceState.STARTED) || (fTraceResource.getTraceState() == TraceState.PAUSED)) {
            fPathText.setEnabled(false);
            fBrowseButton.setEnabled(false);
            fTargetButton.setEnabled(false);
            fHostButton.setEnabled(false);
            fIsAppendButton.setEnabled(false);
            fNumLttdThreadsText.setEnabled(false);
            fFlightRecorderButton.setEnabled(false);
            fNormalButton.setEnabled(false);
        }

        validate();

        fDisplay.getActiveShell().addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.TRAVERSE_ESCAPE) {
                    event.doit = false;
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            validate();
        }
        super.setVisible(visible);
    }
    
    /*
     * Validates the trace name which has to be unique.
     */
    protected boolean validateTraceName(String name) {
        if (name.length() > 0) {
            TraceResource[] traces = new TraceResource[0];
            try {
                traces = fSubSystem.getAllTraces();
            } catch (SystemMessageException e) {
                SystemBasePlugin.logError("TraceConfigurationPage ", e); //$NON-NLS-1$
            }
            for (int i = 0; i < traces.length; i++) {
                if (traces[i].getName().compareTo(name) == 0) {
                    fTraceNameError = Messages.NewTraceDialog_Error_Already_Exists;
                    setErrorMessage(fTraceNameError);
                    return false;
                }
            }
            final char[] chars = name.toCharArray();
            for (int x = 0; x < chars.length; x++) {
                final char c = chars[x];
                if ((x == 0) && !(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))) {
                    fTraceNameError = Messages.NewTraceDialog_Error_Invalid_First_Char;
                    setErrorMessage(fTraceNameError);
                    return false;
                } else if (x != 0) {
                    if (!((c >= 'a') && (c <= 'z')) && !((c >= 'A') && (c <= 'Z')) && !((c >= '0') && (c <= '9'))) {
                        fTraceNameError = Messages.NewTraceDialog_Error_Invalid_Name;
                        setErrorMessage(fTraceNameError);
                        return false;
                    }
                }
            }
        }
        if (fTracePathError.length() > 0) {
            setErrorMessage(fTracePathError);
        } else {
            setErrorMessage(null);
        }
        fTraceNameError = ""; //$NON-NLS-1$
        return true;
    }

    /*
     * Validates the trace path.
     */
    private boolean validatePathName(String path) {
        if (path.length() > 0) {
            final char c0 = path.charAt(0);
            if (c0 != '/') {
                fTracePathError = Messages.ConfigureTraceDialog_Error_Invalid_Path;
                setErrorMessage(fTracePathError);
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
                        setErrorMessage(fTracePathError);
                        return false;
                    }
                }
                if (path.length() > 1) {
                    for (int i = 0; i < path.length() - 1; i++) {
                        if ((path.charAt(i) == '/') && (path.charAt(i + 1) == '/')) {
                            fTracePathError = Messages.ConfigureTraceDialog_Error_Multiple_Seps;
                            setErrorMessage(fTracePathError);
                            return false;
                        }
                    }
                }
            }
            if (fHostButton.getSelection()) {
                File file = new File(path);
                if (file.isFile()) {
                    fTracePathError = Messages.ConfigureTraceDialog_Error_File_Exists;
                    setErrorMessage(fTracePathError);
                    return false;
                }
                if (path.length() > 1 && !file.getParentFile().canWrite()) {
                    fTracePathError = Messages.ConfigureTraceDialog_Error_Can_Not_Write;
                    setErrorMessage(fTracePathError);
                    return false;
                }
            }
        }
        if (fTraceNameError.length() > 0) {
            setErrorMessage(fTraceNameError);
        } else {
            setErrorMessage(null);
        }
        fTracePathError = ""; //$NON-NLS-1$
        return true;
    }
    
    /*
     * Validates all input values.
     */
    private void validate() {
        if ((fNameText.getText() == null) || (fTransportText.getText() == null) || (fTransportText.getText().length() == 0) || (fNameText.getText().length() == 0)
                || (fNumLttdThreadsText.getText().length() == 0) || (fNumLttdThreadsText.getText().length() == 0)) {
            setPageComplete(false);
            return;
        }
        if (fPathText.getText().length() == 0) {
            setPageComplete(false);
            return;
        }
        if ((fTracePathError.length() > 0) || (fTraceNameError.length() > 0)) {
            setPageComplete(false);
            return;
        }
        fTraceName = fNameText.getText();
        fTraceTransport = fTransportText.getText();
        fTracePath = fPathText.getText();
        fNumChannel = Integer.parseInt(fNumLttdThreadsText.getText());
        
        if (fTraceNameError.length() == 0) {
            setErrorMessage(null);
            setPageComplete(true);
       } else {
            setErrorMessage(fTraceNameError);
            setPageComplete(false);
        }
    }

    /**
     * Gets the trace configuration. 
     * 
     * @return trace configuration
     */
    public TraceConfig getTraceConfig() {
        TraceConfig newTraceConfig = new TraceConfig();
        newTraceConfig.setTraceName(fTraceName);
        newTraceConfig.setTraceTransport(fTraceTransport);
        newTraceConfig.setTracePath(fTracePath);
        newTraceConfig.setNetworkTrace(fIsNetwork);
        newTraceConfig.setIsAppend(fIsAppend);
        newTraceConfig.setMode(fMode);
        newTraceConfig.setNumChannel(fNumChannel);

        return newTraceConfig;
    }

    /**
     * Gets if trace is a local trace (i.e. if trace output is stored on host 
     * where client is running)
     * 
     * @return isLocalTrace
     */
    public boolean isLocalTrace() {
        return fHostButton.getSelection();
    }
}
