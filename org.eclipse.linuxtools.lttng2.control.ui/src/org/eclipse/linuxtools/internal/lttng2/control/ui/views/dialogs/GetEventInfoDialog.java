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
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
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
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * Dialog box for collecting information about the events to enable.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class GetEventInfoDialog extends Dialog implements IGetEventInfoDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/enable_event.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The session combo box.
     */
    private CCombo fSessionsCombo = null;
    /**
     * The channel combo box.
     */
    private CCombo fChannelsCombo = null;
    /**
     * The filter text
     */
    private Text fFilterText;
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
    /**
     * The filter expression
     */
    private String fFilterExpression;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor of dialog box.
     * @param shell - the shell for the dialog box
     */
    public GetEventInfoDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public TraceSessionComponent getSession() {
        return fSessions[fSessionIndex];
    }

    @Override
    public TraceChannelComponent getChannel() {
        return fChannel;
    }

    @Override
    public void setIsKernel(boolean isKernel) {
        fIsKernel = isKernel;
    }

    @Override
    public void setSessions(TraceSessionComponent[] sessions) {
        fSessions = Arrays.copyOf(sessions, sessions.length);
    }

    @Override
    public String getFilterExpression() {
       return fFilterExpression;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_EnableEventsDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group sessionsGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        sessionsGroup.setText(Messages.TraceControl_EnableEventsSessionGroupName);
        layout = new GridLayout(1, true);
        sessionsGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        sessionsGroup.setLayoutData(data);

        fSessionsCombo = new CCombo(sessionsGroup, SWT.READ_ONLY);
        fSessionsCombo.setToolTipText(Messages.TraceControl_EnableEventsSessionsTooltip);
        fSessionsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String items[] = new String[fSessions.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(fSessions[i].getName());
        }

        fSessionsCombo.setItems(items);
        fSessionsCombo.setEnabled(fSessions.length > 0);

        Group channelsGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        channelsGroup.setText(Messages.TraceControl_EnableEventsChannelGroupName);
        layout = new GridLayout(1, true);
        channelsGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        channelsGroup.setLayoutData(data);

        fChannelsCombo = new CCombo(channelsGroup, SWT.READ_ONLY);
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
                        String selectionItems[] = new String[fChannels.length];
                        for (int i = 0; i < selectionItems.length; i++) {
                            selectionItems[i] = String.valueOf(fChannels[i].getName());
                        }
                        fChannelsCombo.setItems(selectionItems);
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

        // take first session to test whether events filtering is supported or not
        if (fSessions[0].isEventFilteringSupported() && !fIsKernel) {
            Group filterMainGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
            filterMainGroup.setText(Messages.TraceControl_EnableEventsFilterGroupName);
            layout = new GridLayout(2, false);
            filterMainGroup.setLayout(layout);
            data = new GridData(GridData.FILL_HORIZONTAL);
            filterMainGroup.setLayoutData(data);

            fFilterText = new Text(filterMainGroup, SWT.LEFT);
            fFilterText.setToolTipText(Messages.TraceControl_EnableEventsFilterTooltip);
            data = new GridData(GridData.FILL_HORIZONTAL);
            fFilterText.setLayoutData(data);
        }

        getShell().setMinimumSize(new Point(300, 200));

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {

        if (fSessionsCombo.getSelectionIndex() < 0) {
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableEventsDialogTitle,
                  Messages.TraceControl_EnableEventsNoSessionError);
            return;
        }

        fSessionIndex = fSessionsCombo.getSelectionIndex();

        // if no channel is available or no channel is selected use default channel indicated by fChannel=null
        fChannel = null;
        if ((fChannels != null) && (fChannelsCombo.getSelectionIndex() >= 0)) {
            fChannel = fChannels[fChannelsCombo.getSelectionIndex()];
        }

        // initialize filter with null
        fFilterExpression = null;
        if (fSessions[0].isEventFilteringSupported() && !fIsKernel) {
            String tempFilter = fFilterText.getText();

            if(!tempFilter.isEmpty() && !tempFilter.matches("\\s*")) { //$NON-NLS-1$
                fFilterExpression = tempFilter;
            }
        }

        super.okPressed();
    }
}
