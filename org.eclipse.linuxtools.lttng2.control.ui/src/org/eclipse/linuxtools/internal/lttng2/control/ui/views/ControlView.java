/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Filled with content
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponentChangedListener;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceControlContentProvider;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceControlLabelProvider;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceControlRoot;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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
        // Create tree viewer
        fTreeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        ColumnViewerToolTipSupport.enableFor(fTreeViewer);

        fTreeViewer.setContentProvider(new TraceControlContentProvider());
        fTreeViewer.setLabelProvider(new TraceControlLabelProvider());

        // Create model root
        fRoot = new TraceControlRoot();
        fRoot.addComponentListener(this);
        fTreeViewer.setInput(fRoot);

        // Create context menu for the tree viewer
        createContextMenu();

        getSite().setSelectionProvider(fTreeViewer);

        RSECorePlugin.getTheSystemRegistry(); // to load RSE
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
