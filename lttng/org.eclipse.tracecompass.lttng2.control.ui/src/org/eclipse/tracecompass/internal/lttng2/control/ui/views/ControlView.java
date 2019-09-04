/*******************************************************************************
 * Copyright (c) 2009, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Filled with content
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponentChangedListener;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlContentProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlLabelProvider;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlRoot;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * <p>
 * View implementation for Trace Control.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ControlView extends ViewPart implements ITraceControlComponentChangedListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.internal.lttng2.ui.views.control"; //$NON-NLS-1$

    private static final String KEY_REMOTE_CONNECTION_NAME = "rc_name_"; //$NON-NLS-1$
    private static final String KEY_REMOTE_PROVIDER = "rc_id_"; //$NON-NLS-1$
    /**
     * The default expand level.
     */
    private static final int DEFAULT_EXPAND_LEVEL = 3;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The tree viewer.
     */
    private TreeViewer fTreeViewer = null;
    /**
     * The trace control root node. This provides access to the whole model.
     */
    private ITraceControlComponent fRoot = null;

    private List<IRemoteConnection> fInitialConnections;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the trace control tree node (model)
     *
     * @return the trace control tree node (model).
     */
    public ITraceControlComponent getTraceControlRoot() {
        return fRoot;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        // Filter that shows all children of a match node
        PatternFilter filter = new PatternFilter() {
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                ITraceControlComponent parentElement = (ITraceControlComponent) element;
                while (parentElement != null) {
                    if (super.isLeafMatch(viewer, parentElement)) {
                        return true;
                    }
                    parentElement = parentElement.getParent();
                }
                return false;
            }
        };

        // Create filtered tree
        FilteredTree filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true, true) {
            @Override
            protected void updateToolbar(boolean visible) {
                super.updateToolbar(visible);
                treeViewer.expandToLevel(DEFAULT_EXPAND_LEVEL);
            }
        };

        fTreeViewer = filteredTree.getViewer();
        ColumnViewerToolTipSupport.enableFor(fTreeViewer);

        fTreeViewer.setContentProvider(new TraceControlContentProvider());
        fTreeViewer.setLabelProvider(new TraceControlLabelProvider());

        // Create model root
        fRoot = new TraceControlRoot();
        fRoot.addComponentListener(this);
        if (fInitialConnections != null) {
            for (IRemoteConnection rc : fInitialConnections) {
                TargetNodeComponent node = new TargetNodeComponent(rc.getName(), fRoot, rc);
                fRoot.addChild(node);
            }
            fInitialConnections = null;
        }
        fTreeViewer.setInput(fRoot);

        // Create context menu for the tree viewer
        createContextMenu();

        getSite().setSelectionProvider(fTreeViewer);
    }

    @Override
    public void saveState(IMemento memento) {
        int i = 0;
        for (ITraceControlComponent cmp : fRoot.getChildren()) {
            if (cmp instanceof TargetNodeComponent) {
                IRemoteConnection rc = ((TargetNodeComponent) cmp).getRemoteSystemProxy().getRemoteConnection();
                memento.putString(KEY_REMOTE_PROVIDER + i, rc.getConnectionType().getId());
                memento.putString(KEY_REMOTE_CONNECTION_NAME + i, rc.getName());
                i++;
            }
        }
        super.saveState(memento);
    }


    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            fInitialConnections = new ArrayList<>();
            for(int i = 0; ; i++) {
                String id = memento.getString(KEY_REMOTE_PROVIDER + i);
                String name = memento.getString(KEY_REMOTE_CONNECTION_NAME + i);
                if ((id == null) || (name == null)) {
                    break;
                }
                IRemoteConnection conn = TmfRemoteConnectionFactory.getRemoteConnection(id, name);
                if (conn != null) {
                    fInitialConnections.add(conn);
                }
            }
        }
    }

    @Override
    public void setFocus() {
        fTreeViewer.getControl().setFocus();
    }

    @Override
    public void componentAdded(ITraceControlComponent parent, ITraceControlComponent component) {
        componentChanged(parent);
    }

    @Override
    public void componentRemoved(ITraceControlComponent parent, ITraceControlComponent component) {
        componentChanged(parent);
    }

    @Override
    public void componentChanged(final ITraceControlComponent component) {
        if (fTreeViewer.getTree().isDisposed()) {
            return;
        }

        UIJob myJob = new UIJob("Refresh") { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (fTreeViewer.getTree().isDisposed()) {
                    return Status.OK_STATUS;
                }

                fTreeViewer.refresh(component);

                // Change selection needed
                final ISelection sel = fTreeViewer.getSelection();
                fTreeViewer.setSelection(null);
                fTreeViewer.setSelection(sel);

                // Show component that was changed
                fTreeViewer.reveal(component);

                return Status.OK_STATUS;
            }
        };
        myJob.setUser(false);
        myJob.setSystem(true);
        myJob.schedule();
    }

    /**
     * Sets the selected component in the tree
     * @param component - component to select
     */
    public void setSelection(ITraceControlComponent component) {
        ITraceControlComponent[] components = new ITraceControlComponent[1];
        components[0] = component;
        setSelection(components);
    }

    /**
     * Sets the selected components in the tree
     * @param components - array of components to select
     */
    public void setSelection(ITraceControlComponent[] components) {
        final StructuredSelection selection = new StructuredSelection(components);
        UIJob myJob = new UIJob("Select") { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                fTreeViewer.setSelection(selection);
                return Status.OK_STATUS;
            }
        };
        myJob.setUser(false);
        myJob.schedule();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Creates the context sensitive menu.
     */
    private void createContextMenu() {
        // First we create a menu Manager
        final MenuManager menuManager = new MenuManager();
        final Menu menu = menuManager.createContextMenu(fTreeViewer.getTree());
        // Set the MenuManager
        fTreeViewer.getTree().setMenu(menu);
        getSite().registerContextMenu(menuManager, fTreeViewer);
    }
}
