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
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
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
                if (rc.getRemoteServices() == RemoteServices.getLocalServices()) {
                    return rc.getName();
                }

                return format("{0} [{1}]", rc.getName(), rc.getAddress()); //$NON-NLS-1$
            } else if (element instanceof IRemoteServices) {
                IRemoteServices rs = (IRemoteServices) element;
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
        private static List<IRemoteServices> fProviders;

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            List<Object> children = new ArrayList<>();
            children.addAll(RemoteServices.getLocalServices().getConnectionManager().getConnections());

            List<IRemoteServices> result = getProviders();
            children.addAll(result);
            return children.toArray();
        }

        private static List<IRemoteServices> getProviders() {
            if (fProviders == null) {
                IExtensionRegistry registry = Platform.getExtensionRegistry();
                IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.remote.core", "remoteServices"); //$NON-NLS-1$ //$NON-NLS-2$
                List<IRemoteServices> result = new ArrayList<>();
                if (extensionPoint != null) {
                    Set<String> handled = new HashSet<>();
                    handled.add(RemoteServices.getLocalServices().getId());
                    for (IConfigurationElement ce : extensionPoint.getConfigurationElements()) {
                        String id = ce.getAttribute("id"); //$NON-NLS-1$
                        if (handled.add(id)) {
                            IRemoteServices service = RemoteServices.getRemoteServices(id);
                            if (service != null) {
                                result.add(service);
                            }
                        }
                    }
                    Collections.sort(result);
                }
                fProviders = Collections.unmodifiableList(result);
            }
            return fProviders;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IRemoteServices) {
                return getConnections((IRemoteServices) parentElement);
            }
            return NO_CHILDREN;
        }

        private static IRemoteConnection[] getConnections(IRemoteServices parentElement) {
            List<IRemoteConnection> connectionList = parentElement.getConnectionManager().getConnections();
            IRemoteConnection[] result = connectionList.toArray(new IRemoteConnection[connectionList.size()]);
            Arrays.sort(result);
            return result;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IRemoteConnection) {
                return ((IRemoteConnection) element).getRemoteServices();
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

        List<IRemoteServices> providers = ConnectionContentProvider.getProviders();
        if (!providers.isEmpty()) {
            IRemoteServices provider = providers.get(0);
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
        fConnectionTree.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                onSelectionChanged();
            }
        });
        fConnectionTree.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
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

        return dialogComposite;
    }

    private void onSelectionChanged() {
        setConnection();
        getButton(OK).setEnabled(fConnection != null);
        fEditButton.setEnabled(canEdit(fConnection));
        fNewButton.setEnabled(getServiceForCreation() != null);
    }

    private IRemoteServices getServiceForCreation() {
        Object o = ((IStructuredSelection) fConnectionTree.getSelection()).getFirstElement();
        IRemoteServices result = null;
        if (o instanceof IRemoteServices) {
            result = (IRemoteServices) o;
        } else if (o instanceof IRemoteConnection) {
            result = ((IRemoteConnection) o).getRemoteServices();
        } else {
            return null;
        }

        if ((result.getCapabilities() & IRemoteServices.CAPABILITY_ADD_CONNECTIONS) == 0) {
            return null;
        }

        return result;
    }

    private static boolean canEdit(IRemoteConnection conn) {
        if (conn == null) {
            return false;
        }
        IRemoteServices rs = conn.getRemoteServices();
        return (rs.getCapabilities() & IRemoteServices.CAPABILITY_EDIT_CONNECTIONS) != 0;
    }

    private void onNewConnection() {
        IRemoteServices rs = getServiceForCreation();
        if (rs != null) {
            IRemoteUIServices uiService = RemoteUIServices.getRemoteUIServices(rs);
            if (uiService != null) {
                IRemoteUIConnectionWizard wiz = uiService.getUIConnectionManager().getConnectionWizard(getShell());
                if (wiz != null) {
                    IRemoteConnectionWorkingCopy wc = wiz.open();
                    if (wc != null) {
                        IRemoteConnection conn = wc.save();
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
            IRemoteUIServices ui = RemoteUIServices.getRemoteUIServices(fConnection.getRemoteServices());
            if (ui != null) {
                IRemoteUIConnectionManager connManager = ui.getUIConnectionManager();
                if (connManager != null) {
                    IRemoteUIConnectionWizard wiz = connManager.getConnectionWizard(getShell());
                    wiz.setConnection(fConnection.getWorkingCopy());
                    IRemoteConnectionWorkingCopy result = wiz.open();
                    if (result != null) {
                        result.save();
                        fConnectionTree.refresh();
                    }
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
}
