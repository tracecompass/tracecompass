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

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * <b><u>EnableEventsDialog</u></b>
 * <p>
 * Dialog box for collecting information about the events to enable.
 * </p>
 */
public class GetEventInfoDialog extends Dialog implements IGetEventInfoDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/enable.gif"; //$NON-NLS-1$ 

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The Group for the session combo box.
     */
    private Group fSessionsGroup = null;
    /**
     * The Group for the channel combo box.
     */
    private Group fChannelsGroup = null;
    /**
     * The session combo box.
     */
    private CCombo fSessionsCombo = null;
    /**
     * The channel combo box.
     */
    private CCombo fChannelsCombo = null;
    /**
     * The list of available sessions.
     */
    private TraceSessionComponent[] fSessions;
    /**
     * True for kernel, false for UST.
     */
    private boolean fIsKernel;
    /**
     * Index in session array (selected session).
     */
    private int fSessionIndex = 0;
    /**
     * The Channel where the events should be enabled. 
     */
    private TraceChannelComponent fChannel;
    /**
     * List of available channels of the selected session.
     */
    private TraceChannelComponent[] fChannels;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor of dialog box.
     * @param shell - the shell for the dialog box
     */
    public GetEventInfoDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog#getSession()
     */
    @Override
    public TraceSessionComponent getSession() {
        return fSessions[fSessionIndex];
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog#getChannel()
     */
    @Override
    public TraceChannelComponent getChannel() {
        return fChannel;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IGetEventInfoDialog#setIsKernel(boolean)
     */
    @Override
    public void setIsKernel(boolean isKernel) {
        fIsKernel = isKernel;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IGetEventInfoDialog#setSessions(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent[])
     */
    @Override
    public void setSessions(TraceSessionComponent[] sessions) {
        fSessions = Arrays.copyOf(sessions, sessions.length);
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
        newShell.setText(Messages.TraceControl_EnableEventsDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
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
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fSessionsGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fSessionsGroup.setText(Messages.TraceControl_EnableEventsSessionGroupName);
        layout = new GridLayout(1, true);
        fSessionsGroup.setLayout(layout); 
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        fSessionsGroup.setLayoutData(data);

        fSessionsCombo = new CCombo(fSessionsGroup, SWT.READ_ONLY);
        fSessionsCombo.setToolTipText(Messages.TraceControl_EnableEventsSessionsTooltip);
        fSessionsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        String items[] = new String[fSessions.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(fSessions[i].getName());
        }

        fSessionsCombo.setItems(items);
        fSessionsCombo.setEnabled(fSessions.length > 0);
        
        fChannelsGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fChannelsGroup.setText(Messages.TraceControl_EnableEventsChannelGroupName);
        layout = new GridLayout(1, true);
        fChannelsGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fChannelsGroup.setLayoutData(data);
        
        fChannelsCombo = new CCombo(fChannelsGroup, SWT.READ_ONLY);
        fChannelsCombo.setToolTipText(Messages.TraceControl_EnableEventsChannelsTooltip);
        fChannelsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fChannelsCombo.setEnabled(false);

        fSessionsCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fSessionIndex = fSessionsCombo.getSelectionIndex();
                
                if (fSessionIndex >= 0) {
                    TraceDomainComponent domain = null;
                    TraceDomainComponent[] domains = fSessions[fSessionIndex].getDomains();
                    for (int i = 0; i < domains.length; i++) {

                        if (domains[i].isKernel() == fIsKernel) {
                            domain = domains[i];
                            break;
                        }
                    }

                    if (domain != null) {
                        fChannels = domain.getChannels();
                        String items[] = new String[fChannels.length];
                        for (int i = 0; i < items.length; i++) {
                            items[i] = String.valueOf(fChannels[i].getName());
                        }
                        fChannelsCombo.setItems(items);
                        fChannelsCombo.setEnabled(fChannels.length > 0);
                    } else { 
                        fChannelsCombo.setItems(new String[0]);
                        fChannelsCombo.setEnabled(false);
                        fChannels = null;
                   }
                   fChannelsCombo.getParent().getParent().layout();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        getShell().setMinimumSize(new Point(300, 200));
        
        return fDialogComposite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        
        if (fSessionsCombo.getSelectionIndex() < 0) {
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableEventsDialogTitle,
                  Messages.TraceControl_EnableEventsNoSessionError);  
            return;
        }

        fSessionIndex = fSessionsCombo.getSelectionIndex();

        if ((fChannels != null) && (fChannels.length > 0) && (fChannelsCombo.getSelectionIndex() < 0)) {
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_EnableEventsDialogTitle,
                    Messages.TraceControl_EnableEventsNoChannelError);  
              return;
        }
        
        if ((fChannels != null) && (fChannels.length > 0)) {
            fChannel = fChannels[fChannelsCombo.getSelectionIndex()];
        }

        super.okPressed();
    }
}
