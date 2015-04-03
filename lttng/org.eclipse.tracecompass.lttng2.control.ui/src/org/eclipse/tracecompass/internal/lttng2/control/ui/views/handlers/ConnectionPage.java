/**********************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert - Extracted from NewConnectionDialog
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ConnectionContentProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ConnectionTreeLabelProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;

final class ConnectionPage extends WizardPage {
    /**
         *
         *
         *
         */

    private static final int BUTTONS_NUMBER_OF_COLUMNS = 3;
    private Button fNewButton;
    private Button fEditButton;
    private IRemoteConnection fConnection;
    private TreeViewer fConnectionTree = null;

    ConnectionPage(String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        GridData gd;
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(dialogComposite, SWT.NONE);
        label.setText(Messages.TraceControl_NewNodeExistingConnectionGroupName);
        gd = new GridData();
        label.setLayoutData(gd);
        gd.widthHint = label.computeSize(-1, -1).x + convertWidthInCharsToPixels(NewConnectionWizard.LABEL_WIDTH_CHARS);
        // Existing connections group
        this.fConnectionTree = new TreeViewer(dialogComposite);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.fConnectionTree.getTree().setLayoutData(gd);
        gd.widthHint = convertWidthInCharsToPixels(NewConnectionWizard.CONNECTIONTREE_WIDTH_CHARS);
        gd.heightHint = convertHeightInCharsToPixels(NewConnectionWizard.CONNECTIONTREE_HEIGHT_CHARS);
        fConnectionTree.setLabelProvider(new ConnectionTreeLabelProvider());
        fConnectionTree.setContentProvider(new ConnectionContentProvider());
        fConnectionTree.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ConnectionPage.this.onSelectionChanged();
            }
        });
        fConnectionTree.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                getWizard().performFinish();
            }
        });

        Composite buttons = new Composite(dialogComposite, SWT.NONE);
        layout = new GridLayout(BUTTONS_NUMBER_OF_COLUMNS, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

        new Label(buttons, SWT.NONE);

        fEditButton = new Button(buttons, SWT.PUSH);
        fEditButton.setText(Messages.TraceControl_NewNodeEditButtonName);
        setButtonLayoutData(fEditButton);
        fEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onEditConnection();
            }
        });

        fNewButton = new Button(buttons, SWT.PUSH);
        fNewButton.setText(Messages.TraceControl_NewNodeCreateButtonText);
        setButtonLayoutData(fNewButton);
        fNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onNewConnection();
            }
        });
        setControl(dialogComposite);

    }

    private void onSelectionChanged() {
        setConnection();
        setPageComplete(true);
        fEditButton.setEnabled(canEdit(fConnection));
        fNewButton.setEnabled(getServiceForCreation() != null);
    }

    private void onEditConnection() {
        setConnection();
        if (fConnection != null) {
            IRemoteConnectionType connectionType = fConnection.getConnectionType();
            if (connectionType != null) {
                IRemoteUIConnectionService connManager = connectionType.getService(IRemoteUIConnectionService.class);
                if (connManager != null) {
                    IRemoteUIConnectionWizard wiz = connManager.getConnectionWizard(getShell());
                    wiz.setConnection(fConnection.getWorkingCopy());
                    IRemoteConnectionWorkingCopy result = wiz.open();
                    if (result != null) {
                        try {
                            result.save();
                        } catch (RemoteConnectionException e) {
                            Activator.getDefault().logError(e.toString(), e);
                        }
                        fConnectionTree.refresh();
                    }
                }
            }
        }
    }

    private void setConnection() {
        Object o = ((IStructuredSelection) fConnectionTree.getSelection()).getFirstElement();
        fConnection = o instanceof IRemoteConnection ? (IRemoteConnection) o : null;
    }

    private static boolean canEdit(IRemoteConnection conn) {
        if (conn == null) {
            return false;
        }
        return conn.getConnectionType().canEdit();
    }

    private IRemoteConnectionType getServiceForCreation() {
        Object o = ((IStructuredSelection) fConnectionTree.getSelection()).getFirstElement();
        IRemoteConnectionType result = null;
        if (o instanceof IRemoteConnectionType) {
            result = (IRemoteConnectionType) o;
        } else if (o instanceof IRemoteConnection) {
            IRemoteConnection iRemoteConnection = (IRemoteConnection) o;
            result = iRemoteConnection.getConnectionType();
        } else {
            return null;
        }
        if (!result.canAdd()) {
            return null;
        }

        return result;
    }

    public void onNewConnection() {
        IRemoteConnectionType rs = getServiceForCreation();
        if (rs != null) {
            IRemoteUIConnectionService uiService = rs.getService(IRemoteUIConnectionService.class);
            if (uiService != null) {
                IRemoteUIConnectionWizard wiz = uiService.getConnectionWizard(getShell());
                if (wiz != null) {
                    IRemoteConnectionWorkingCopy wc = wiz.open();
                    if (wc != null) {
                        IRemoteConnection conn = null;
                        try {
                            conn = wc.save();
                            fConnectionTree.refresh();
                            fConnectionTree.setSelection(new StructuredSelection(conn), true);
                        } catch (RemoteConnectionException e) {
                            Activator.getDefault().logError(e.toString(), e);
                        }
                    }
                }
            }
        }
    }
}