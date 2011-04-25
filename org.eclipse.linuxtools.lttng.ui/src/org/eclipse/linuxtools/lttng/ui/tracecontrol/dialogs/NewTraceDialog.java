/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tracecontrol.dialogs;

import java.io.File;

import org.eclipse.linuxtools.lttng.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.lttng.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.lttng.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>NewTraceDialog</u></b>
 * <p>
 * Dialog box to create a new trace.
 * </p>
 */
public class NewTraceDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String fTraceName;
    private String fTraceTransport;
    private String fTracePath;
    private int fMode;
    private int fNumChannel;
    private Boolean fIsAppend;
    private Boolean fIsLocal;
    private Text fNameText;
    private Text fTransportText;
    private Text fPathText;
    private Text fNumChannelText;
    private Button fFinishButton;
    private Button fLocalButton;
    private Button fRemoteButton;
    private Button fIsAppendButton;
    private Button fNoneButton;
    private Button fFlightRecorderButton;
    private Button fNormalButton;
    private Boolean fFinishButtonClicked;
    private Display fDisplay;
    private String fTraceNameError;
    private String fTracePathError;
    private Label fErrorLabel;
    private Button fBrowseButton;
    
    private TraceSubSystem fSubSystem;
    private TargetResource fTargetResource;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor 
     * 
     * @param parent The parent shell
     * @param subSystem The trace SubSystem
     * @param targetResource The parent target resource
     */
    public NewTraceDialog(Shell parent, TraceSubSystem subSystem, TargetResource targetResource) {
        super(parent);
        fSubSystem = subSystem;
        fTargetResource = targetResource;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Makes the dialog visible and returns the trace configuration of the new trace.
     * 
     * @return trace configuration
     */
    public TraceConfig open() {
        Shell parent = getParent();
        final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText(Messages.NewTraceDialog_Title);

        shell.setLayout(new GridLayout(3, false));
        shell.setSize(520, 400);
        shell.setMinimumSize(520, 400);
        shell.setImage(LTTngUiPlugin.getDefault().getImage(LTTngUiPlugin.ICON_ID_NEW_TRACE));

        GridData griddata = new GridData();
        fFinishButtonClicked = Boolean.valueOf(false);
        fTraceNameError = ""; //$NON-NLS-1$
        fTracePathError = ""; //$NON-NLS-1$

        griddata = new GridData();
        Composite composite1 = new Composite(shell, SWT.NONE);
        GridLayout compositeLayout1 = new GridLayout(4, false);
        composite1.setSize(520, 300);
        composite1.setLayout(compositeLayout1);
        griddata.horizontalSpan = 3;
        griddata.widthHint = 520;
        griddata.minimumWidth = 520;
        composite1.setLayoutData(griddata);

        Label nameLabel = new Label(composite1, SWT.NULL);
        nameLabel.setText(Messages.NewTraceDialog_TraceName + ":"); //$NON-NLS-1$
        griddata = new GridData();
        griddata.verticalIndent = 20;
        nameLabel.setLayoutData(griddata);

        fNameText = new Text(composite1, SWT.SINGLE | SWT.BORDER);
        if (fTargetResource.isUst()) {
        	fNameText.setText(TraceControlConstants.Lttng_Ust_TraceName);
        	fNameText.setEnabled(false);
        }

        griddata = new GridData();
        griddata.horizontalAlignment = SWT.FILL;
        griddata.grabExcessHorizontalSpace = true;
        griddata.verticalIndent = 20;
        griddata.horizontalSpan = 3;
        fNameText.setLayoutData(griddata);
        fNameText.setSize(500, 50);

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
        fTransportText.setSize(500, 50);
        fTransportText.setText(TraceControlConstants.Lttng_Trace_Transport_Relay);
        fTransportText.setEnabled(false); // relay is the only allowed value

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
        fRemoteButton = new Button(composite21, SWT.RADIO);
        fRemoteButton.setText(Messages.ConfigureTraceDialog_Remote);
        fRemoteButton.setSelection(true);
        fLocalButton = new Button(composite21, SWT.RADIO);
        fLocalButton.setText(Messages.ConfigureTraceDialog_Local);
        griddata = new GridData();
        griddata.horizontalSpan = 3;
        fLocalButton.setLayoutData(griddata);
        fIsLocal = false;
        fLocalButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fLocalButton.getSelection()) {
                    fIsLocal = true;
                    fBrowseButton.setEnabled(true);
                } else {
                    fIsLocal = false;
                    fBrowseButton.setEnabled(false);
                }
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
                DirectoryDialog dialog = new DirectoryDialog(shell);
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

        Label numChannelLabel = new Label(composite2, SWT.NULL);
        numChannelLabel.setText(Messages.ConfigureTraceDialog_Num_Channels + ":"); //$NON-NLS-1$);
        griddata = new GridData();
        griddata.verticalIndent = 10;
        numChannelLabel.setLayoutData(griddata);

        fNumChannelText = new Text(composite2, SWT.SINGLE | SWT.BORDER);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.BEGINNING;
        griddata.verticalIndent = 10;
        griddata.widthHint = 50;
        griddata.minimumWidth = 50;
        fNumChannelText.setLayoutData(griddata);
        if (fTargetResource.isUst()) {
        	fNumChannelText.setText("1"); //$NON-NLS-1$
        	fNumChannelText.setEnabled(false);
        } else {
        	fNumChannelText.setText("2"); //$NON-NLS-1$
        }

        fNumChannelText.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String string = e.text;
                char[] chars = new char[string.length()];
                string.getChars(0, chars.length, chars, 0);
                for (int i = 0; i < chars.length; i++) {
                    if (!('0' <= chars[i] && chars[i] <= '9')) {
                        e.doit = false;
                        return;
                    }
                }
            }
        });

        fNumChannelText.addListener(SWT.Modify, new Listener() {
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
        fIsAppend = Boolean.valueOf(false);
        fIsAppendButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fIsAppendButton.getSelection()) {
                    fIsAppend = true;
                }
                else {
                    fIsAppend = false;
                }
            }
        });
        if (fTargetResource.isUst()) {
        	fIsAppendButton.setEnabled(false);
        }

        griddata = new GridData();
        Group composite22 = new Group(composite1, SWT.SHADOW_OUT);
        composite22.setText(Messages.ConfigureTraceDialog_Trace_Mode);
        griddata.horizontalSpan = 4;
        griddata.verticalIndent = 10;
        composite22.setLayoutData(griddata);
        GridLayout compositeLayout22 = new GridLayout(3, false);
        composite22.setLayout(compositeLayout22);
        fNoneButton = new Button(composite22, SWT.RADIO);
        fNoneButton.setText(Messages.ConfigureTraceDialog_Mode_None);
        fNoneButton.setSelection(true);
        fFlightRecorderButton = new Button(composite22, SWT.RADIO);
        fFlightRecorderButton.setText(Messages.ConfigureTraceDialog_Mode_Flight_Recorder);
        fNormalButton = new Button(composite22, SWT.RADIO);
        fNormalButton.setText(Messages.ConfigureTraceDialog_Mode_Normal);
        fMode = 0;
        fNoneButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fNoneButton.getSelection()) {
                    fMode = TraceConfig.NONE_MODE;
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
        fNormalButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (fNormalButton.getSelection()) {
                    fMode = TraceConfig.NORMAL_MODE;
                }
            }
        });
        if (fTargetResource.isUst()) {
        	fNoneButton.setEnabled(false);
        	fFlightRecorderButton.setEnabled(false);
        	fNormalButton.setEnabled(false);
        }

        fErrorLabel = new Label(shell, SWT.NULL);
        fDisplay = parent.getDisplay();
        fErrorLabel.setForeground(fDisplay.getSystemColor(SWT.COLOR_RED));
        griddata = new GridData();
        griddata.widthHint = 400;
        griddata.minimumWidth = 400;
        fErrorLabel.setLayoutData(griddata);

        Label shadow_sep_h = new Label(shell, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
        griddata = new GridData();
        griddata.horizontalAlignment = SWT.FILL;
        griddata.horizontalSpan = 4;
        griddata.grabExcessHorizontalSpace = true;
        griddata.verticalIndent = 20;
        shadow_sep_h.setLayoutData(griddata);

        Button cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText(Messages.ConfigureTraceDialog_Cancel);
        griddata = new GridData();
        griddata.grabExcessHorizontalSpace = false;
        griddata.widthHint = 100;
        griddata.horizontalIndent = 290;
        cancelButton.setLayoutData(griddata);

        fFinishButton = new Button(shell, SWT.PUSH);
        fFinishButton.setText(Messages.ConfigureTraceDialog_Finish);
        griddata = new GridData();
        griddata.grabExcessHorizontalSpace = false;
        griddata.widthHint = 100;
        fFinishButton.setLayoutData(griddata);
        fFinishButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fFinishButtonClicked = true;
                shell.dispose();
            }
        });
        validate();
        
        cancelButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fTraceName = null;
                fTraceTransport = null;
                fTracePath = null;
                shell.dispose();
            }
        });

        shell.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.TRAVERSE_ESCAPE) {
                    event.doit = false;
                }
            }
        });
        shell.pack();
        shell.open();

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if ((fTraceName == null) || (fTraceTransport == null) || (fTracePath == null) || (!fFinishButtonClicked)) {
            return null;
        }
        TraceConfig result = new TraceConfig();
        result.setTraceName(fTraceName);
        result.setTraceTransport(fTraceTransport);
        result.setTracePath(fTracePath);
        result.setNetworkTrace(fIsLocal);
        result.setIsAppend(fIsAppend);
        result.setMode(fMode);
        result.setNumChannel(fNumChannel);
        return result;
    }

    /*
     * Validates the trace name which has to be unique.
     */
    private boolean validateTraceName(String name) {
        if (name.length() > 0) {
            TraceResource[] traces = new TraceResource[0];
            try {
                traces = fSubSystem.getAllTraces();
            } catch (SystemMessageException e) {
                SystemBasePlugin.logError("NewTraceDialog ", e); //$NON-NLS-1$
            }
            for (int i = 0; i < traces.length; i++) {
                if (traces[i].getName().compareTo(name) == 0) {
                    fTraceNameError = Messages.NewTraceDialog_Error_Already_Exists;
                    fErrorLabel.setText(fTraceNameError);
                    return false;
                }
            }
            final char[] chars = name.toCharArray();
            for (int x = 0; x < chars.length; x++) {
                final char c = chars[x];
                if ((x == 0) && !(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))) {
                    fTraceNameError = Messages.NewTraceDialog_Error_Invalid_First_Char;
                    fErrorLabel.setText(fTraceNameError);
                    return false;
                } else if (x != 0) {
                    if (!((c >= 'a') && (c <= 'z')) && !((c >= 'A') && (c <= 'Z')) && !((c >= '0') && (c <= '9'))) {
                        fTraceNameError = Messages.NewTraceDialog_Error_Invalid_Name;
                        fErrorLabel.setText(fTraceNameError);
                        return false;
                    }
                }
            }
        }
        if (fTracePathError.length() > 0) {
        	fErrorLabel.setText(fTracePathError);
        } else {
        	fErrorLabel.setText(""); //$NON-NLS-1$
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
            if (fIsLocal) {
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
        }
        if (fTraceNameError.length() > 0) {
        	fErrorLabel.setText(fTraceNameError);
        } else {
        	fErrorLabel.setText(""); //$NON-NLS-1$
        }
        fTracePathError = ""; //$NON-NLS-1$
        return true;
    }
    
    /*
     * It validates all input values.
     */
    private void validate() {
        if ((fNameText.getText() == null) || (fTransportText.getText() == null) || (fTransportText.getText().length() == 0) || (fNameText.getText().length() == 0)
                || (fNumChannelText.getText().length() == 0) || (fNumChannelText.getText().length() == 0)) {
            fFinishButton.setEnabled(false);
            return;
        }
    	if (fPathText.getText().length() == 0) {
            fFinishButton.setEnabled(false);
            return;
    	}
        if ((fTracePathError.length() > 0) || (fTraceNameError.length() > 0)) {
            fFinishButton.setEnabled(false);
            return;
        }
        fTraceName = fNameText.getText();
        fTraceTransport = fTransportText.getText();
        fTracePath = fPathText.getText();
        fNumChannel = Integer.parseInt(fNumChannelText.getText());
        fFinishButton.setEnabled(true);
    }

}