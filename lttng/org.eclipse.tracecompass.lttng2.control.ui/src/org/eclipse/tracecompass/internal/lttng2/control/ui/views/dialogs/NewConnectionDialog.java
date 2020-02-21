/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import static java.text.MessageFormat.format;
import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;

/**
 * <p>
 * Dialog box for connection information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class NewConnectionDialog extends Dialog implements INewConnectionDialog {

    private static final int BUTTONS_NUMBER_OF_COLUMNS = 3;
    private static final int LABEL_WIDTH_CHARS = 4;
    private static final int CONNECTIONTREE_HEIGHT_CHARS = 10;
    private static final int CONNECTIONTREE_WIDTH_CHARS = 40;
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/target_add.gif"; //$NON-NLS-1$
    private static final String PROVIDERS_ICON_FILE = "icons/obj16/providers.gif"; //$NON-NLS-1$
    private static final String CONNECTION_ICON_FILE = "icons/obj16/target_connected.gif"; //$NON-NLS-1$

    private final class ConnectionTreeLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof IRemoteConnection) {
                IRemoteConnection rc = (IRemoteConnection) element;
                return getConnectionLabel(rc);
            } else if (element instanceof IRemoteConnectionType) {
                IRemoteConnectionType rs = (IRemoteConnectionType) element;
                return rs.getName();
            }
            return Messages.TraceControl_UnknownNode;
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IRemoteConnection) {
                return Activator.getDefault().loadIcon(CONNECTION_ICON_FILE);
            }
            return Activator.getDefault().loadIcon(PROVIDERS_ICON_FILE);
        }
    }

    private static final class ConnectionContentProvider implements ITreeContentProvider {
        private static final Object[] NO_CHILDREN = {};

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do nothing
        }

        @Override
        public void dispose() {
            // Do nothing
        }

        @Override
        public Object[] getElements(Object inputElement) {
            List<Object> children = new ArrayList<>();
            IRemoteServicesManager manager = TmfRemoteConnectionFactory.getService(IRemoteServicesManager.class);
            if (manager != null) {
                children.addAll(manager.getAllConnectionTypes());
            }
            return children.toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IRemoteConnectionType) {
                return getConnections((IRemoteConnectionType) parentElement);
            }
            return NO_CHILDREN;
        }

        private static IRemoteConnection[] getConnections(IRemoteConnectionType parentElement) {
            List<IRemoteConnection> connectionList = parentElement.getConnections();
            IRemoteConnection[] result = connectionList.toArray(new IRemoteConnection[connectionList.size()]);
            Arrays.sort(result, (o1, o2) -> getConnectionLabel(o1).compareTo(getConnectionLabel(o2)));
            return result;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IRemoteConnection) {
                return ((IRemoteConnection) element).getConnectionType();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The host combo box.
     */
    private TreeViewer fConnectionTree = null;
    /**
     * The push button for creating a new connection.
     */
    private Button fNewButton = null;
    /**
     * The push button for editing a connection.
     */
    private Button fEditButton = null;

    private IRemoteConnection fConnection;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            The shell
     */
    public NewConnectionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_NewDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    @Override
    protected Control createContents(Composite parent) {
        Control result = super.createContents(parent);
        fConnectionTree.setAutoExpandLevel(2);
        fConnectionTree.setInput(this);

        IRemoteServicesManager manager = TmfRemoteConnectionFactory.getService(IRemoteServicesManager.class);
        if (manager == null) {
            return result;
        }
        List<IRemoteConnectionType> providers = manager.getAllConnectionTypes();
        if (!providers.isEmpty()) {
            IRemoteConnectionType provider = providers.get(0);
            IRemoteConnection[] connections = ConnectionContentProvider.getConnections(provider);
            if (connections.length > 0) {
                fConnectionTree.setSelection(new StructuredSelection(connections[0]));
            } else {
                fConnectionTree.setSelection(new StructuredSelection(provider));
            }
        } else {
            onSelectionChanged();
        }
        return result;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // Main dialog panel
        GridData gd;
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(dialogComposite, SWT.NONE);
        label.setText(Messages.TraceControl_NewNodeExistingConnectionGroupName);
        gd = new GridData();
        label.setLayoutData(gd );
        gd.widthHint = label.computeSize(-1, -1).x + convertWidthInCharsToPixels(LABEL_WIDTH_CHARS);
        // Existing connections group
        fConnectionTree = new TreeViewer(dialogComposite);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        fConnectionTree.getTree().setLayoutData(gd);
        gd.widthHint = convertWidthInCharsToPixels(CONNECTIONTREE_WIDTH_CHARS);
        gd.heightHint = convertHeightInCharsToPixels(CONNECTIONTREE_HEIGHT_CHARS);
        fConnectionTree.setLabelProvider(new ConnectionTreeLabelProvider());
        fConnectionTree.setContentProvider(new ConnectionContentProvider());
        fConnectionTree.addSelectionChangedListener(event -> onSelectionChanged());
        fConnectionTree.addDoubleClickListener(event -> okPressed());

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

        return dialogComposite;
    }

    private void onSelectionChanged() {
        setConnection();
        getButton(OK).setEnabled(fConnection != null);
        fEditButton.setEnabled(canEdit(fConnection));
        fNewButton.setEnabled(getServiceForCreation() != null);
    }

    private IRemoteConnectionType getServiceForCreation() {
        Object o = ((IStructuredSelection) fConnectionTree.getSelection()).getFirstElement();
        IRemoteConnectionType result = null;
        if (o instanceof IRemoteConnectionType) {
            result = (IRemoteConnectionType) o;
        } else if (o instanceof IRemoteConnection) {
            result = ((IRemoteConnection) o).getConnectionType();
        } else {
            return null;
        }

        if (!result.canAdd()) {
            return null;
        }

        return result;
    }

    private static boolean canEdit(IRemoteConnection conn) {
        if (conn == null) {
            return false;
        }
        return conn.getConnectionType().canEdit();
    }

    private void onNewConnection() {
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
                        } catch (RemoteConnectionException e) {
                            Activator.getDefault().logWarning("Connection configuration could not be saved for " + fConnection.getName() , e); //$NON-NLS-1$
                        }
                        if (conn != null) {
                            fConnectionTree.refresh();
                            fConnectionTree.setSelection(new StructuredSelection(conn), true);
                        }
                    }
                }
            }
        }
    }

    private void onEditConnection() {
        setConnection();
        if (fConnection != null) {
            IRemoteUIConnectionService ui = fConnection.getConnectionType().getService(IRemoteUIConnectionService.class);
            if (ui != null) {
                    IRemoteUIConnectionWizard wiz = ui.getConnectionWizard(getShell());
                    wiz.setConnection(fConnection.getWorkingCopy());
                    IRemoteConnectionWorkingCopy result = wiz.open();
                    if (result != null) {
                        try {
                            result.save();
                        } catch (RemoteConnectionException e) {
                            Activator.getDefault().logWarning("Connection configuration could not be saved for " + fConnection.getName() , e); //$NON-NLS-1$
                        }
                        fConnectionTree.refresh();
                    }
            }
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        setConnection();
        if (fConnection != null) {
            super.okPressed();
        }
    }

    private void setConnection() {
        Object o = ((IStructuredSelection) fConnectionTree.getSelection()).getFirstElement();
        fConnection = o instanceof IRemoteConnection ? (IRemoteConnection) o : null;
    }

    @Override
    public IRemoteConnection getConnection() {
        return fConnection;
    }

    private static String getConnectionLabel(IRemoteConnection rc) {
        StringBuffer label = new StringBuffer();
        label.append(rc.getName());
        if (rc.hasService(IRemoteConnectionHostService.class)) {
            IRemoteConnectionHostService service = checkNotNull(rc.getService(IRemoteConnectionHostService.class));
            label.append(format(" [{0}]", service.getHostname())); //$NON-NLS-1$
        }
        return label.toString();
    }
}
