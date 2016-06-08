/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Base dialog box for collecting information about the event(s) or logger(s) to
 * enable.
 *
 * @author Bruno Roy
 */
public class BaseGetInfoDialog extends Dialog implements IBaseGetInfoDialog {

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
    protected CCombo fSessionsCombo = null;
    /**
     * The list of available sessions.
     */
    protected TraceSessionComponent[] fSessions;
    /**
     * Index in session array (selected session).
     */
    protected int fSessionIndex = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor of dialog box.
     *
     * @param shell
     *            the shell for the dialog box
     */
    public BaseGetInfoDialog(Shell shell) {
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
    public void setSessions(TraceSessionComponent[] sessions) {
        fSessions = Arrays.copyOf(sessions, sessions.length);
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
    // Only the component that are common to every type of GetInfoDialog
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Creating the group for the sessions
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

        // Default listener, should be overrided in the sub classes
        fSessionsCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fSessionIndex = fSessionsCombo.getSelectionIndex();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        getShell().setMinimumSize(new Point(300, 200));

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }
}
